package com.erp.server.plm.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Classname ThreadPoolConfig
 * @Description TODO
 * @Date 2022-11-16 14:33
 * @Created by yl
 */

@Configuration
public class ThreadPoolConfig {


    @Bean("customExecutor")
    public ThreadPoolTaskExecutor  threadPoolExecutor() {
        ThreadPoolTaskExecutor  executor = new ThreadPoolTaskExecutor();
        // 设置核心线程数
        executor.setCorePoolSize(8);
        // 设置最大线程数
        executor.setMaxPoolSize(20);
        // 设置队列大小
        executor.setQueueCapacity(Integer.MAX_VALUE);
        // 设置线程活跃时间(秒)
        executor.setKeepAliveSeconds(60);

        // 所有任务结束后关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 初始化
        executor.initialize();
        return executor;
    }
}
