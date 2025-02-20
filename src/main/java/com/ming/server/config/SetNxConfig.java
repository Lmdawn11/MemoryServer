package com.ming.server.config;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class SetNxConfig {
    private static final int SHARD_COUNT = 16; // 分片数量
    private final Map<String, String>[] setNxShards; // 存储 Key-Value（锁）
    private final ConcurrentHashMap<String, Long> ttlMap; // 存储 Key-TTL
    private static int INIT_CAPACITY = 1024;
    private static volatile SetNxConfig instance;


    /**
     * 构造方法
     */
    private SetNxConfig() {
        setNxShards = new ConcurrentHashMap[SHARD_COUNT];
        for (int i = 0; i < SHARD_COUNT; i++) {
            setNxShards[i] = new ConcurrentHashMap<>(INIT_CAPACITY);
        }
        this.ttlMap = new ConcurrentHashMap<>();
    }

    /**
     * 单例模式
     * @return
     */
    public static SetNxConfig getInstance() {
        if (instance == null) {
            synchronized (SetNxConfig.class) {
                if (instance == null) {
                    instance = new SetNxConfig();
                }
            }
        }
        return instance;
    }

    // 计算 key 所在的 shard
    private int getShardIndex(String key) {
        return (key.hashCode() & 0x7FFFFFFF) % SHARD_COUNT;
    }

    /**
     * **SETNX (SET if Not Exists) 命令**
     * - 只有当 `key` 不存在时，才能成功设置 `value`
     * - `ttlSeconds` 指定过期时间，自动删除
     * - 存储 `clientId`，防止误删
     */
    public String setnx(String key,String value, int ttlSeconds) {
        String clientId = UUID.randomUUID().toString(); // 生成唯一 Client ID
        int index = getShardIndex(key);

        String storedClientId = setNxShards[index].computeIfAbsent(key, k -> {
            ttlMap.put(k, System.currentTimeMillis() + ttlSeconds * 1000L);
            return clientId; // 存储 clientId
        });

        return storedClientId.equals(clientId) ? clientId : null; // 只有成功获取锁的客户端才返回 clientId
    }

    /**
     * **DELETE (仅允许 `clientId` 持有者删除)**
     */

    public boolean deleteNx(String key, String clientId) {
        int index = getShardIndex(key);
        return setNxShards[index].computeIfPresent(key, (k, v) -> {
            if (clientId.equals(v)) { // 只有持有者可以删除
                ttlMap.remove(k);
                return null;
            }
            return v;
        }) == null;
    }

    /**
     * 实现关门狗，延长key的时间  判断操作用户，
     */
    public boolean delayNX(String key, String clientId, int ttlSeconds) {
        int index = getShardIndex(key);
        return setNxShards[index].computeIfPresent(key,(k,value)->{
            if (clientId.equals(value)) {
                ttlMap.put(k, System.currentTimeMillis() + ttlSeconds * 1000L);
                return value; // 续期成功，返回原值
            }
            return value; // 续期失败，保持原值
        }) !=null; // 说明 `key` 仍然存在，续期成功
    }

}
