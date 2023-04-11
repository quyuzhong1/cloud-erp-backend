package com.erp.server.sys.rocketmq.sync.kingdee;

import com.erp.model.sys.entity.SysUserInfoEntity;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/4/10 14:40
 */
public interface SyncKingdeeSysUserInfoService {

    /**
     * @description:
     * @author Will
     * @date: 2023/4/11 18:06
     * @param entity
     * @param operate
     */
    void syncDataToKingdee(SysUserInfoEntity entity,String operate);
}
