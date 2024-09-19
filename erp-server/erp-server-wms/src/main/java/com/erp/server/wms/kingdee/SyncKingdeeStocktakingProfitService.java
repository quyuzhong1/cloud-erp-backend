package com.erp.server.wms.kingdee;

import java.util.Map;

import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.wms.entity.StocktakingProfitLossEntity;

/** 盘盈单同步金蝶
 * @author Lambda
 * @Classname SyncKingdeeStocktakingProfitService
 * @Description TODO
 * @Date 2023-08-14 15:08
 * @Created by yl
 */
public interface SyncKingdeeStocktakingProfitService {
    DmpPushTaskEntity syncDataToKingdee(StocktakingProfitLossEntity entity, String code);
    Map<String , Object> newSyncDataToKingdee(StocktakingProfitLossEntity entity, String code);
}

