package com.ming.server.config;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AOFManager {
    private static final String AOF_FILE = "persistence/aof/aof.log"; // AOF 文件路径
    private final BlockingQueue<String[]> aofQueue = new LinkedBlockingQueue<>(); // AOF 队列
    private volatile boolean running = true;
    private final Thread aofThread;

    // **单例模式 - 只初始化一次**
    private static volatile AOFManager instance;

    public AOFManager() {
        ensureAOFDirectory(); // 确保目录存在
        aofThread = new Thread(this::processAOFQueue);
        aofThread.start();
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
