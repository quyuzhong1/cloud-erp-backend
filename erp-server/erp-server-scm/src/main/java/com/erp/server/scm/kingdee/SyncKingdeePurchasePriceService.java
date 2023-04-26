package com.erp.server.scm.kingdee;

import com.erp.model.scm.entity.PurchasePriceDetailEntity;
import com.erp.model.scm.entity.PurchasePriceEntity;

import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/4/10 14:40
 */
public interface SyncKingdeePurchasePriceService {

    /**
     * @description:推送金蝶
     * @author Will
     * @date: 2023/4/11 18:06
     * @param entity
     * @param operate
     */
    void syncDataToKingdee(PurchasePriceEntity entity, String operate);
    /**
     * @description: 金蝶更新分录禁用
     * @author Will
     * @date: 2023/4/26 15:56
     * @param detailList
     * @param disabled
     */
    void syncDataDetailToKingdee(List<PurchasePriceDetailEntity> detailList, Boolean disabled);
}
