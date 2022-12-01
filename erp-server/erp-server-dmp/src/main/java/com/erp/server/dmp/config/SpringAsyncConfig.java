package com.erp.server.dmp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.ObjectUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class SpringAsyncConfig {
	@Autowired
	private ConfigurableEnvironment env;

	@Bean("mabang")
	public Executor asyncServiceMabangExecutor() {
		return createExecutor("mabang.download");
	}

	@Bean("gyy")
	public Executor asyncServiceGyyExecutor() {
		return createExecutor("gyy.download");
	}

	@Bean("kingdee")
	public Executor asyncServiceKingdeeExecutor() {
		return createExecutor("kingdee.download");
	}

	private ThreadPoolTaskExecutor createExecutor(String str) {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		// 设置核心线程数
		executor.setCorePoolSize(getCorePoolSize(str));
		// 设置最大线程数
		executor.setMaxPoolSize(getMaxPoolSize(str));
		//配置队列大小
		executor.setQueueCapacity(getQueueCapacity(str));
		// 设置线程活跃时间（秒）
		executor.setKeepAliveSeconds(getKeepAliveSeconds(str));
		// 设置默认线程名称
		executor.setThreadNamePrefix(getPoolName(str)+"-");
		// 等待所有任务结束后再关闭线程池
		executor.setWaitForTasksToCompleteOnShutdown(true);
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		//执行初始化
		executor.initialize();
		return executor;
	}

	public Integer getCorePoolSize(String dbName) {
		return getPropertyAsInt(env,"config.poolConfig.pool."+dbName+".corePoolSize",5);
	}

	public Integer getMaxPoolSize(String dbName) {
		return getPropertyAsInt(env,"config.poolConfig.pool."+dbName+".maxPoolSize",10);
	}

	public Integer getQueueCapacity(String dbName) {
		return getPropertyAsInt(env,"config.poolConfig.pool."+dbName+".queueCapacity",1);
	}

	public Integer getKeepAliveSeconds(String dbName) {
		return getPropertyAsInt(env,"config.poolConfig.pool."+dbName+".keepAliveSeconds",1);
	}

	public String getPoolName(String dbName) {
		return getPropertyAsString(env,"config.poolConfig.pool."+dbName+".poolName","默认线程");
	}

	private int getPropertyAsInt(ConfigurableEnvironment env, String key, int defaultVal) {
		try {
			return Integer.parseInt(env.getProperty(key));
		} catch (Exception e) {
			return defaultVal;
		}
	}

	private String getPropertyAsString(ConfigurableEnvironment env, String key, String defaultVal) {
		if(ObjectUtils.isEmpty(env.getProperty(key))) {
			return defaultVal;
		}else {
			return env.getProperty(key);
		}
	}
}
