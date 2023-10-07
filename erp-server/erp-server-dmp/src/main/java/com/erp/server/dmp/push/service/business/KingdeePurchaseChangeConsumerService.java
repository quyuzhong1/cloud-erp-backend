package com.erp.server.dmp.push.service.business;

import java.util.Map;

/**
 * @description: 采购变更
 * @author Will
 * @date: 2023/9/28 18:01
 */
public interface KingdeePurchaseChangeConsumerService {

    void executeConsumer(Map<String, Object> map);
}
