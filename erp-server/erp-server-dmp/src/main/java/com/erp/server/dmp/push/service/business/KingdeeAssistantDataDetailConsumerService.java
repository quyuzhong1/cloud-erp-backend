package com.erp.server.dmp.push.service.business;

import java.util.Map;

/**
 * @author Lambda
 * @Classname KingdeeAssistantDataDetailConsumerService
 * @Description TODO
 * @Date 2023-08-01 18:34
 * @Created by yl
 */
public interface KingdeeAssistantDataDetailConsumerService {

    /**
     * 同步辅助资料
     *
     * @param
     * @return void
     * @author yl
     * @date 2023-08-01 14:07
     */
    void executeAssistantDataDetailConsumer(Map<String, Object> map);
}
