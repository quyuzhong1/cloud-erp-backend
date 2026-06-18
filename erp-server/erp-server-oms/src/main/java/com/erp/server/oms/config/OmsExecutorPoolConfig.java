package com.erp.server.oms.config;

import com.common.business.config.TraceableExecutorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
public class OmsExecutorPoolConfig {
    @Bean(name = "soB2cTabExecutorPool")
    public ExecutorService soB2cTabExecutorPool() {
        ThreadPoolExecutor service = new ThreadPoolExecutor(10, 30,
                5L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100));
        //设置线城池的饱和策略
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
        service.setRejectedExecutionHandler(handler);

        return new TraceableExecutorService(service);
    }

    /**
     * 平台订单MQ消费后的延迟任务线程池（自动提交发货、获取物流单号等）
     */
    @Bean(name = "platformOrderDeferredExecutor")
    public ExecutorService platformOrderDeferredExecutor() {
        ThreadPoolExecutor service = new ThreadPoolExecutor(4, 16,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(200));
        service.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        return new TraceableExecutorService(service);
    }
    
}
