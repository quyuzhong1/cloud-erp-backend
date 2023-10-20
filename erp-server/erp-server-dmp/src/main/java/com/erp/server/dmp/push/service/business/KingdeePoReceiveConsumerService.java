package com.erp.server.dmp.push.service.business;

import java.util.Map;

/**
 * 采购收货单同步金蝶
 * @Author Luo_WG
 * @Date 2023/10/11 9:06
 **/
public interface KingdeePoReceiveConsumerService {
    void executeConsumer(Map<String, Object> map);
}
