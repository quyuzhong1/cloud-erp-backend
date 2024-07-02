package com.erp.server.wms.wdt;

import com.common.business.dto.DmpPushTaskFeignDTO;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
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
     * @param operateCode 操作代码: 审核/反审核
     * @param sourceCode  来源单据编号
     * @param detailId 明细行id
     * @param outerCode 外部单据号
     * @param thirdWarehouseCode 第三方仓库编码
     * @param checkOuterCode 是否校验外部单号重复
     * @return DmpPushTaskEntity DMP返回的任务
     * @date: 2024-05-25
     * @author: tanmujin
     */
    DmpPushTaskEntity saveTask(List<CreateOtherStockoutRequest.GoodsList> goodsList, String operateCode, String sourceCode, String detailId, String outerCode, String thirdWarehouseCode, boolean checkOuterCode);

    DmpPushTaskFeignDTO generateTask(List<CreateOtherStockoutRequest.GoodsList> goodsList, String operateCode, String sourceCode, String detailId, String outerCode, String thirdWarehouseCode, boolean checkOuterCode);
}
