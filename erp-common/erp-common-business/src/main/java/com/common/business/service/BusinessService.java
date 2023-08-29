package com.common.business.service;

import com.common.business.handler.BusinessHandler;
import com.common.business.handler.BusinessHandlerRegistry;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 业务处理服务类
 * @author Cloud
 */
@Service
public class BusinessService {

    @Resource
    private BusinessHandlerRegistry registry;

    public <T> void processBusiness(String category, String platform, String business, T data) {
        BusinessHandler<T> handler = (BusinessHandler<T>) registry.getHandler(category, platform, business);
        if (handler != null) {
            handler.handle(data);
        } else {
            // Handle the case when no handler is found
            throw new RuntimeException("No handler found for category: " + category + ", platform: " + platform + ", business: " + business);
        }
    }
}
