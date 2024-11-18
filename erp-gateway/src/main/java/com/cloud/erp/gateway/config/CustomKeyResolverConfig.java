package com.cloud.erp.gateway.config;

import lombok.Data;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

@Configuration
public class CustomKeyResolverConfig implements Converter<String, KeyResolver>{

    
    @Override
    public KeyResolver convert(String source) {
        CustomKeyResolver customKeyResolver = new CustomKeyResolver();
        customKeyResolver.setId(source);
		return customKeyResolver;
    }
    
    @Data
	static class RateLimiterPathMap{
    	/**
    	 * 路由id
    	 */
    	private String id;
    	/**
    	 * 删除标识，删除时移除路由
    	 */
    	private Boolean isDeleted;
		/**
		 * 令牌桶速率
		 */
		private Integer rate;
		/**
		 * 令牌桶总数
		 */
		private Integer count;
		/**
		 * 限流接口
		 */
		private String rateLimiterPath;
		/**
		 * 限流系统
		 */
		private String serviceCode;
		/**
		 * 被 限流来源
		 */
		private String referer;
		/**
		 *  被限流ip
		 */
		private String remoteAddr;
	}
    
}
