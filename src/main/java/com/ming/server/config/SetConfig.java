package com.ming.server.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

import static java.nio.file.Files.delete;

public class SetConfig {
    private static final int SHARD_COUNT = 16;  // 分片数
    private final List<ConcurrentHashMap<String, String>> setShards; // 存储 Key-Value
    private final ConcurrentHashMap<String, Long> ttlMap; // 存储 Key-TTL
    private final ScheduledExecutorService cleaner; // 定时清理任务
    private static final int CLEAN_BATCH_SIZE = 100;  // 每次最多清理 100 个 key
    private static final double THRESHOLD_PERCENT = 0.1; // 10% 过期 key 触发暂停
    private static int INIT_CAPACITY = 1024;


    //实现单例模式
    private static volatile SetConfig instance;

    private SetConfig() {
        this.setShards = new ArrayList<>(SHARD_COUNT);
        for (int i = 0; i < SHARD_COUNT; i++) {
            setShards.add(new ConcurrentHashMap<>(INIT_CAPACITY));
        }

        this.ttlMap = new ConcurrentHashMap<>();
        this.cleaner = Executors.newScheduledThreadPool(1);
        startCleanupTask(); // 开启定时清理任务
    }

    public static SetConfig getSetConfig() {
        if (instance == null) {
            synchronized (SetConfig.class) {
                if (instance == null) {
                    instance = new SetConfig();
                }
            }
        }
        return instance;
    }


    // 计算 key 所在的 shard
    private int getShardIndex(String key) {
        return (key.hashCode() & 0x7FFFFFFF) % SHARD_COUNT;
    }

    // 使用computeIfPresent保证get del的原子性操作
    public String get(String key) {
        int index = getShardIndex(key);
        return setShards.get(index).compute(key,(k,value)->{
            Long expireTime = ttlMap.get(k);
            if (expireTime!=null && System.currentTimeMillis()>expireTime) {  //过期或者
                ttlMap.remove(k);
                return null;
            }
            return value;
        });
    }

    // 存储 key-value，带 TTL ，由于要存两个hashmap，那么此时有4个操作，单个操作是原子性的，但是多个操作执行顺序将导致并发问题
    public void set(String key, String value, int ttlSeconds) {
        int index = getShardIndex(key);
        setShards.get(index).compute(key,(k,oldvalue)->{   //oldvalue是setShards的旧值
            if (ttlSeconds > 0) {
                ttlMap.put(k,System.currentTimeMillis()+ttlSeconds + ttlSeconds*1000L);
            }
            return value;
        });
    }

    // 防止多个线程同时删除一个key，保证原子性
    public Boolean delete(String key) {
        int index = getShardIndex(key);
        return setShards.get(index).compute(key,(k,oldvalue)->{
            ttlMap.remove(key);
            return null;
        }) == null;
    }


    // 定期清理 ttlMap
    private void startCleanupTask() {
        cleaner.scheduleAtFixedRate(()->{
            long now = System.currentTimeMillis();
            int expiredCount = 0;
            int totalKeys = ttlMap.size();

            if (totalKeys == 0) return;

            Iterator<Map.Entry<String, Long>> iterator = ttlMap.entrySet().iterator();
            while (iterator.hasNext()) {
                if (expiredCount >= CLEAN_BATCH_SIZE) break;
                Map.Entry<String, Long> entry = iterator.next();
                if (entry.getValue() <now || entry.getValue() != -1){
                    delete(entry.getKey());
                    expiredCount++;
                    iterator.remove();
                }
            }
            // 计算当前 TTL Map 中的过期 key 占比
            double expiredRatio = (expiredCount * 1.0) / totalKeys;

            // 如果过期 key 少于 THRESHOLD_PERCENT（比如 10%），则暂停清理
            if (expiredRatio < THRESHOLD_PERCENT) {
                try {
                    Thread.sleep(10000); // 过期 key 少，休眠 10 秒
                } catch (InterruptedException ignored) {}
            }

        }, 5,5,TimeUnit.SECONDS);
    }

    // 停止清理任务
    public void shutdown() {
        cleaner.shutdown();
    }
}
