package com.cloud.erp.gateway.config;

import java.net.URI;
import java.net.URISyntaxException;
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
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.stereotype.Component;

import com.cloud.erp.gateway.config.CustomKeyResolverConfig.RateLimiterPathMap;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RouteDataSchedule implements ApplicationRunner {

	public static final String SUB_STRING = "/";

	@Value("${route.refresh.time:3}")
    private int routereFreshTime;

    @Resource
    private DynamicRouteService dynamicRouteService;
    
    /*-----------------------------路由优先级开始--------------------------------*/
    
    private static final AtomicInteger sysPathOrder = new AtomicInteger(10000);
    private static final AtomicInteger pathMatchOrder = new AtomicInteger(100000);
    private static final AtomicInteger pathOrder = new AtomicInteger(200000);
    private static final AtomicInteger refererOrder = new AtomicInteger(400000);
    private static final AtomicInteger hostMatchOrder = new AtomicInteger(800000);
    private static final AtomicInteger hostOrder = new AtomicInteger(1600000);
    
    /*-----------------------------路由优先级结束--------------------------------*/
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        ScheduledExecutorService routeRefreshPool = Executors.newScheduledThreadPool(1);
        routeRefreshPool.scheduleAtFixedRate(this::refreshRoutes, routereFreshTime, routereFreshTime, TimeUnit.SECONDS);
    }

	private void refreshRoutes() {
		try {
			List<RateLimiterPathMap> datas = this.getSysRouteConfig();

			List<String> deleteList = filterDeletedRoutes(datas);

			datas = datas.stream().filter(d -> null == d.getIsDeleted() || !d.getIsDeleted()).collect(Collectors.toList());
			List<RouteDefinition> definitionList = new ArrayList<>();
			for (RateLimiterPathMap data : datas) {
				try {
					RouteDefinition definition = createRouteDefinition(data);
					if (definition == null) continue;
					definitionList.add(definition);
				} catch (Exception e) {
					log.error("动态路由id={}创建失败" , data.getId() , e);
				}
			}
			if(CollUtil.isNotEmpty(deleteList)) {
				deleteList.forEach(d -> dynamicRouteService.deleteById(d));
			}
			if(CollUtil.isNotEmpty(definitionList)) {
				dynamicRouteService.updateList(definitionList);
			}
		} catch (Exception e) {
			log.error("动态路由任务失败", e);
		}
	}

	private static RouteDefinition createRouteDefinition(RateLimiterPathMap data) throws URISyntaxException {
		String serviceCode = data.getServiceCode();
		if(serviceCode == null || serviceCode.isEmpty()) {
			return null;
		}
		String rateLimiterPath = data.getRateLimiterPath();
		String remoteAddr = data.getRemoteAddr();
		String referer = data.getReferer();

		String id = data.getId();
		RouteDefinition definition = new RouteDefinition();
		definition.setId(id);
		definition.setUri(new URI("lb://erp-" + serviceCode));
		int order = 0;
		// 断言
		List<PredicateDefinition> predicates = new ArrayList<>();
		PredicateDefinition predicate;

		String sysPath = "/api/"+ serviceCode + "/**";
		if(rateLimiterPath == null || rateLimiterPath.isEmpty()) {
			rateLimiterPath = sysPath;
		}
		if(!rateLimiterPath.startsWith(SUB_STRING)) {
			rateLimiterPath = SUB_STRING + rateLimiterPath;
		}

		predicate = new PredicateDefinition("Path="+ rateLimiterPath);
		predicates.add(predicate);

		if(sysPath.equals(rateLimiterPath)) {
			order = order + sysPathOrder.incrementAndGet();
		}else if(rateLimiterPath.contains("*")) {
			order = order + pathMatchOrder.incrementAndGet();
		}else {
			order = order + pathOrder.incrementAndGet();
		}

		if(referer != null && !referer.isEmpty()) {
			predicate = new PredicateDefinition("Header=Referer, " + referer);
			predicates.add(predicate);
			order = order + refererOrder.incrementAndGet();
		}

		if(remoteAddr != null && !remoteAddr.isEmpty()) {
			if(remoteAddr.contains(SUB_STRING)) {
				order = order + hostMatchOrder.incrementAndGet();
			}else {
				remoteAddr = remoteAddr + "/32";
				order = order + hostOrder.incrementAndGet();
			}

			predicate = new PredicateDefinition("RemoteAddr="+ remoteAddr);
			predicates.add(predicate);
		}

		definition.setPredicates(predicates);
		// 过滤器
		List<FilterDefinition> filters = buildFilters(data, serviceCode, id);
		definition.setFilters(filters);
		definition.setOrder(order * -1);
		return definition;
	}


	private static List<FilterDefinition> buildFilters(RateLimiterPathMap data, String serviceCode, String id) {
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
		return filters;
	}


	private static List<String> filterDeletedRoutes(List<RateLimiterPathMap> datas) {
        return datas.stream()
				.filter(d -> null != d.getIsDeleted() && d.getIsDeleted())
				.map(RateLimiterPathMap::getId)
				.collect(Collectors.toList());
	}

	@Value("${spring.datasource.url:}")
    private String url;
    @Value("${spring.datasource.username:}")
    private String username;
    @Value("${spring.datasource.password:}")
    private String password;
    private boolean dealFinish = true;
    
    private DataSource dataSource;
    
    public List<CustomKeyResolverConfig.RateLimiterPathMap> getSysRouteConfig() {
    	List<CustomKeyResolverConfig.RateLimiterPathMap> sysRouteConfigList = new ArrayList<>();
    	if(dealFinish) {
    		dealFinish = false;
    		try {
				String queryCondition = buildQueryCondition();

				Connection connection = null;
    	        Statement statement = null;
    	        ResultSet resultSet = null;

    	        try {
    	            connection = dataSource.getConnection();
    	            String sql = "SELECT id,is_deleted,rate,count,rate_limiter_path,service_code,referer,remote_addr FROM sys_route_config where " + queryCondition;
    	            statement = connection.createStatement();
    	            resultSet = statement.executeQuery(sql);

    	            CustomKeyResolverConfig.RateLimiterPathMap rateLimiterPathMap = null;
    	            while (resultSet.next()) {
    	            	rateLimiterPathMap = new RateLimiterPathMap();
    	            	rateLimiterPathMap.setId(resultSet.getString("id").trim());
    	            	rateLimiterPathMap.setIsDeleted(resultSet.getBoolean("is_deleted"));
    	            	rateLimiterPathMap.setRate(resultSet.getInt("rate"));
    	            	rateLimiterPathMap.setCount(resultSet.getInt("count"));
    	            	rateLimiterPathMap.setRateLimiterPath(resultSet.getString("rate_limiter_path"));
    	            	rateLimiterPathMap.setServiceCode(resultSet.getString("service_code"));
    	            	rateLimiterPathMap.setReferer(resultSet.getString("referer"));
    	            	rateLimiterPathMap.setRemoteAddr(resultSet.getString("remote_addr"));

    	                sysRouteConfigList.add(rateLimiterPathMap);
    	            }
    	        } catch (Exception e) {
    	            log.error("查询动态路由数据失败" , e);
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
    	            	log.error("关闭动态路由数据连接失败" , e);
    	            }
    	        }
    		}catch(Exception e) {
    			log.error("获取动态路由数据失败" , e);
    		}finally {
    			dealFinish = true;
    		}
    	}
        return sysRouteConfigList;
    }

	private String buildQueryCondition() {
		String queryCondition = null;
		if( null == dataSource ) {
			HikariConfig config = new HikariConfig();
			config.setJdbcUrl(url);
			config.setUsername(username);
			config.setPassword(password);

			// 设置其他连接池参数，例如最大连接数、最小空闲连接数等
			config.setMaximumPoolSize(3);
			config.setMinimumIdle(1);
			dataSource = new HikariDataSource(config);
			log.warn("初始化动态路由数据库连接池成功");
			queryCondition = "is_deleted = 'f'";
		}else {
			queryCondition = "update_time >= '" + DateUtil.formatDateTime(DateUtil.offsetSecond(new Date(), -(routereFreshTime + 1))) + "'";
		}
		return queryCondition;
	}

}
