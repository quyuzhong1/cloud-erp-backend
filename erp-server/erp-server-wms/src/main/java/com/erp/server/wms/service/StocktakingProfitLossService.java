package com.erp.server.wms.service;

import com.erp.model.wms.entity.StocktakingProfitLossEntity;
import com.common.business.service.SuperService;
import com.erp.model.wms.entity.StocktakingTaskEntity;

/**
 * <p>
 * 盘盈盘亏单 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-07-31
 */
public interface StocktakingProfitLossService extends SuperService<StocktakingProfitLossEntity> {

    
    /**
     * 盘点任务单审核通过生成盘盈盘亏单
     * @author yl
     * @date 2023-08-10 11:52
     * @param taskEntity
     * @return void
     */
    void autoCreateBill(StocktakingTaskEntity taskEntity);
}
