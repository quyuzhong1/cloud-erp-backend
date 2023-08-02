package com.erp.server.dmp.push.service.business;

import java.util.Map;

/**
 * @author Lambda
 * @Classname KingdeeBomInfoConsumer
 * @Description TODO
 * @Date 2023-08-01 18:43
 * @Created by yl
 */
public interface KingdeeBomInfoConsumerService {
    /**
     * 同步bomINFO
     *
     * @param map
     */
    void executeBomInfoConsumer(Map<String, Object> map);
}
