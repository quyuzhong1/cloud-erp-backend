package com.erp.server.sys.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.*;

@Configuration
@EnableAsync
public class SysExecutorPoolConfig {

    @Bean("thirdNoticePushExecutor")
    public Executor noticePushExecutor() {
        ThreadPoolExecutor service = new ThreadPoolExecutor(10, 20,
                5L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(100));
        //设置线城池的饱和策略
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
        service.setRejectedExecutionHandler(handler);
        return service;
    }
}
