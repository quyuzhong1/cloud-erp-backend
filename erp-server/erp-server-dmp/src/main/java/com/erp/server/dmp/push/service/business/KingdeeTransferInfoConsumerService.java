package com.erp.server.dmp.push.service.business;

import java.util.Map;

/**
 * @author Lambda
 * @Classname KingdeeTransferInfoConsumerService
 * @Description TODO
 * @Date 2023-08-02 9:49
 * @Created by yl
 */
public interface KingdeeTransferInfoConsumerService {

    void executeConsumer(Map<String, Object> map);
}
