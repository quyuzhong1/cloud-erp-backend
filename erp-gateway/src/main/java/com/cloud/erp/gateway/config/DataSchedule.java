package com.cloud.erp.gateway.config;

import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.cloud.erp.gateway.config.CustomKeyResolverConfig.RateLimiterPathMap;
import com.common.business.enums.ServiceCodeNameEnum;
import com.common.core.constant.EnumMessage;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DataSchedule implements ApplicationListener<ContextRefreshedEvent> {

    @Value("${route.refresh.time:3}")
    private int routereFreshTime;

    @Resource
    private DynamicRouteService dynamicRouteService;
    
    private static final AtomicInteger systemOrder = new AtomicInteger(1000);
    private static final AtomicInteger matchOrder = new AtomicInteger(100000);
    private static final AtomicInteger pathOrder = new AtomicInteger(200000);
    private static final AtomicInteger refererOrder = new AtomicInteger(400000);
    private static final AtomicInteger hostMatchOrder = new AtomicInteger(800000);
    private static final AtomicInteger hostOrder = new AtomicInteger(1600000);
    
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        ScheduledExecutorService routeRefreshPool = Executors.newScheduledThreadPool(1);
        routeRefreshPool.scheduleAtFixedRate(()->{
            try {
                List<CustomKeyResolverConfig.RateLimiterPathMap> datas = this.getSysRouteConfig();
        		List<String> deleteList = datas.stream().filter(d -> d.getIsDeleted() != null && d.getIsDeleted()).map(RateLimiterPathMap::getId).collect(Collectors.toList());
        		datas = datas.stream().filter(d -> d.getIsDeleted() == null || !d.getIsDeleted()).collect(Collectors.toList());
                List<RouteDefinition> definitionList = new ArrayList<>();
                for (RateLimiterPathMap data : datas) {
                	ServiceCodeNameEnum serviceCodeNameEnum = data.getServiceCode();
                	if(serviceCodeNameEnum == null) {
                		continue;
                	}
                	String serviceCode = serviceCodeNameEnum.getCode();
					String rateLimiterPath = data.getRateLimiterPath();
					String host = data.getHost();
					String referer = data.getReferer();
					
					String id = serviceCode + "_route_" + data.getId();
                    RouteDefinition definition = new RouteDefinition();
                    definition.setId(id);
                    definition.setUri(new URI("lb://erp-" + serviceCode));
                    // 断言
                    List<PredicateDefinition> predicates = new ArrayList<>();
                    PredicateDefinition predicate = null;
                    
                    Integer order = 0;
                    if(rateLimiterPath != null && !"".equals(rateLimiterPath)) {
                    	if(!rateLimiterPath.startsWith("/")) {
    						rateLimiterPath = "/" + rateLimiterPath;
    					}
                    	predicate = new PredicateDefinition("Path=/api/"+ serviceCode + rateLimiterPath);
                        predicates.add(predicate);
                        if(rateLimiterPath.contains("*")) {
                        	order = order + matchOrder.incrementAndGet();
                        }else {
                        	order = order + pathOrder.incrementAndGet();
                        }
                    }
                    
                    if(referer != null && !"".equals(referer)) {
                    	predicate = new PredicateDefinition("Header=Referer, " + referer);
                        predicates.add(predicate);
                        order = order + refererOrder.incrementAndGet();
                    }
                    
                    if(host != null && !"".equals(host)) {
                    	if(!host.contains(":")) {
                    		host = host + "*";
                    	}
                        if(host.contains("*")) {
                        	order = order + hostMatchOrder.incrementAndGet();
                        }else {
                        	order = order + hostOrder.incrementAndGet();
                        }
                        predicate = new PredicateDefinition("Host="+ host);
                        predicates.add(predicate);
                    }
                    
                    if(order == 0) {
                    	order = systemOrder.incrementAndGet();
                    	predicate = new PredicateDefinition("Path=/api/"+ serviceCode + "/**");
                        predicates.add(predicate);
                    }
                    
                    definition.setPredicates(predicates);
                    // 过滤器
                    List<FilterDefinition> filters = new ArrayList<>();
                    FilterDefinition filter = new FilterDefinition("RewritePath=/api/" + serviceCode +"/(?<segment>.*),/$\\{segment}");
                    filters.add(filter);
                    filter = new FilterDefinition();
                    filter.setName("RequestRateLimiter");
                    Map<String, String> filterArgs = new HashMap<>();
                    filterArgs.put("key-resolver", id);
                    filterArgs.put("redis-rate-limiter.replenishRate", data.getRate().toString());
                    filterArgs.put("redis-rate-limiter.burstCapacity", data.getCount().toString());
                    filter.setArgs(filterArgs);
                    filters.add(filter);
                    definition.setFilters(filters);
                    definition.setOrder(order * -1);
                    definitionList.add(definition);
                }
                if(CollUtil.isNotEmpty(deleteList)) {
                	deleteList.forEach(d -> dynamicRouteService.deleteById(d));
                }
                if(CollUtil.isNotEmpty(definitionList)) {
                	dynamicRouteService.updateList(definitionList);
                }
            } catch (Exception e) {
                log.error("任务失败", e);
            }
        }, routereFreshTime, routereFreshTime, TimeUnit.SECONDS);
    }
    
    @Value("${spring.datasource.url:}")
    private String url;
    @Value("${spring.datasource.username:}")
    private String username;
    @Value("${spring.datasource.password:}")
    private String password;
    private static boolean isFirst = true;
    
    private DataSource dataSource;
    
    public List<CustomKeyResolverConfig.RateLimiterPathMap> getSysRouteConfig() {
    	List<CustomKeyResolverConfig.RateLimiterPathMap> sysRouteConfigList = new ArrayList<>();
    	String queryCondition = null;
    	try {
			if(isFirst) {
				int retryCount = 0;
				while(dataSource == null && retryCount < 3) {
					HikariConfig config = new HikariConfig();
				    config.setJdbcUrl(url);
				    config.setUsername(username);
				    config.setPassword(password);

				    // 设置其他连接池参数，例如最大连接数、最小空闲连接数等
				    config.setMaximumPoolSize(5);
				    config.setMinimumIdle(2);

				    dataSource = new HikariDataSource(config);
				    
				    if(dataSource == null) {
				    	try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
						}
				    	retryCount = retryCount + 1;
				    }
				}
			    
			    queryCondition = "is_deleted = 'f'";
			}else {
				queryCondition = "update_time >= '" + DateUtil.formatDateTime(DateUtil.offsetSecond(new Date(), -(routereFreshTime + 1))) + "'";
			}
		} finally {
			isFirst = false;
		}
    	
    	if(dataSource == null) {
    		return sysRouteConfigList;
    	}

        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            connection = dataSource.getConnection();

            String sql = "SELECT id,is_deleted,rate,count,rate_limiter_path,service_code,referer,host FROM sys_route_config where " + queryCondition;
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);

            CustomKeyResolverConfig.RateLimiterPathMap rateLimiterPathMap = null;
            while (resultSet.next()) {
            	rateLimiterPathMap = new RateLimiterPathMap();
            	rateLimiterPathMap.setId(String.valueOf(resultSet.getInt("id")));
            	rateLimiterPathMap.setIsDeleted(resultSet.getBoolean("is_deleted"));
            	rateLimiterPathMap.setRate(resultSet.getInt("rate"));
            	rateLimiterPathMap.setCount(resultSet.getInt("count"));
            	rateLimiterPathMap.setRateLimiterPath(resultSet.getString("rate_limiter_path"));
            	rateLimiterPathMap.setServiceCode(EnumMessage.getByCode(ServiceCodeNameEnum.class , resultSet.getString("service_code")));
            	rateLimiterPathMap.setReferer(resultSet.getString("referer"));
            	rateLimiterPathMap.setHost(resultSet.getString("host"));
            	
                sysRouteConfigList.add(rateLimiterPathMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return sysRouteConfigList;
    }
}
