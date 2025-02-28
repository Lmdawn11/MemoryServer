package com.ming.server.config;

import com.ming.server.ioc.Bean;
import io.netty.util.HashedWheelTimer;

import java.util.concurrent.TimeUnit;

@Bean
public class TimeWheelConfig {
    private final HashedWheelTimer TIMER; //时间轮
    private final int TICKDURATION = 30;  //定时任务多久执行一次
    private final int TICKSPERWHEEL = 10; //时间槽数

    public TimeWheelConfig() {
        this.TIMER = new HashedWheelTimer(TICKDURATION, TimeUnit.SECONDS, TICKSPERWHEEL); //每分钟一个槽，一共10个槽
    }


    // **提供 getTimer() 方法**
    public HashedWheelTimer getTimer() {
        return TIMER;
    }
}
