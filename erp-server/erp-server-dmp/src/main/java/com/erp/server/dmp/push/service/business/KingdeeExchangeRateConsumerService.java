package com.erp.server.dmp.push.service.business;

import java.util.Map;

/**
 * 汇率数据同步金蝶
 * @author Cloud
 * @date 2025-09-29
 */
public interface KingdeeExchangeRateConsumerService {
    /**
     * 汇率数据同步金蝶
     * @author will
     * @date  2025-09-29
     * @param map
     * @return void
     */
    void executeConsumer(Map<String, Object> map);
}
