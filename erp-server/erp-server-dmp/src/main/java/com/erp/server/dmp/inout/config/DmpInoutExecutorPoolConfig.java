package com.erp.server.dmp.inout.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
public class DmpInoutExecutorPoolConfig {
    @Bean(name = "dmpInputExecutorPool")
    public ExecutorService dmpInputExecutorPool() {
        ThreadPoolExecutor service = new ThreadPoolExecutor(20, 50,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(10000));
        //设置线城池的饱和策略
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
        service.setRejectedExecutionHandler(handler);
        return service;
    }
    
    @Bean(name = "dmpDoOutputErrorTask")
    public ExecutorService dmpDoOutputErrorTask() {
    	ThreadPoolExecutor service = new ThreadPoolExecutor(5, 10,
    			60L, TimeUnit.SECONDS,
    			new LinkedBlockingQueue<Runnable>(10000));
    	//设置线城池的饱和策略
    	RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
    	service.setRejectedExecutionHandler(handler);
    	return service;
    }
    
    @Bean(name = "dmpOutputExecutorPool")
    public ExecutorService dmpOutputExecutorPool() {
        ThreadPoolExecutor service = new ThreadPoolExecutor(200, 400,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(100000));
        //设置线城池的饱和策略
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
        service.setRejectedExecutionHandler(handler);
        return service;
    }
    
    @Bean(name = "dmpTabListExecutorPool")
    public ExecutorService dmpTabListExecutorPool() {
    	ThreadPoolExecutor service = new ThreadPoolExecutor(4, 12,
    			0L, TimeUnit.SECONDS,
    			new LinkedBlockingQueue<Runnable>(10000));
    	//设置线城池的饱和策略
    	RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
    	service.setRejectedExecutionHandler(handler);
    	return service;
    }
    
    @Bean(name = "dmpListTimeExecutorPool")
    public ExecutorService dmpListTimeExecutorPool() {
    	ThreadPoolExecutor service = new ThreadPoolExecutor(10, 30,
    			0L, TimeUnit.SECONDS,
    			new LinkedBlockingQueue<Runnable>(10000));
    	//设置线城池的饱和策略
    	RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
    	service.setRejectedExecutionHandler(handler);
    	return service;
    }
    
    @Bean(name = "dmpSdyOutputExecutorPool")
    public ExecutorService dmpSdyOutputExecutorPool() {
        ThreadPoolExecutor service = new ThreadPoolExecutor(10, 50,
                10L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(10000));
        //设置线城池的饱和策略
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
        service.setRejectedExecutionHandler(handler);
        return service;
    }
    
    @Bean(name = "dmpSdyOutputPushExecutorPool")
    public ExecutorService dmpSdyOutputPushExecutorPool() {
    	ThreadPoolExecutor service = new ThreadPoolExecutor(20, 20,
    			10L, TimeUnit.SECONDS,
    			new LinkedBlockingQueue<Runnable>(10000));
    	//设置线城池的饱和策略
    	RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
    	service.setRejectedExecutionHandler(handler);
    	return service;
    }
    
    @Bean(name = "dmpInputDbNextPageFinishPool")
    public ExecutorService dmpInputDbNextPageFinishPool() {
        ThreadPoolExecutor service = new ThreadPoolExecutor(10, 30,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(10000));
        //设置线城池的饱和策略
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
        service.setRejectedExecutionHandler(handler);
        return service;
    }
}
