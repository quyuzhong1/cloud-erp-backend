package com.erp.server.sys.rocketmq.sync.kingdee;

import java.util.Map;

import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.sys.entity.SysUserInfoEntity;

/**
 * @author Will
 * @version 1.0

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
    DmpPushTaskEntity syncDataToKingdee(SysUserInfoEntity entity, String operate);
    
    Map<String , Object> newSyncDataToKingdee(SysUserInfoEntity entity, String operate);
}
