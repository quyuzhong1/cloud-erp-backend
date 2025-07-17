package com.erp.server.wms.rocketmq.sync;

import com.common.business.dto.TeMuSoOutStockDTO;
import com.common.business.dto.WdtSoOutStockDTO;
import com.erp.model.dmp.kingdee.KingdeeDeliveryDetailEntity;

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

    /**
     * 同步旺店通的销售出库单
     * @param entity 旺店通参数
     */
    void syncWdtSoOutStock(WdtSoOutStockDTO entity);

    void syncTemuSoOutStock(TeMuSoOutStockDTO entity);
}
