package com.common.business.factory;

import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.EnumMap;
import java.util.Map;

@Component
public class ApproveEndHandlerFactory {
    private final Map<SourceTypeEnum, AbstractApproveHandler> handlerMap = new EnumMap<>(SourceTypeEnum.class);
    
    @Resource
    private ApplicationContext applicationContext;

    @PostConstruct
    public void init() {
        // 扫描所有带有@ApproveBusinessKey注解的Bean
        Map<String, Object> handlers = applicationContext.getBeansWithAnnotation(ApproveBusinessKey.class);
        
        handlers.forEach((beanName, handler) -> {
            // 获取原始类
            Class<?> targetClass = AopUtils.getTargetClass(handler);
            ApproveBusinessKey annotation = AnnotationUtils.findAnnotation(targetClass, ApproveBusinessKey.class);
            if (handler instanceof AbstractApproveHandler && annotation != null) {
                    handlerMap.put(annotation.value(), (AbstractApproveHandler) handler);
                }

        });
    }

    /**
     * 获取处理器
     */
    public AbstractApproveHandler getHandler(SourceTypeEnum sourceType) {
        AbstractApproveHandler handler = handlerMap.get(sourceType);
        if (handler == null) {
            // 未找到处理器
            throw new ServiceException(ApiError.ERROR_NOT_FOUND_APPROVE_HANDLER,sourceType.getName());
        }
        return handler;
    }
}