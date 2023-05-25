package com.erp.server.wms.kingdee;

import com.erp.model.wms.entity.OtherOutstockEntity;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/5/24 18:57
 */
public interface SyncKingdeeOtherOutstockService {

    /**
     * 直接调拨单推送金蝶
     */
    void syncDataToKingdee(OtherOutstockEntity entity, String operate);
}
