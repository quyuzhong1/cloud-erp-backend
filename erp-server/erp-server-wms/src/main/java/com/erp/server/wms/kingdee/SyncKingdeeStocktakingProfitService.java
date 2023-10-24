package com.erp.server.wms.kingdee;

import com.erp.model.wms.entity.StocktakingProfitLossEntity;

/** 盘盈单同步金蝶
 * @author Lambda
 * @Classname SyncKingdeeStocktakingProfitService
 * @Description TODO
 * @Date 2023-08-14 15:08
 * @Created by yl
 */
public interface SyncKingdeeStocktakingProfitService {
    void syncDataToKingdee(StocktakingProfitLossEntity entity, String code);
}

