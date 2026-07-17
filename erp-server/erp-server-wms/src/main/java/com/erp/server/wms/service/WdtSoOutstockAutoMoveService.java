package com.erp.server.wms.service;

import com.erp.model.wms.dto.inventory.InOutStockDTO;
import com.erp.model.wms.entity.SoOutstockDetailEntity;

import java.util.List;

/**
 * 旺店通销售出库同步前：实体仓库存结构化预检，不足时按仓位优先级自动移仓至空仓位。
 * <p>在扣库存事务外执行；移仓走 {@code wdtAutoAddNewTx}（REQUIRES_NEW），不影响 DMP Job 使用的 {@code wdtAutoAdd}。</p>
 */
public interface WdtSoOutstockAutoMoveService {

    /**
     * 按出库明细预检当前库位可用量；不足则改写明细为空仓位并全量移入空仓位。
     * <p>凑不满时不移仓、不抛错，交由后续 {@code approveByRule} 按原逻辑报库存不足。</p>
     * <p>已存在同 sourceId 的移仓单时跳过移仓，避免异常重试重复移仓；库位改写仍会执行。</p>
     *
     * @param detailList     本单出库明细（不足时会改写 warehouseLocation 为空仓位）
     * @param inOutStockList 本单待扣减的实体仓出入库明细（与 detail 同步改写）
     * @param sourceId       幂等键：旺店通出库单号
     * @param sourceCode     来源单号（通常同 sourceId）
     */
    void preCheckAndAutoMove(List<SoOutstockDetailEntity> detailList,
                             List<InOutStockDTO> inOutStockList,
                             String sourceId,
                             String sourceCode);
}
