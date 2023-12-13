package com.common.business.handler;

import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 业务初始化处理
 * @author Cloud
 */
@Service
public class BusinessHandlerRegistry {
    private final Map<String, IBusinessHandler<?,?>> handlers = new HashMap<>();

    @Resource
    private ApplicationContext context;

    @PostConstruct
    public void init() {
        Map<String, IBusinessHandler> beans = context.getBeansOfType(IBusinessHandler.class);
        for (Object bean : beans.values()) {
            Class<?> actualClass = AopProxyUtils.ultimateTargetClass(bean);
            PlatformCategoryType categoryAnnotation = actualClass.getAnnotation(PlatformCategoryType.class);
            PlatformType platformAnnotation = actualClass.getAnnotation(PlatformType.class);
            BusinessType businessAnnotation = actualClass.getAnnotation(BusinessType.class);

            if (categoryAnnotation != null && platformAnnotation != null && businessAnnotation != null) {
                String key = categoryAnnotation.value().getCode() + "-" +
                             platformAnnotation.value().getCode() + "-" +
                             businessAnnotation.value().getCode();
                handlers.put(key, (IBusinessHandler<?,?>) bean);
            }
        }
    }

    public IBusinessHandler<?,?> getHandler(String category, String platform, String business) {
        return handlers.get(category + "-" + platform + "-" + business);
    }
}
