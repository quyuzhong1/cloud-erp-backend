package com.erp.server.dmp.push.service.business;

import java.util.Map;

/**
 * @author Lambda
 * @Classname KingdeeMachineInfoConsumerService
 * @Description TODO
 * @Date 2023-08-01 19:46
 * @Created by yl
 */
public interface KingdeeMachineInfoConsumerService {

    /**
     * 同步加工单
     * @param map
     */
    void executeConsumer(Map<String, Object> map);
}
