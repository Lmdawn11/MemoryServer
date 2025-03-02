package com.ming.server.config.ttl;

import java.util.concurrent.ConcurrentHashMap;

public interface TtlManger {

    void startCleanupTask(ConcurrentHashMap<String, Long> ttlMap,int batchSize,double threshold);

    boolean delete(String key);

}
