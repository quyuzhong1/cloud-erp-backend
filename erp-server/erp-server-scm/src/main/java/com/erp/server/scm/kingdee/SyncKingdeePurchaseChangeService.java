package com.erp.server.scm.kingdee;

import java.util.Map;

import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.scm.entity.AssetPurchaseChangeEntity;
import com.erp.model.scm.entity.PurchaseChangeEntity;

/**
 * @author Will
 * @version 1.0
 * @description: 采购变更单推送金蝶接口
 * @date 2023/9/28 16:18
 */
public interface SyncKingdeePurchaseChangeService {

    /**
     * @description:推送金蝶
     * @author Will
     * @date: 2023/4/11 18:06
     * @param entity
     * @parChangerate
     */
    DmpPushTaskEntity syncDataToKingdee(PurchaseChangeEntity entity, String operate);
    
    Map<String , Object> newSyncDataToKingdee(PurchaseChangeEntity entity, String operate);

    DmpPushTaskEntity syncDataToKingdee(AssetPurchaseChangeEntity entity, String operate);

    Map<String , Object> newSyncDataToKingdee(AssetPurchaseChangeEntity entity, String operate);
}
