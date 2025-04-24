package com.erp.server.oms.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static cn.hutool.core.thread.ThreadUtil.createThreadFactory;

@Configuration
@EnableAsync
@Slf4j
public class SpringAsyncConfig {

	@Value("${openApi.download.pool.corePoolSize}")
	private Integer corePoolSize = 2;


	@Value("${openApi.download.pool.maxPoolSize}")
	private Integer maxPoolSize = 3;

	@Value("${openApi.download.pool.queueCapacity}")
	private Integer queueCapacity= 5;

	@Value("${openApi.download.pool.keepAliveSeconds}")
	private Integer keepAliveSeconds = 10;
	@Value("${openApi.download.pool.poolName}")
	private String poolName = "";

	@Bean("omsErpExecutor")
	public ThreadPoolTaskExecutor asyncServiceErpExecutor() {
		ThreadPoolTaskExecutor executor = createExecutor();
		printThreadPoolStatus(executor);
		return executor;
	}

	private ThreadPoolTaskExecutor createExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		// 设置核心线程数
		executor.setCorePoolSize(this.corePoolSize);
		// 设置最大线程数
		executor.setMaxPoolSize(this.maxPoolSize);
		//配置队列大小
		executor.setQueueCapacity(this.queueCapacity);
		// 设置线程活跃时间（秒）
		executor.setKeepAliveSeconds(this.keepAliveSeconds);
		// 设置线程名称
		executor.setThreadNamePrefix(this.poolName+"-");
		// 等待所有任务结束后再关闭线程池
		executor.setWaitForTasksToCompleteOnShutdown(true);
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
		//执行初始化
		executor.initialize();
		return executor;
	}

	/**
	 * 打印线程池的状态
	 *
	 * @param threadPool 线程池对象
	 */
	public static void printThreadPoolStatus(ThreadPoolTaskExecutor threadPool) {
		ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1, createThreadFactory("print-images/thread-pool-status"));
		scheduledExecutorService.scheduleAtFixedRate(() -> {
			log.info("=========================");
			log.info("ThreadPool Size: [{}]", threadPool.getPoolSize());
			log.info("Active Threads: {}", threadPool.getActiveCount());
			log.info("Number of Tasks : {}", threadPool.getThreadPoolExecutor().getTaskCount());
			log.info("Number of Tasks in Queue: {}", threadPool.getThreadPoolExecutor().getQueue().size());
			log.info("=========================");
		}, 0, 300, TimeUnit.SECONDS);
	}

}
