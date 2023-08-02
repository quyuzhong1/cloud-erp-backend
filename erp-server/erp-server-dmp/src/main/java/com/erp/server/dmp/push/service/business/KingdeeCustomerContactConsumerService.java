package com.erp.server.dmp.push.service.business;

import java.util.Map;

/**
 * @author Lambda
 * @Classname KingdeeCustomerContactConsumerService
 * @Description TODO
 * @Date 2023-08-01 19:18
 * @Created by yl
 */
public interface KingdeeCustomerContactConsumerService {
    /**
     *同步客户联系人信息
     *
     * @param map
     * @return void
     * @author yl
     * @date 2023-08-01 12:19
     */
    void executeConsumer(Map<String, Object> map);
}
