package com.erp.server.scm.kingdee;

import java.util.Map;

import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.scm.entity.AssetPurchaseOrderEntity;
import com.erp.model.scm.entity.PurchaseOrderEntity;

/**
 * @author Will
 * @version 1.0

 * @date 2023/4/10 14:40
 */
public interface SyncKingdeePurchaseOrderService {

    /**
     * @description:推送金蝶
     * @author Will
     * @date: 2023/4/11 18:06
     * @param entity
     * @param operate
     */
    DmpPushTaskEntity syncDataToKingdee(PurchaseOrderEntity entity, String operate);
    
    Map<String, Object> newSyncDataToKingdee(PurchaseOrderEntity entity, String operate);

    DmpPushTaskEntity syncDataToKingdee(AssetPurchaseOrderEntity entity, String operate);

    Map<String, Object> newSyncDataToKingdee(AssetPurchaseOrderEntity entity, String operate);

}
