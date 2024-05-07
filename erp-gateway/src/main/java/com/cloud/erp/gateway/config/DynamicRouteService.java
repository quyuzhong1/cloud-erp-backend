package com.cloud.erp.gateway.config;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.common.business.service.impl.RedisService;
import com.common.business.utils.RedisUtil;

import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@SuppressWarnings("all")
public class DynamicRouteService implements ApplicationEventPublisherAware {

    /* 写gateway中的路由定义 */
    private final RouteDefinitionWriter routeDefinitionWriter;

    /* 获取路由定义 */
    private final RouteDefinitionLocator routeDefinitionLocator;

    /* 事件发布对象 */
    private ApplicationEventPublisher publisher;
    
    @Autowired
    private RedisService redisService;

    public DynamicRouteService(RouteDefinitionWriter routeDefinitionWriter, RouteDefinitionLocator routeDefinitionLocator) {
        this.routeDefinitionWriter = routeDefinitionWriter;
        this.routeDefinitionLocator = routeDefinitionLocator;
    }


    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        //完成时间推送句柄的初始化
        this.publisher = applicationEventPublisher;
    }

    /**
     * @Description 添加路由定义
     * @Params [definition]
     * @Return java.lang.String
     * @Author JiaChaoYang
     * @Date 2022/9/12 9:24
     */
    public String addRouteDefinition(RouteDefinition/*读取出来的配置会到这里，网关*/ definition){
        log.info("gateway add route: {}",definition);
        /* 保存路由配置并发布 */
        routeDefinitionWriter.save(Mono.just(definition)).subscribe();
        /* 发布事件通知给Gateway  同步新增路由定义 */
        this.publisher.publishEvent(new RefreshRoutesEvent(this));
        return "success";
    }

    /**
     * @Description 根据路由id去删除路由配置
     * @Params [id]
     * @Return java.lang.String
     * @Author JiaChaoYang
     * @Date 2022/9/12 9:29
     */
    public String deleteById(String id){
        try {
            log.info("gateway delete route id : {}",id);
            this.routeDefinitionWriter.delete(Mono.just(id));
            redisService.deleteObject(String.format("request_rate_limiter.{%s}.timestamp", id));
            redisService.deleteObject(String.format("request_rate_limiter.{%s}.tokens", id));
            //发布事件通知给gateway 更新路由定义
            this.publisher.publishEvent(new RefreshRoutesEvent(this));
            return "delete success";
        }catch (Exception e) {
            log.error("gateway delete route fail: {}",e.getMessage(),e);
            return "delete fail";
        }
    }

    /**
     * @Description 更新路由
     * @Params [routeDefinitionList]
     * @Return java.lang.String
     * @Author JiaChaoYang
     * @Date 2022/9/12 9:36
     */
    public String  updateList(List<RouteDefinition> routeDefinitionList){
        // 把更新的路由定义同步到gateway中
        routeDefinitionList.forEach(definition -> updateByRouteDefinition(definition));
        return "success";
    }

    public String  saveList(List<RouteDefinition> routeDefinitionList){
        log.info("gateway update route: {}",routeDefinitionList);
        Set<String> hasIds;
        //拿到当前gateway 中存储的路由定义
        List<RouteDefinition> routeDefinitions = routeDefinitionLocator.getRouteDefinitions().buffer().blockFirst();
        if (!CollectionUtils.isEmpty(routeDefinitions)){
            hasIds = routeDefinitions.stream().map(RouteDefinition::getId).collect(Collectors.toSet());
        }else {
            hasIds = new HashSet<>();
        }
        // 把更新的路由定义同步到gateway中
        routeDefinitionList.forEach(definition -> {
            if (hasIds.contains(definition.getId())){
                return;
            }
            addRouteDefinition(definition);
        });
        return "success";
    }

    /**
     * @Description 更新路由，更新的实现策略比较简单：删除 + 新增 = 更新
     * @Params [definition]
     * @Return java.lang.String
     * @Author JiaChaoYang
     * @Date 2022/9/12 9:33
     */
    private String updateByRouteDefinition(RouteDefinition definition){
        try {
            log.info("gateway update route : {}",definition);
            this.deleteById(definition.getId());
        }catch (Exception e) {
            return "update fail , not find route routeId:"+ definition.getId();
        }
        try {
            this.routeDefinitionWriter.save(Mono.just(definition)).subscribe();
            this.publisher.publishEvent(new RefreshRoutesEvent(this));
            return "success";
        }catch (Exception e) {
            return "update route fail";
        }
    }

}

