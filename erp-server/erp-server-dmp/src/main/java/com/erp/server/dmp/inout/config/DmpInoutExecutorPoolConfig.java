package com.erp.server.dmp.inout.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
public class DmpInoutExecutorPoolConfig {
    @Bean(name = "dmpInputExecutorPool")
    public ExecutorService wmsDataCompareExecutorPool() {
        ThreadPoolExecutor service = new ThreadPoolExecutor(10, 30,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(10000));
        //设置线城池的饱和策略
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
        service.setRejectedExecutionHandler(handler);
        return service;
    }
    
}
