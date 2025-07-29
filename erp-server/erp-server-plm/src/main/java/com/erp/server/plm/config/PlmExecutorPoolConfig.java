package com.erp.server.plm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
public class PlmExecutorPoolConfig {

    @Bean(name = "zipImageExecutorPool")
    public ExecutorService zipImageExecutorPool() {
        ThreadPoolExecutor service = new ThreadPoolExecutor(5, 10,
                5L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(100));
        //设置线城池的饱和策略
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
        service.setRejectedExecutionHandler(handler);
        return service;
    }
}
