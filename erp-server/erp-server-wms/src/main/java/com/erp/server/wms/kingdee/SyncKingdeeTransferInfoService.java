package com.erp.server.wms.kingdee;

import java.util.Map;

import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.wms.entity.TransferInfoEntity;

/**
 * @author Will
 * @version 1.0

 * @date 2023/5/23 16:24
 */
public interface SyncKingdeeTransferInfoService {

    /**
     * 直接调拨单推送金蝶
     */
    DmpPushTaskEntity syncDataToKingdee(TransferInfoEntity entity, String operate);
    
    Map<String , Object> newSyncDataToKingdee(TransferInfoEntity entity, String operate);
}
