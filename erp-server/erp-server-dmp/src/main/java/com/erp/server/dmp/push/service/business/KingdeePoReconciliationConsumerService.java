package com.erp.server.dmp.push.service.business;

import java.util.Map;

/**
 * 分步式调出消费接口
 * @author will
 * @date 2025/4/22 18:03
 */
public interface KingdeePoReconciliationConsumerService {
    /**
     * 执行消费
     * @author will
     * @date 2025/4/22 18:03
     * @param map
     * @return void
     */
    void executeConsumer(Map<String, Object> map);
}
