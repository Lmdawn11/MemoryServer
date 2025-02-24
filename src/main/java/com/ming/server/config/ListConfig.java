package com.ming.server.config;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ListConfig {
    private final Map<String, LinkedList<String>> listMap = new ConcurrentHashMap<>();
    private volatile ListConfig instance;

    /**
     * 左插入
     * @param key
     * @param values
     * @return
     */
    public Boolean lpush(String key,String... values){
        try {
            listMap.compute(key, (k, v) -> {
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
    public Boolean rpush(String key,String... values){
        try {
            listMap.compute(key,(k,v)->{
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
}
