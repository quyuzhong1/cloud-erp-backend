package com.erp.server.dmp.push.service.business;

import java.util.Map;

/**
 * @author Lambda
 * @Classname KingdeeSysPostConsumerService
 * @Description TODO
 * @Date 2024-03-13 16:25
 * @Created by yl
 */
public interface KingdeeSysPostConsumerService {
    void executeConsumer(Map<String, Object> map);
}
