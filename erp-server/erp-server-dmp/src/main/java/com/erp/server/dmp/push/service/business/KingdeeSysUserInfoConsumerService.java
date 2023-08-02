package com.erp.server.dmp.push.service.business;

import java.util.Map;

/**
 * @author Lambda
 * @Classname KingdeeSysUserInfoConsumerService
 * @Description TODO
 * @Date 2023-08-01 18:27
 * @Created by yl
 */
public interface KingdeeSysUserInfoConsumerService {

    /**
     * 同步员工信息
     *
     * @param map
     * @return void
     * @author yl
     * @date 2023-08-01 12:19
     */
    void executeSysUserConsumer(Map<String, Object> map);
}
