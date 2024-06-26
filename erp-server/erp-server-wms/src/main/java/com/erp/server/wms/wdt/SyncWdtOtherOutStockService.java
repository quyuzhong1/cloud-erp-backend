package com.erp.server.wms.wdt;

import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.wms.entity.OtherOutstockEntity;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.CreateOtherStockoutRequest;

import java.util.List;

/**
 * 旺店通其他出库单
 * @author tanmujin
 * @date 2024-05-16
 */
public interface SyncWdtOtherOutStockService {

    /**
     * 保存推送旺店通其他出库单任务
     *
     * @param goodsList   SKU明细列表
     * @param entity      其他出库单
     * @param operateCode 操作代码: 审核/反审核
     * @param sourceCode  来源单据编号
     * @param detailId
     * @param code
     * @param warehouseId
     * @return DmpPushTaskEntity DMP返回的任务
     * @date: 2024-05-25
     * @author: tanmujin
     */
    DmpPushTaskEntity saveTask(List<CreateOtherStockoutRequest.GoodsList> goodsList, OtherOutstockEntity entity, String operateCode, String sourceCode, String detailId, String code, String warehouseId);
}
