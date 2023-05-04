package com.erp.server.sys.rocketmq.sync.kingdee;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/4/10 14:40
 */
public interface SyncKingdeeSysDeptService {

    /**
     * @description:新增同步
     * @author Will
     * @date: 2023/4/11 18:06
     * @param id
     * @param operate
     */
    void syncDataToKingdee(String id, String operate);
}
