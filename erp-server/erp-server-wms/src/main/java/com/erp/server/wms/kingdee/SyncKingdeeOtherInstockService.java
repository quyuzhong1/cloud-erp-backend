package com.erp.server.wms.kingdee;

import com.erp.model.wms.entity.OtherInstockEntity;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/5/24 18:57
 */
public interface SyncKingdeeOtherInstockService {

    /**
     * 直接调拨单推送金蝶
     */
    void syncDataToKingdee(OtherInstockEntity entity, String operate);
}
