package com.erp.server.tms.service;

import com.common.business.service.SuperService;
import com.erp.model.tms.dto.LogisticsReconDTO;
import com.erp.model.tms.dto.LogisticsReconMatchDTO;
import com.erp.model.tms.entity.LogisticsReconDetailSubEntity;

import java.math.BigDecimal;
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
     *
     * @param detailSubIds       待认领费用项 id
     * @param matchStatus        目标 match_status
     * @param failReason         失败原因（仅置 failed 时写入）
     * @param fromMatchStatuses  前置 match_status 条件（为空则不限制）
     * @return 本次真正更新成功的费用项 id
     */
    List<String> batchClaimMatchStatus(Collection<String> detailSubIds, String matchStatus, String failReason,
                                       Collection<String> fromMatchStatuses);

    /**
     * 认领对账单下一批可匹配费用项（短事务），返回本次真正认领成功的 id 集合。
     * 排除已确认/部分确认费用项，仅认领未匹配/失败状态及可安全回收的超时匹配状态。
     *
     * @param mainId    对账单 id
     * @param batchSize 单批认领上限
     * @return 认领成功的费用项 id
     */
    List<String> claimMainSubsMatchingBatch(String mainId, int batchSize);

    /**
     * 将主单下「匹配中且 update_time 已超时」的费用项打回匹配失败，供后续重新认领匹配。
     * <p>用于进程宕机等场景下 MATCHING 残留；在跑任务会刷新 update_time，未超时的不会被误伤。</p>
     *
     * @param mainId 对账单 id
     * @return 本次打回失败的费用项数量
     */
    int failStaleMatchingSubsByMainId(String mainId);

    /**
     * 收敛已经产生确认结果但仍残留 MATCHING 的费用项，避免确认状态与匹配状态长期不一致。
     *
     * @param mainId 对账单 id
     * @return 本次收敛为 MATCHED 的费用项数量
     */
    int settleConfirmedMatchingSubsByMainId(String mainId);

    /**
     * 统计主表下有效费用项数（detail 归属与 sub.main_id 一致）
     */
    int countValidByMainId(String mainId);

    /**
     * 汇总主表下有效费用项的本位币金额（local_amount），无数据返回 0
     */
    BigDecimal sumLocalAmountByMainId(String mainId);

    /**
     * 按主表 id 集合批量聚合分页列表所需费用项统计
     */
    List<LogisticsReconDTO.PagingStatsDTO> listPagingStatsByMainIds(List<String> mainIds);
}
