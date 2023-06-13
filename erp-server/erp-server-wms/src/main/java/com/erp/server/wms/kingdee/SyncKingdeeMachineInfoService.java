package com.erp.server.wms.kingdee;

import com.erp.model.wms.entity.MachineInfoEntity;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/5/24 18:57
 */
public interface SyncKingdeeMachineInfoService {

    /**
     * 直接调拨单推送金蝶
     */
    void syncDataToKingdee(MachineInfoEntity entity, String operate);
}
