package com.erp.server.wms.wdt;

import com.common.business.dto.DmpPushTaskFeignDTO;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.CreateOtherStockinRequest;

import java.util.List;

/**
 * 同步其他入库单到旺店通
 *
 * @author tanmujin
 * @date 2024-05-15
 */
public interface SyncWdtOtherInStockService {

    /**
     * 保存推送旺店通其他入库单任务
     *
     * @param goodsList          SKU明细列表
     * @param operateCode        操作方向: 审核/反审核
     * @param sourceCode         来源单据编号
     * @param detailId           明细ID
     * @param outerCode          外部单号
     * @param thirdWarehouseCode 第三方仓库编码
     * @return DmpPushTaskEntity 由DMP返回的任务实体
     * @date: 2024-05-25
     * @author: tanmujin
     */
    DmpPushTaskFeignDTO generateTask(List<CreateOtherStockinRequest.GoodsList> goodsList, String operateCode, String sourceCode, String detailId, String outerCode, String thirdWarehouseCode);
}
