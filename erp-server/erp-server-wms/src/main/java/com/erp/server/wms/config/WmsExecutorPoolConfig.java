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
    @Bean(name = "b2bVirtualFlowRefactorPool")
    public ExecutorService b2bVirtualFlowRefactorPool() {
        ThreadPoolExecutor service = new ThreadPoolExecutor(20, 100,
                30L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000));
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
    @Bean(name = "b2cVirtualFlowRefactorPool")
    public ExecutorService b2cVirtualFlowRefactorPool() {
        ThreadPoolExecutor service = new ThreadPoolExecutor(20, 100,
                30L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000));
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
    @Bean(name = "firstMileVirtualFlowRefactorPool")
    public ExecutorService firstMileVirtualFlowRefactorPool() {
        ThreadPoolExecutor service = new ThreadPoolExecutor(20, 100,
                30L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000));
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
    @Bean(name = "allocationVirtualFlowRefactorPool")
    public ExecutorService allocationVirtualFlowRefactorPool() {
        ThreadPoolExecutor service = new ThreadPoolExecutor(20, 100,
                30L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000));
        //设置线城池的饱和策略
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
        service.setRejectedExecutionHandler(handler);

        return service;
    }

    /**
     * 其他出入口单状态变更时间监听线程池
     * @author wuhaotian
     * @date 2025/8/22 09:59
     * @return ExecutorService
     */
    @Bean(name = "outboundOrderDetailChangeEventPool")
    public ExecutorService outboundOrderDetailChangeEventPool() {
        ThreadPoolExecutor service = new ThreadPoolExecutor(1, 4,
                30L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000));
        //设置线城池的饱和策略
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
        service.setRejectedExecutionHandler(handler);
        return service;
    }
    
    
    @Bean(name = "transactionIdToInventoryHisPool")
    public ExecutorService transactionIdToInventoryHisPool() {
        ThreadPoolExecutor service = new ThreadPoolExecutor(20, 50,
                30L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000));
        //设置线城池的饱和策略
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
        service.setRejectedExecutionHandler(handler);
        return service;
    }
}
