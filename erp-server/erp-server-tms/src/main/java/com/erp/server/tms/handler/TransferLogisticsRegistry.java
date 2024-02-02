package com.erp.server.tms.handler;

import com.common.business.annotation.TransferLogisticsPlatformType;
import com.erp.server.tms.service.TransferLogisticsService;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 物流业务初始化处理
 *
 * @author Cloud
 */
@Service
public class TransferLogisticsRegistry {
    private final Map<String, TransferLogisticsService> handlers = new HashMap<>();

    @Resource
    private ApplicationContext context;

    @PostConstruct
    public void init() {
        Map<String, TransferLogisticsService> beans = context.getBeansOfType(TransferLogisticsService.class);
        for (Object bean : beans.values()) {
            Class<?> actualClass = AopProxyUtils.ultimateTargetClass(bean);
            TransferLogisticsPlatformType platformAnnotation = actualClass.getAnnotation(TransferLogisticsPlatformType.class);
            handlers.put(platformAnnotation.value().getCode(), (TransferLogisticsService) bean);
        }
    }

    public TransferLogisticsService getHandler(String platform) {
        return handlers.get(platform);
    }
}
