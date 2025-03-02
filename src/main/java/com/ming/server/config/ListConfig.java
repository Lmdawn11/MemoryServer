package com.ming.server.config;

import com.ming.server.config.ttl.TtlMangerImpl;
import com.ming.server.ioc.Bean;
import com.ming.server.ioc.SimpleIOC;
import io.netty.util.HashedWheelTimer;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Bean
public class ListConfig extends TtlMangerImpl {
    private final Map<String, LinkedList<String>> listMap;
    private final ConcurrentHashMap<String, Long> ttlMap; // 存储 Key-TTL
    private static final int CLEAN_BATCH_SIZE = 100;  // 每次最多清理 100 个 key
    private static final double THRESHOLD_PERCENT = 0.1; // 10% 过期 key 触发暂停

    public ListConfig() {
        ttlMap = new ConcurrentHashMap<>();
        listMap = new ConcurrentHashMap<>();
    }

    /**
     * 左插入
     * @param key
     * @param values
     * @return
     */
    public Boolean lpush(String key,int ttlSeconds,String... values){
        try {
            listMap.compute(key, (k, v) -> {
                if (ttlSeconds>0){
                    ttlMap.put(key,System.currentTimeMillis()+ttlSeconds + ttlSeconds*1000L);
                }
                if (v == null) {
                    v = new LinkedList<>();
                }
                for (int i = values.length - 1; i >= 0; i--) {  //倒序插入 , abc 插入顺序cba
                    v.addFirst(values[i]);
                }
                return v;
            });
            return true;
        } catch (Exception e) {
            log.error("lpush error:{}",e.getMessage());
            return false;
        }
    }

    /**
     * 右插入
     * @param key
     * @param values
     * @return
     */
    public Boolean rpush(String key,int ttlSeconds,String... values){
        try {
            listMap.compute(key,(k,v)->{
                if (ttlSeconds>0){
                    ttlMap.put(key,System.currentTimeMillis()+ttlSeconds + ttlSeconds*1000L);
                }
                if (v == null) {
                    v = new LinkedList<>();
                }
                for (String value : values) {
                    v.addLast(value);
                }
                return v;
            });
            return true;
        } catch (Exception e) {
            log.error("rpush error:{}",e.getMessage());
            return false;
        }
    }

    /**
     * 左出
     * @param key
     * @return
     */
    public String lpop(String key){
        return listMap.computeIfPresent(key,(k,v)->
            v.isEmpty()? null : v
        ).pollFirst();
    }

    /**
     *     右出
     */
    public String rpop(String key){
        return listMap.computeIfPresent(key,(k,v)->
                v.isEmpty()? null : v).pollLast();
    }

    /**
     * 范围查询
     * @param key
     * @param start
     * @param end
     * @return
     */
    public List<String> lrange(String key,int start,int end){
        LinkedList<String> list = listMap.get(key);
        if (list == null) {
            return new LinkedList<>();
        }
        synchronized (list){
            return list.subList(start, end);
        }
    }
    /**
     * 开启定时任务
     */
    public void startTimerTask(){
        TimeWheelConfig timeWheelConfig = SimpleIOC.getBean(TimeWheelConfig.class);
        HashedWheelTimer timer = timeWheelConfig.getTimer();
        timer.newTimeout(timeout -> {
            startCleanupTask(ttlMap,CLEAN_BATCH_SIZE,THRESHOLD_PERCENT);},
                1,TimeUnit.MINUTES);
    }

    @Override
    public boolean delete(String key) {
        return listMap.compute(key,(k,oldvalue)->{
            ttlMap.remove(key);
            return null;
        }) == null;

    }
}
