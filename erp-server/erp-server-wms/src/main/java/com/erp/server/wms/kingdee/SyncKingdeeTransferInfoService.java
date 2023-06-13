package com.erp.server.wms.kingdee;

import com.erp.model.wms.entity.TransferInfoEntity;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/5/23 16:24
 */
public interface SyncKingdeeTransferInfoService {

    /**
     * 直接调拨单推送金蝶
     */
    void syncDataToKingdee(TransferInfoEntity entity, String operate);
}
