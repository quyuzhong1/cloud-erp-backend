package com.erp.server.tms.service;

import com.common.business.service.SuperService;
import com.erp.model.tms.dto.LogisticsReconDetailSubDTO;
import com.erp.model.tms.dto.LogisticsReconMatchDTO;
import com.erp.model.tms.entity.LogisticsReconDetailSubEntity;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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
     * 批量更新费用项匹配状态（合并匹配/手动匹配/解绑等内部调用）
     * @author Will
     * @date: 2026/06/01
     * @param detailSubIds
     * @param matchStatus
     * @param failReason
     * @return void
     */
    void batchUpdateMatchStatus(Collection<String> detailSubIds, String matchStatus, String failReason);

    /**
     * 批量更新费用项匹配状态（带前置 match_status 条件，防并发覆盖）
     */
    void batchUpdateMatchStatus(Collection<String> detailSubIds, String matchStatus, String failReason,
                                Collection<String> fromMatchStatuses);

    /**
     * 匹配成功回写 ERP 费用配置（仅 matching 状态、分批 updateBatchById）
     */
    void batchUpdateResolvedCfgCost(Map<String, LogisticsReconMatchDTO.ResolvedCfgCostDTO> resolvedBySubId,
                                    Collection<String> detailSubIds);

    /**
     * 条件更新匹配状态并返回实际更新成功的费用项 id（用于认领防并发）。
     */
    List<String> batchClaimMatchStatus(Collection<String> detailSubIds, String matchStatus, String failReason,
                                       Collection<String> fromMatchStatuses);

    /**
     * 认领对账单下一批可匹配费用项（短事务），返回本次真正认领成功的 id 集合。
     */
    List<String> claimMainSubsMatchingBatch(String mainId, int batchSize);
}
