package com.erp.server.dmp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class SpringAsyncConfig {

	@Value("${openApi.download.pool.corePoolSize}")
	private Integer corePoolSize;


	@Value("${openApi.download.pool.maxPoolSize}")
	private Integer maxPoolSize;

	@Value("${openApi.download.pool.queueCapacity}")
	private Integer queueCapacity;

	@Value("${openApi.download.pool.keepAliveSeconds}")
	private Integer keepAliveSeconds;
	@Value("${openApi.download.pool.poolName}")
	private String poolName;

//	@Bean("mabang")
//	public Executor asyncServiceMabangExecutor() {
//		return createExecutor("mabang.download");
//	}

	@Bean("pullErpOpenApi")
	public Executor asyncServiceErpExecutor() {
		return createExecutor();
	}

//	@Bean("kingdee")
//	public Executor asyncServiceKingdeeExecutor() {
//		return createExecutor("kingdee.download");
//	}

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
		// 设置默认线程名称
		executor.setThreadNamePrefix(this.poolName+"-");
		// 等待所有任务结束后再关闭线程池
		executor.setWaitForTasksToCompleteOnShutdown(true);
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		//执行初始化
		executor.initialize();
		return executor;
	}
}
