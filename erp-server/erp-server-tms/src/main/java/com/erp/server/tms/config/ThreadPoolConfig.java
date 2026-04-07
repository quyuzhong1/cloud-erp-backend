package com.erp.server.tms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;

/**
 * @Classname ThreadPoolConfig

 * @Date 2022-11-16 14:33
 * @Created by yl
 */

@Configuration
public class ThreadPoolConfig {
    @Bean("tmsExecutor")
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

    @Bean("tmsTransferChannelExecutor")
    public ThreadPoolTaskExecutor threadPoolTransferChannelExecutor() {
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
    
    @Bean(name = "costAllocationPool")
    public ExecutorService costAllocationPool() {
        ThreadPoolExecutor service = new ThreadPoolExecutor(1, 10,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(Integer.MAX_VALUE));
        //设置线城池的饱和策略
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
        service.setRejectedExecutionHandler(handler);
        return service;
    }
    @Bean(name = "tmsLogisticsLabelPool")
    public ExecutorService tmsLogisticsLabelPool() {
        return new ThreadPoolExecutor(
                10,
                20,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(200),
                r -> {
                    Thread t = new Thread(r);
                    t.setName("tms-label-" + t.getId());
                    return t;
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    /**
     * 导入历史记录线程池
     * @author will
     * @date 2025/10/10 14:59
     * @return ExecutorService
     */
    @Bean(name = "importHistoryRecordPool")
    public ExecutorService importHistoryRecordPool() {
        ThreadPoolExecutor service = new ThreadPoolExecutor(50, 100,
                30L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(10000));
        //设置线城池的饱和策略
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
        service.setRejectedExecutionHandler(handler);
        return service;
    }
}
