package com.erp.server.tms.service;

import com.common.business.service.SuperService;
import com.erp.model.tms.dto.LogisticsReconDetailSubDTO;
import com.erp.model.tms.entity.LogisticsReconDetailSubEntity;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 物流商对账费用项明细 服务类
 * </p>
 *
 * @author Will
 * @since 2026-05-29
 */
public interface LogisticsReconDetailSubService extends SuperService<LogisticsReconDetailSubEntity> {

    /**
     * 物流商对账费用项查询（按 detail_id 批量；详情页展开 / 合并匹配阶段读取）
     * @author Will
     * @date: 2026/05/29
     * @param detailIds
     * @return List<LogisticsReconDetailSubDTO.ListDTO>
     */
    List<LogisticsReconDetailSubDTO.ListDTO> listByDetailIds(Collection<String> detailIds);

    /**
     * 按 detail_id 级联逻辑删除（用于行级删除 / 主表 batchDelete）
     * @author Will
     * @date: 2026/05/29
     * @param detailIds
     * @return
     */
    void removeByDetailIds(Collection<String> detailIds);

    /**
     * 按 main_id 级联逻辑删除
     * @author Will
     * @date: 2026/05/29
     * @param mainIds
     * @return
     */
    void removeByMainIds(Collection<String> mainIds);

    /**
     * 物流商对账费用项配置外补齐 cfg_cost_id
     * @author Will
     * @date: 2026/05/29
     * @param detailSubIds
     * @param cfgCostId
     * @param cfgCostName
     * @return
     */
    void batchUpdateCfgCost(Collection<String> detailSubIds, String cfgCostId, String cfgCostName);

    /**
     * 批量更新费用项匹配状态（合并匹配/手动匹配/解绑等内部调用）
     * @author Will
     * @date: 2026/06/01
     * @param detailSubIds
     * @param matchStatus
     * @param failReason
     * @return void
     */
    void batchUpdateMatchStatus(Collection<String> detailSubIds, String matchStatus, String failReason);
}
