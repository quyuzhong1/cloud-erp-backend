package com.erp.server.wms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
public class WmsExecutorPoolConfig {
    @Bean(name = "wmsDataCompareExecutorPool")
    public ExecutorService wmsDataCompareExecutorPool() {
        ThreadPoolExecutor service = new ThreadPoolExecutor(5, 10,
                5L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(100));
        //设置线城池的饱和策略
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
        service.setRejectedExecutionHandler(handler);

        return service;
    }
    @Bean(name = "thirdWarehouseExecutorPool")
    public ExecutorService thirdWarehouseExecutorPool() {
        ThreadPoolExecutor service = new ThreadPoolExecutor(5, 10,
                5L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100));
        //设置线城池的饱和策略
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
        service.setRejectedExecutionHandler(handler);

        return service;
    }
    /**
     * 流水生成线程池
     * @author will
     * @date 2025/4/8 09:59
     * @return ExecutorService
     */
    @Bean(name = "virtualFlowRefactorPool")
    public ExecutorService virtualFlowRefactorPool() {
        ThreadPoolExecutor service = new ThreadPoolExecutor(10, 30,
                5L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100));
        //设置线城池的饱和策略
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
        service.setRejectedExecutionHandler(handler);

        return service;
    }
}
