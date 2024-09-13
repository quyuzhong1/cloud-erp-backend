package com.erp.server.wms.kingdee;

import java.util.Map;

import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.wms.entity.OtherInstockEntity;

/**
 * @author Will
 * @version 1.0

 * @date 2023/5/24 18:57
 */
public interface SyncKingdeeOtherInstockService {

    /**
     * 直接调拨单推送金蝶
     */
    DmpPushTaskEntity syncDataToKingdee(OtherInstockEntity entity, String operate);
    
    Map<String , Object> newSyncDataToKingdee(OtherInstockEntity entity, String operate);
}
