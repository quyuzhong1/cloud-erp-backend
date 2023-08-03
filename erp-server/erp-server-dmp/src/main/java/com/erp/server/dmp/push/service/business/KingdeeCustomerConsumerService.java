package com.erp.server.dmp.push.service.business;

import java.util.Map;

/**
 * @author Lambda
 * @Classname KingdeeCustomerConsumerService
 * @Description TODO
 * @Date 2023-08-01 19:12
 * @Created by yl
 */
public interface KingdeeCustomerConsumerService {
    /**
     * 同步客户联系人信息
     *
     * @param map
     * @return void
     * @author yl
     * @date 2023-08-01 14:20
     */
    void executeCustomerContactConsumer(Map<String, Object> map);
}
