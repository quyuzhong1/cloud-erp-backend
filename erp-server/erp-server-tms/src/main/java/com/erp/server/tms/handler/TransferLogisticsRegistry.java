package com.erp.server.tms.handler;

import com.common.business.annotation.LogisticsPlatformType;
import com.erp.server.tms.service.LogisticsService;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 中转报关商方法处理器
 * @Author Luo_WG
 * @Date 2024/1/19 11:40
 **/
@Service
public class TransferLogisticsRegistry {
    private final Map<String, LogisticsService> handlers = new HashMap<>();

    @Resource
    private ApplicationContext context;

    @PostConstruct
    public void init() {
        Map<String, LogisticsService> beans = context.getBeansOfType(LogisticsService.class);
        for (Object bean : beans.values()) {
            Class<?> actualClass = AopProxyUtils.ultimateTargetClass(bean);
            LogisticsPlatformType platformAnnotation = actualClass.getAnnotation(LogisticsPlatformType.class);
            handlers.put(platformAnnotation.value().getCode(), (LogisticsService) bean);
        }
    }

    public LogisticsService getHandler(String platform) {
        return handlers.get(platform);
    }
}
