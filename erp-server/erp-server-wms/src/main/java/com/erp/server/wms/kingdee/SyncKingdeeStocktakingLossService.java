package com.erp.server.wms.kingdee;

import com.erp.model.wms.entity.StocktakingProfitLossEntity;

/**
 * @author Lambda
 * @Classname SyncKingdeeStocktakingLossService
 * @Description TODO
 * @Date 2023-08-14 15:08
 * @Created by yl
 */
public interface SyncKingdeeStocktakingLossService {
    String syncDataToKingdee(StocktakingProfitLossEntity entity, String code);
}
