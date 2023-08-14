package com.erp.server.wms.kingdee.impl;

import com.erp.model.wms.entity.StocktakingProfitLossEntity;
import com.erp.server.wms.kingdee.SyncKingdeeStocktakingLossService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author Lambda
 * @Classname SyncKingdeeStocktakingLossServiceImpl
 * @Description TODO
 * @Date 2023-08-14 15:13
 * @Created by yl
 */
@Slf4j
@Service
public class SyncKingdeeStocktakingLossServiceImpl implements SyncKingdeeStocktakingLossService {
   
   /**
    * 同步金蝶
    * @author yl
    * @date 2023-08-14 15:14
    * @param entity
    * @param code
    * @return void
    */
   
    @Override
    public void syncDataToKingdee(StocktakingProfitLossEntity entity, String code) {
        
    }
}
