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
     * 发送金蝶
     */
    void syncDataToKingdee(SysUserInfoEntity entity);
}
