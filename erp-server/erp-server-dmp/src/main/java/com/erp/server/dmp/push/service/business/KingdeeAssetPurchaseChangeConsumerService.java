package com.erp.server.dmp.push.service.business;

import java.util.Map;

/**
 * @description: 王维
 * @author Will
 * @date: 2023/10/11 10:56
 */
public interface KingdeeAssetPurchaseChangeConsumerService {

    /**
     * 同步加工单
     * @param map
     */
    void executeConsumer(Map<String, Object> map);
}
