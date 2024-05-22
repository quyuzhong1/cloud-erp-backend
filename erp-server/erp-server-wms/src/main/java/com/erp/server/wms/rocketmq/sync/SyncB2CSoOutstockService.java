package com.erp.server.wms.rocketmq.sync;

import com.erp.model.dmp.kingdee.KingdeeDeliveryDetailEntity;
import com.erp.model.dmp.wdt.WangDianOrderEntity;

/**
 * @author Lambda
 * @Classname SyncB2CSoOutstockService
 * @Date 2023-06-27 10:54
 * @Created by yl
 */
public interface SyncB2CSoOutstockService {
    
    /**
     * 同步金蝶的销售出库单
     * @author yl
     * @date 2023-06-27 14:05
     * @param entity
     * @return void
     */
    void syncKingdeeSoOutstock(KingdeeDeliveryDetailEntity entity);

    void syncWdtSoOutStock(WangDianOrderEntity entity);
}
