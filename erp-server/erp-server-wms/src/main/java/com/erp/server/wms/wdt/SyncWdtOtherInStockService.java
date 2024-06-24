package com.erp.server.wms.wdt;

import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.wms.entity.OtherInstockEntity;
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
     * @param goodsList   SKU明细列表
     * @param entity      其他入库单原始单据
     * @param operateCode 操作方向: 审核/反审核
     * @param sourceCode  来源单据编号
     * @return DmpPushTaskEntity 由DMP返回的任务实体
     * @date: 2024-05-25
     * @author: tanmujin
     */
    DmpPushTaskEntity saveTask(List<CreateOtherStockinRequest.GoodsList> goodsList, OtherInstockEntity entity, String operateCode, String sourceCode);
}
