package com.erp.server.scm.kingdee;

import com.erp.model.scm.entity.PurchasePriceChangeEntity;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/4/10 14:40
 */
public interface SyncKingdeePurchasePriceChangeService {

    /**
     * @description:推送金蝶
     * @author Will
     * @date: 2023/4/11 18:06
     * @param entity
     * @param operate
     */
    void syncDataToKingdee(PurchasePriceChangeEntity entity, String operate);
}
