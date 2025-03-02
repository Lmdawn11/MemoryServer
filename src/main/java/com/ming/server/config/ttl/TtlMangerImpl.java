package com.ming.server.config.ttl;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class  TtlMangerImpl implements TtlManger{


    @Override
    public void startCleanupTask(ConcurrentHashMap<String, Long> ttlMap,int batchSize,double threshold) {
        long now = System.currentTimeMillis();
        int expiredCount = 0;
        int totalKeys = ttlMap.size();

        if (totalKeys == 0) return;

        Iterator<Map.Entry<String, Long>> iterator = ttlMap.entrySet().iterator();
        while (iterator.hasNext()) {
            if (expiredCount >= batchSize) break;
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
        if (expiredRatio < threshold) {
            try {
                Thread.sleep(5000); // 过期 key 少，休眠 10 秒
            } catch (InterruptedException ignored) {}
        }
    }
}
