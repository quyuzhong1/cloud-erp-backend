package com.erp.server.wms.service;

import com.erp.model.wms.dto.inventory.InOutStockDTO;

import java.util.List;

/**
 * 旺店通销售出库同步前：实体仓库存结构化预检，不足时按仓位优先级自动移仓。
 * <p>在扣库存事务外执行；移仓走 {@code wdtAutoAddNewTx}（REQUIRES_NEW），不影响 DMP Job 使用的 {@code wdtAutoAdd}。</p>
 */
public interface WdtSoOutstockAutoMoveService {

    /**
     * 按出库明细预检目标仓位可用量；不足则规划并执行仓位移动。
     * <p>凑不满时不移仓、不抛错，交由后续 {@code approveByRule} 按原逻辑报库存不足。</p>
     *
     * @param inOutStockList 本单待扣减的实体仓出入库明细（与后续 approveByRule 同源）
     */
    void preCheckAndAutoMove(List<InOutStockDTO> inOutStockList);
}
