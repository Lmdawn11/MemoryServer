package com.ming.server.config;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class AOFManager {
    private static final String AOF_FILE = "persistence/aof/aof.log"; // AOF 文件路径
    private static final String AOF_REWRITE_FILE = "persistence/aof/aof_rewrite.log"; // AOF 整理文件

    private final BlockingQueue<String[]> aofQueue = new LinkedBlockingQueue<>(); // **AOF 记录队列**
    private final BlockingQueue<String[]> rewriteBufferQueue = new LinkedBlockingQueue<>(); // **AOF 整理缓冲队列**
    private static final long AOF_MAX_SIZE = 10 * 1024 * 1024; // **10MB 触发整理**
    private volatile boolean running = true;
    private Thread aofThread;
    private volatile boolean rewriteMode = false; // **标记 AOF Rewrite 状态**

    private final ReentrantLock lock = new ReentrantLock();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private HashedWheelTimer timer;
    // **单例模式 - 只初始化一次**
    private static volatile AOFManager instance;

    public AOFManager() {
        readAOFToMemory();
        aofThread = new Thread(this::processAOFQueue);
        aofThread.start();
        readAOFToMemory();//读取aof中的内容到memory中
        startTaskCheckAofSizeAndRewrite();
    }

    private void startTaskCheckAofSizeAndRewrite() {
        timer.newTimeout(new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                checkAOFSizeAndRewrite();
            }
        }, 1, TimeUnit.MINUTES);
    }
    /**
     * 检查 AOF 文件大小，并决定是否触发 rewriteAOF
     */
    private void checkAOFSizeAndRewrite() {
        File aofFile = new File(AOF_FILE);
        if (aofFile.exists() && aofFile.length() > AOF_MAX_SIZE) {
            log.info("[AOF] 文件过大，触发 AOF Rewrite...");
            rewriteAOF();
        }
    }
    /*
    启动时恢复AOF到内存中
     */
    private void readAOFToMemory() {
        File file = new File(AOF_FILE);
        /**
         * 确保 AOF 目录存在
         */
        if (!file.exists()) {
            File directory = file.getParentFile();
            if (!directory.exists()) {
                directory.mkdirs();
            }
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(AOF_FILE), StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                executeRecoveredCommand(parseAOFCommand(reader, line));;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    /**
    * 解析命令
     **/
    private String[] parseAOFCommand(BufferedReader reader, String firstLine) throws IOException {
        if (!firstLine.startsWith("*")) return new String[]{};
        int argCount = Integer.parseInt(firstLine.substring(1));
        String[] commandParts = new String[argCount];
        for (int i = 0; i < argCount; i++) {
            reader.readLine();
            commandParts[i] = reader.readLine();
        }
        reader.readLine(); //去掉那个换行符
        return commandParts;
    }

    /*
    执行恢复的AOF命令
     */
    private void executeRecoveredCommand(String[] commandParts) {
        if (commandParts.length <2) return;
        String cmdType = commandParts[0];
        String key = commandParts[1];
        SetConfig setShards = SetConfig.getSetConfig();
        switch (cmdType) {
            case "set":
                String value = commandParts[2];
                int ttl = (commandParts.length == 4) ? Integer.parseInt(commandParts[3]) : -1;
                setShards.set(key, value, ttl);
                break;
            case "del":
                setShards.delete(key);
                break;
        }
    }

    /**
    单例模式，只初始化一次
     **/
    public static AOFManager getAOFManager() {
        if (instance == null) {
            synchronized (AOFManager.class) {
                if (instance == null) {
                    instance = new AOFManager();
                }
            }
        }
        return instance;
    }

    /**
     * AOF文件整理（不会影响主线程）
     */
    public synchronized void rewriteAOF() {
        log.info("开始整理 rewriteAOF ");
        rewriteMode = true; // **启用 rewrite 模式，开始记录新的数据**

        File file = new File(AOF_REWRITE_FILE);    //确保rewrite aof文件存在
        if (!file.exists()) {
            File directory = file.getParentFile();
            if (!directory.exists()) {
                directory.mkdirs();
            }
        }

        Map<String, String> latestEntries = new HashMap<>();

        // **Step 1: 读取 AOF 旧数据 以后还需要再增加新的命令
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(AOF_FILE), StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] commandParts = parseAOFCommand(reader, line);// **处理旧 AOF 记录**
                String key = commandParts[1];
                if ("set".equalsIgnoreCase(commandParts[0])) {
                    latestEntries.put(key, formatRESP(commandParts));
                } else if ("del".equalsIgnoreCase(commandParts[0])) {
                    latestEntries.remove(key);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            rewriteMode = false; // **整理失败，恢复模式**
            return;
        }


        // **Step 2: 写入整理后的 AOF_REWRITE_FILE**
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(AOF_REWRITE_FILE, false))) {
            for (Map.Entry<String, String> entry : latestEntries.entrySet()) {
                writer.write(entry.getValue());
                writer.newLine();
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            rewriteMode = false; // **整理失败，恢复模式**
            return;
        }


        // **Step 3: 追加 rewriteBufferQueue 里的最新数据**
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(AOF_REWRITE_FILE, true))) {
            while (!rewriteBufferQueue.isEmpty()) {
                String[] commandParts = rewriteBufferQueue.poll();
                writer.write(formatRESP(commandParts));
                writer.newLine();
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            rewriteMode = false; // **整理失败，恢复模式**
            return;
        }

        // **Step 4: 原子替换旧 AOF 文件**
        // **暂停 AOF 线程，确保不会写入 AOF_FILE**
        running = false;
        try {
            aofThread.join(); //等待aof线程结束
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.info("AOF join错误");
        }

        try {
            Files.deleteIfExists(Paths.get(AOF_FILE)); // 确保原 AOF_FILE 被删除
            Files.move(Paths.get(AOF_REWRITE_FILE), Paths.get(AOF_FILE), StandardCopyOption.REPLACE_EXISTING);
            log.info("[AOF] 整理完成，文件已更新！");
        } catch (IOException e) {
            e.printStackTrace();
            log.error("[AOF] AOF Rewrite 失败，保留原文件");
        } finally {
            rewriteMode = false; // **关闭 rewriteMode**
            restartAOFThread();
        }
    }

    /**
     * 重新启动 AOF 线程
     */
    private void restartAOFThread() {
        running = true;
        aofThread = new Thread(this::processAOFQueue);
        aofThread.start();
    }

    /**
     * 记录 AOF 命令，格式为 ["SET", "mykey", "myvalue"]
     */
    public void logCommand(String... commandParts) {
        lock.lock();
        try{
            aofQueue.offer(commandParts); // 放入队列
            if (rewriteMode) {
                rewriteBufferQueue.offer(commandParts);
            }
        }finally {
            lock.unlock();
        }
    }

    /**
     * 线程异步写入 AOF 文件
     */
    private void processAOFQueue() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(AOF_FILE, true))) {
            while (running || !aofQueue.isEmpty()) {
                String[] commandParts = aofQueue.poll();
                if (commandParts != null) {
                    writer.write(formatRESP(commandParts)); // 转换成 RESP
                    writer.write("\n");
                    writer.flush();
                } else {
                    Thread.sleep(100); // 减少 CPU 占用
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 确保 AOF 目录存在
     */
    private void ensureAOFDirectory() {
        File file = new File(AOF_FILE);
        File directory = file.getParentFile();
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    /**
     * **将命令转换为 RESP 格式**
     */
    private String formatRESP(String[] commandParts) {
        StringBuilder sb = new StringBuilder();
        sb.append("*").append(commandParts.length).append("\n");
        for (String part : commandParts) {
            sb.append("$").append(part.getBytes(StandardCharsets.UTF_8).length).append("\n");
            sb.append(part).append("\n");
        }
        return sb.toString();
    }

    /**
     * 服务器关闭时，确保 AOF 线程安全退出
     */
    public void shutdown() {
        running = false;
        try {
            aofThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
