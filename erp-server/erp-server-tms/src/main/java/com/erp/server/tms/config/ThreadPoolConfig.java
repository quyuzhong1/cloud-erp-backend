package com.erp.server.tms.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.common.business.config.TraceableExecutorService;
import com.common.business.config.TraceableThreadPoolTaskExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

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
        TraceableThreadPoolTaskExecutor executor = new TraceableThreadPoolTaskExecutor();
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
        ThreadPoolExecutor service = new ThreadPoolExecutor(50, 100,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(Integer.MAX_VALUE));
        //设置线城池的饱和策略
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
        service.setRejectedExecutionHandler(handler);
        return new TraceableExecutorService(service);
    }

    @Bean(name = "tmsLogisticsLabelPool")
    public ExecutorService tmsLogisticsLabelPool() {
        // 1. 先创建原始的 ThreadPoolExecutor
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
        // 2. 用 TraceableExecutorService 包装（自动传递 TraceId）
        return new TraceableExecutorService(executor);
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

    /**
     * 物流商对账合并匹配 / 手动匹配异步线程池（本模块自管，不走通用异步任务）
     * @author Will
     * @date 2026/6/12
     * @return ExecutorService
     */
    @Bean(name = "logisticsReconMatchPool")
    public ExecutorService logisticsReconMatchPool() {
        ThreadPoolExecutor service = new ThreadPoolExecutor(16, 32,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(20000));
        // 池满时快速失败，由业务层 markReconMatchFailed 回写状态，避免 CallerRunsPolicy 阻塞 HTTP 线程
        service.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        return new TraceableExecutorService(service);
    }

    /**
     * 对账匹配识别组内存计算线程池（与 Excel 导入池隔离）。
     * <p>使用 AbortPolicy：池满时快速失败并由业务回写，避免 CallerRunsPolicy 在持主单锁的 MQ 线程上反压拉长锁时间。</p>
     */
    @Bean(name = "logisticsReconMatchComputePool")
    public ExecutorService logisticsReconMatchComputePool() {
        ThreadPoolExecutor service = new ThreadPoolExecutor(16, 32,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(5000),
                r -> {
                    Thread t = new Thread(r);
                    t.setName("recon-match-compute-" + t.getId());
                    return t;
                },
                new ThreadPoolExecutor.AbortPolicy());
        return new TraceableExecutorService(service);
    }

    @Bean(name = "tmsLogisticsOrderPool")
    public ExecutorService tmsLogisticsOrderPool() {
        // 1. 先创建原始的 ThreadPoolExecutor
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
        // 2. 用 TraceableExecutorService 包装（自动传递 TraceId）
        return new TraceableExecutorService(executor);
    }
}
