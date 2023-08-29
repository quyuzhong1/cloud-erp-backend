package com.common.business.handler;

import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.handler.BusinessHandler;
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
    private final Map<String, BusinessHandler<?>> handlers = new HashMap<>();

    @Resource
    private ApplicationContext context;

    @PostConstruct
    public void init() {
        Map<String, BusinessHandler> beans = context.getBeansOfType(BusinessHandler.class);
        for (Object bean : beans.values()) {
            PlatformCategoryType categoryAnnotation = bean.getClass().getAnnotation(PlatformCategoryType.class);
            PlatformType platformAnnotation = bean.getClass().getAnnotation(PlatformType.class);
            BusinessType businessAnnotation = bean.getClass().getAnnotation(BusinessType.class);

            if (categoryAnnotation != null && platformAnnotation != null && businessAnnotation != null) {
                String key = categoryAnnotation.value().getCode() + "-" +
                             platformAnnotation.value().getCode() + "-" +
                             businessAnnotation.value().getCode();
                handlers.put(key, (BusinessHandler<?>) bean);
            }
        }
    }

    public BusinessHandler<?> getHandler(String category, String platform, String business) {
        return handlers.get(category + "-" + platform + "-" + business);
    }
}
