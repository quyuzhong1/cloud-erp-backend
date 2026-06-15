package com.erp.server.tms.config;

import com.common.business.config.TraceableExecutorService;
import com.common.business.config.TraceableThreadPoolTaskExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Classname ThreadPoolConfig
 * @Date 2022-11-16 14:33
 * @Created by yl
 */
@Configuration
public class ThreadPoolConfig {
    @Bean("tmsExecutor")
    public ThreadPoolTaskExecutor threadPoolExecutor() {
        TraceableThreadPoolTaskExecutor executor = new TraceableThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(Integer.MAX_VALUE);
        executor.setKeepAliveSeconds(60);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }

    @Bean("tmsTransferChannelExecutor")
    public ThreadPoolTaskExecutor threadPoolTransferChannelExecutor() {
        TraceableThreadPoolTaskExecutor executor = new TraceableThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(Integer.MAX_VALUE);
        executor.setKeepAliveSeconds(60);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }

    @Bean(name = "costAllocationPool")
    public ExecutorService costAllocationPool() {
        ThreadPoolExecutor service = new ThreadPoolExecutor(50, 100,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(Integer.MAX_VALUE));
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
        service.setRejectedExecutionHandler(handler);
        return new TraceableExecutorService(service);
    }

    @Bean(name = "tmsLogisticsLabelPool")
    public ExecutorService tmsLogisticsLabelPool() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
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
        return new TraceableExecutorService(executor);
    }

    /**
     * 导入历史记录线程池
     */
    @Bean(name = "importHistoryRecordPool")
    public ExecutorService importHistoryRecordPool() {
        ThreadPoolExecutor service = new ThreadPoolExecutor(50, 100,
                30L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(10000));
        service.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return new TraceableExecutorService(service);
    }

    /**
     * 物流商对账合并匹配 / 手动匹配异步线程池（本模块自管，不走通用异步任务）
     */
    @Bean(name = "logisticsReconMatchPool")
    public ExecutorService logisticsReconMatchPool() {
        ThreadPoolExecutor service = new ThreadPoolExecutor(16, 32,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(20000));
        service.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        return new TraceableExecutorService(service);
    }

    @Bean(name = "tmsLogisticsOrderPool")
    public ExecutorService tmsLogisticsOrderPool() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
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
        return new TraceableExecutorService(executor);
    }
}
