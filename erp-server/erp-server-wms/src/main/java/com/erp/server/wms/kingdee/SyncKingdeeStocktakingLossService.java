package com.erp.server.wms.kingdee;

import java.util.Map;

import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.wms.entity.StocktakingProfitLossEntity;

/**
 * @author Lambda
 * @Classname SyncKingdeeStocktakingLossService
 * @Description TODO
 * @Date 2023-08-14 15:08
 * @Created by yl
 */
public interface SyncKingdeeStocktakingLossService {
    DmpPushTaskEntity syncDataToKingdee(StocktakingProfitLossEntity entity, String code);
    Map<String , Object> newSyncDataToKingdee(StocktakingProfitLossEntity entity, String code);
}
