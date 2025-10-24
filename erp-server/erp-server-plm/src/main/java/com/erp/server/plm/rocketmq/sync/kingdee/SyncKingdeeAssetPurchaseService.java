package com.erp.server.plm.rocketmq.sync.kingdee;

import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.plm.entity.AssetPurchaseOrderEntity;

import java.util.Map;

/**
 * @Author: wtr
 * @Date: 2025/10/23 14:27
 * @Param:
 * @Return:
 * @Description:
 **/
public interface SyncKingdeeAssetPurchaseService {

    DmpPushTaskEntity syncDataToKingdee(AssetPurchaseOrderEntity entity, String operate);

    Map<String, Object> newSyncDataToKingdee(AssetPurchaseOrderEntity entity, String operate);

}
