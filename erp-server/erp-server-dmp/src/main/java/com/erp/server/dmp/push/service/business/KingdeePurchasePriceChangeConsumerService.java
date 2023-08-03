package com.erp.server.dmp.push.service.business;

import java.util.Map;

/**
 * @author Lambda
 * @Classname KingdeePurchasePriceChangeConsumerService
 * @Description TODO
 * @Date 2023-08-01 20:32
 * @Created by yl
 */
public interface KingdeePurchasePriceChangeConsumerService {

    void executeConsumer(Map<String, Object> map);
}
