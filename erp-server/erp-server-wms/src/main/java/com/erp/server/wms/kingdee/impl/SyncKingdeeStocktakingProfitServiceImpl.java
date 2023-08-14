package com.erp.server.wms.kingdee.impl;

import com.erp.model.wms.entity.StocktakingProfitLossEntity;
import com.erp.server.wms.kingdee.SyncKingdeeStocktakingProfitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author Lambda
 * @Classname SyncKingdeeStocktakingProfitServiceImpl
 * @Description TODO
 * @Date 2023-08-14 15:13
 * @Created by yl
 */
@Slf4j
@Service
public class SyncKingdeeStocktakingProfitServiceImpl  implements SyncKingdeeStocktakingProfitService {

    /**
     * 同步金蝶
     * @param entity
     * @param code
     */
    @Override
    public void syncDataToKingdee(StocktakingProfitLossEntity entity, String code) {

    }
}
