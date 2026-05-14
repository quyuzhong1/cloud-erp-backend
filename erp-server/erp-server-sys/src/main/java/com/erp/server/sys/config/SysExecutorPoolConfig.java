package com.erp.server.sys.config;

import com.common.business.config.TraceableExecutorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.*;

@Configuration
@EnableAsync
public class SysExecutorPoolConfig {

    @Bean("thirdNoticePushExecutor")
    public ExecutorService noticePushExecutor() {
        ThreadPoolExecutor service = new ThreadPoolExecutor(10, 20,
                5L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(100));
        //设置线城池的饱和策略
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
        service.setRejectedExecutionHandler(handler);
        return new TraceableExecutorService(service);
    }

    @Bean("noticeHeartbeatExecutor")
    public ExecutorService noticeHeartbeatExecutor() {
        ThreadPoolExecutor service = new ThreadPoolExecutor(4, 8,
                30L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(200));
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
        service.setRejectedExecutionHandler(handler);
        return new TraceableExecutorService(service);
    }
}
