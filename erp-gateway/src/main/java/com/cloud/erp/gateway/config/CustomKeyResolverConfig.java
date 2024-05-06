package com.cloud.erp.gateway.config;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder.Builder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.convert.converter.Converter;
import org.springframework.web.server.ServerWebExchange;

import com.cloud.erp.gateway.config.GatewayConfigProperties.Routes;
import com.common.business.enums.ServiceCodeNameEnum;

import cn.hutool.core.collection.CollUtil;
import lombok.Data;
import reactor.core.publisher.Mono;

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
		private ServiceCodeNameEnum serviceCode;
		/**
		 * 被 限流来源
		 */
		private String referer;
		/**
		 *  被限流ip
		 */
		private String host;
//		private String[] rateLimiterPaths;
	}
    
//    @Resource
//	private GatewayConfigProperties gatewayConfigProperties;
//	
//	private static final List<RateLimiterPathMap> rateLimiterPathMapList = new ArrayList<>();
//	
//	static {
//		RateLimiterPathMap rateLimiterPathMap = new RateLimiterPathMap();
//		rateLimiterPathMap.setRate(1);
//		rateLimiterPathMap.setCount(5);
//		rateLimiterPathMap.setRateLimiterPaths(new String[] {"/api/tms/attachment/test"});
//		rateLimiterPathMapList.add(rateLimiterPathMap);
//		
//		rateLimiterPathMap = new RateLimiterPathMap();
//		rateLimiterPathMap.setRate(1);
//		rateLimiterPathMap.setCount(1);
//		rateLimiterPathMap.setRateLimiterPaths(new String[] {"/api/tms/attachment/test1"});
//		rateLimiterPathMapList.add(rateLimiterPathMap);
//	}
//    
//    @Bean
//    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) throws Exception {
//    	Builder routes = builder.routes();
//    	List<Routes> configRoutes = gatewayConfigProperties.getRoutes();
//    	for(RateLimiterPathMap rateLimiterPathMap : rateLimiterPathMapList) {
//    		String[] rateLimiterPaths = rateLimiterPathMap.getRateLimiterPaths();
//    		RedisRateLimiter redisRateLimiter = redisRateLimiter(rateLimiterPathMap.getRate() , rateLimiterPathMap.getCount());
//    		for(String rateLimiterPath : rateLimiterPaths) {
//    			Routes configRoute = null;
//        		for(Routes config : configRoutes) {
//        			List<String> predicates = config.getPredicates();
//        			if(CollUtil.isNotEmpty(predicates)) {
//        				if(predicates.stream().anyMatch(p -> rateLimiterPath.matches(p.split("=")[1].replace("**", ".*")))) {
//        					configRoute = config;
//        					break;
//        				}
//        			}
//        		}
//        		if(configRoute != null) {
//        			String[] filter = configRoute.getFilters().get(0).split("=")[1].split(",");
//        			String uri = configRoute.getUri();
//        			routes.route(configRoute.getId() + rateLimiterPath, r -> r.predicate(p -> p.getRequest().getPath().toString().equals(rateLimiterPath))
//                            .filters(f -> f.rewritePath(filter[0].trim(), filter[1].trim()).requestRateLimiter().configure(c -> c.setRateLimiter(redisRateLimiter))) 
//                            .uri(uri));
//        		}
//    		}
//    	}
//    	return routes.build();
//    }
//    
//    @Bean
//    @Scope("prototype")
//    public RedisRateLimiter redisRateLimiter(@Value("${rate:2147483647}") Integer rate, @Value("${count:2147483647}") Integer count) {
//        return new RedisRateLimiter(rate , count);
//    }
    
}
