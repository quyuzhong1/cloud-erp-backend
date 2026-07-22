package com.erp.server.wms.service;

import com.erp.model.wms.dto.inventory.InOutStockDTO;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.model.wms.entity.SoOutstockDetailEntity;

import java.util.List;

/**
 * 旺店通销售出库同步前：实体仓库存结构化预检，不足时按仓位优先级自动移仓至空仓位。
 * <p>在扣库存事务外执行；移仓走 {@code wdtAutoAddNewTx}（REQUIRES_NEW），不影响 DMP Job 使用的 {@code wdtAutoAdd}。</p>
 */
public interface WdtSoOutstockAutoMoveService {

    /**
     * 按出库仓位推荐规则和库区优先级匹配可用库存仓位。
     * <p>拣货区仓位直接出库；非拣货区先移至空仓位；无可用仓位时抛出业务异常。</p>
     * <p>移仓按 sourceId + sourceType + warehouseId 幂等：解析前若已存在已审核移仓单，
     * 按移仓明细回写目标仓位后再推荐剩余明细，避免重试时因库存已移至空仓位而误报缺货。</p>
     *
     * @param soOutstock    本单出库主单（用于规则匹配）
     * @param detailList     本单出库明细（不足时会改写 warehouseLocation 为空仓位）
     * @param inOutStockList 本单待扣减的实体仓出入库明细（与 detail 同步改写）
     * @param sourceId       幂等键：旺店通出库单号
     * @param sourceCode     来源单号（通常同 sourceId）
     */
    void preCheckAndAutoMove(SoOutstockEntity soOutstock,
                             List<SoOutstockDetailEntity> detailList,
                             List<InOutStockDTO> inOutStockList,
                             String sourceId,
                             String sourceCode);
}
