package com.ming.server.config;

import com.ming.server.ioc.Bean;
import com.ming.server.ioc.SimpleIOC;
import io.netty.util.HashedWheelTimer;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

import static java.nio.file.Files.delete;

@Slf4j
@Bean
public class SetConfig {
    private static final int SHARD_COUNT = 16;  // 分片数
    private final List<ConcurrentHashMap<String, String>> setShards; // 存储 Key-Value
    private final ConcurrentHashMap<String, Long> ttlMap; // 存储 Key-TTL
    private final ScheduledExecutorService cleaner; // 定时清理任务
    private static final int CLEAN_BATCH_SIZE = 100;  // 每次最多清理 100 个 key
    private static final double THRESHOLD_PERCENT = 0.1; // 10% 过期 key 触发暂停
    private static int INIT_CAPACITY = 1024;
    private static final long TASK_TIMEOUT_MS = 30000; // **任务最大执行时间 30s**

    public SetConfig() {
        this.setShards = new ArrayList<>(SHARD_COUNT);
        for (int i = 0; i < SHARD_COUNT; i++) {
            setShards.add(new ConcurrentHashMap<>(INIT_CAPACITY));
        }

        this.ttlMap = new ConcurrentHashMap<>();
        this.cleaner = Executors.newScheduledThreadPool(1);
        startTimerTask(); // 开启定时清理任务
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

    public void startTimerTask(){
        TimeWheelConfig timeWheelConfig = SimpleIOC.getBean(TimeWheelConfig.class);
        HashedWheelTimer timer = timeWheelConfig.getTimer();
        timer.newTimeout(timeout -> {
            Future<?> future = cleaner.submit(this::startCleanupTask);
            try {
                future.get(TASK_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            }catch (TimeoutException e) {
                log.info("ttlMap，清理任务超时");
                future.cancel(true); // **超时取消任务**
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
            startCleanupTask();
        },1,TimeUnit.MINUTES);
    }

    // 定期清理 ttlMap
    private void startCleanupTask() {
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
                Thread.sleep(5000); // 过期 key 少，休眠 10 秒
            } catch (InterruptedException ignored) {}
        }
    }

    // 停止清理任务
    public void shutdown() {
        cleaner.shutdown();
    }
}
