package com.ming.server.config;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.nio.charset.StandardCharsets;

@Slf4j
public class AOFManager {
    private static final String AOF_FILE = "persistence/aof/aof.log"; // AOF 文件路径
    private final BlockingQueue<String[]> aofQueue = new LinkedBlockingQueue<>(); // AOF 队列
    private volatile boolean running = true;
    private final Thread aofThread;

    // **单例模式 - 只初始化一次**
    private static volatile AOFManager instance;

    public AOFManager() {
        readAOFToMemory();
        aofThread = new Thread(this::processAOFQueue);
        aofThread.start();
        readAOFToMemory();//读取aof中的内容到memory中
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
                processAOFCommand(reader, line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void processAOFCommand(BufferedReader reader, String firstLine) throws IOException {
        if (!firstLine.startsWith("*")) return; //不是确定的格式，直接返回
        int argCount = Integer.parseInt(firstLine.substring(1));
        String[] commandParts = new String[argCount];
        for (int i = 0; i < argCount; i++) {
            reader.readLine();// 跳过 `$length` 行
            commandParts[i] = reader.readLine();
        }
        executeRecoveredCommand(commandParts);
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

    /*
    单例模式，只初始化一次
     */
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
     * 记录 AOF 命令，格式为 ["SET", "mykey", "myvalue"]
     */
    public void logCommand(String... commandParts) {
        aofQueue.offer(commandParts); // 放入队列
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
