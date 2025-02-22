package com.ming.server.config;

import io.netty.util.HashedWheelTimer;

import java.util.concurrent.TimeUnit;

public class TimeWheelConfig {
    private final HashedWheelTimer TIMER; //时间轮
    private final int TICKDURATION = 30;  //定时任务多久执行一次
    private final int TICKSPERWHEEL = 10; //时间槽数

    private static volatile TimeWheelConfig timeWheelConfig;

    private  TimeWheelConfig() {
        this.TIMER = new HashedWheelTimer(TICKDURATION, TimeUnit.SECONDS, TICKSPERWHEEL); //每分钟一个槽，一共10个槽
    }

    public static  TimeWheelConfig getTimeWheelConfig() {
        if(timeWheelConfig == null){
            synchronized (TimeWheelConfig.class){
                if(timeWheelConfig == null){
                    timeWheelConfig = new TimeWheelConfig();
                }
            }
        }
        return timeWheelConfig;
    }

    // **提供 getTimer() 方法**
    public HashedWheelTimer getTimer() {
        return TIMER;
    }
}
