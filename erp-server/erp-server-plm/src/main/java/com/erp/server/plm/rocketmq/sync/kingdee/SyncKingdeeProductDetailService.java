package com.erp.server.plm.rocketmq.sync.kingdee;

import java.util.Map;

import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.plm.entity.ProductDetailEntity;

/**
 * @author Will
 * @version 1.0

 * @date 2023/3/8 18:17
 */
public interface SyncKingdeeProductDetailService {
    /**
     * 金蝶同步数据
     * @author Will
     * @date: 2023/3/8 18:22
     * @param entity
     */
    DmpPushTaskEntity syncDataToKingdee(ProductDetailEntity entity, String operate);
    
    Map<String, Object> newSyncDataToKingdee(ProductDetailEntity entity, String operate);
}
