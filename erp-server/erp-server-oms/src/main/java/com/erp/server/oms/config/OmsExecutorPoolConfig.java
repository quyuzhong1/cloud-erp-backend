package com.erp.server.oms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
public class OmsExecutorPoolConfig {
    @Bean(name = "soB2cTabExecutorPool")
    public ExecutorService soB2cTabExecutorPool() {
        ThreadPoolExecutor service = new ThreadPoolExecutor(10, 30,
                5L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(100));
        //设置线城池的饱和策略
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
        service.setRejectedExecutionHandler(handler);

        return service;
    }
    
}
