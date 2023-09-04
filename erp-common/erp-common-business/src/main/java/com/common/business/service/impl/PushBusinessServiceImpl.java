package com.common.business.service.impl;

import com.common.business.dto.JobTaskDTO;
import com.common.business.handler.BusinessHandlerRegistry;
import com.common.business.handler.IPushBusinessHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 业务处理服务类
 * @author Cloud
 */
@Slf4j
@Service
public class PushBusinessServiceImpl {
    @Resource
    private BusinessHandlerRegistry registry;

    public <T> void pushProcessBusiness(String category, String platform, String business, JobTaskDTO data) {
        IPushBusinessHandler<T> handler = (IPushBusinessHandler<T>) registry.getHandler(category, platform, business);
        if (handler != null) {
            handler.handle(data);
        } else {
            // Handle the case when no handler is found
            throw new RuntimeException("No handler found for category: " + category + ", platform: " + platform + ", business: " + business);
        }
    }
}
