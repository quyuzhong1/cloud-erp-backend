package com.erp.server.plm.rocketmq.sync.kingdee;

import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.plm.entity.BomInfoEntity;
import com.erp.model.plm.entity.ProductBomSkuHistoryEntity;
import com.erp.model.plm.entity.ProductDetailEntity;

import java.util.List;
import java.util.Map;

/**
 * @author Will
 * @version 1.0

 * @date 2023/3/9 9:41
 */
public interface SyncKingdeeBomInfoService {
    /**
     * 金蝶同步数据
     * @author Will
     * @date: 2023/3/8 18:22
     * @param entity
     */
    List<DmpPushTaskEntity> syncDataToKingdee(BomInfoEntity entity, String operate);
    
    void syncDataToSdy(BomInfoEntity entity, String operate);
    
    Map<String, Object> newSyncDataToSdy(ProductBomSkuHistoryEntity entity, String operate);
}
