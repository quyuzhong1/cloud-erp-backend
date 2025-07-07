package com.erp.server.scm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
public class ScmExecutorPoolConfig {
    @Bean(name = "contractInfoExecutorPool")
    public ExecutorService contractInfoExecutorPool() {
        ThreadPoolExecutor service = new ThreadPoolExecutor(10, 30,
                5L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100));
        //设置线城池的饱和策略
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
        service.setRejectedExecutionHandler(handler);
        return service;
    }
}
