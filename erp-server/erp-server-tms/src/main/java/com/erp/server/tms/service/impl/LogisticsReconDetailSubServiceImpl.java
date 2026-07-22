package com.erp.server.tms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.tms.dto.LogisticsReconDTO;
import com.erp.model.tms.dto.LogisticsReconMatchDTO;
import com.erp.model.tms.entity.LogisticsReconDetailSubEntity;
import com.erp.model.tms.enums.LogisticsReconDetailMatchStatusEnum;
import com.erp.model.tms.enums.LogisticsReconReconciliationStatusEnum;
import com.erp.server.tms.mapper.LogisticsReconDetailSubMapper;
import com.erp.server.tms.service.LogisticsReconDetailSubService;
import com.erp.server.tms.service.support.LogisticsReconMatchFailReasonSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.hutool.core.util.StrUtil;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 物流商对账费用项明细 服务实现类
 * </p>
 *
 * @author Will
 * @since 2026-05-29
 */
@Slf4j
@Service
public class LogisticsReconDetailSubServiceImpl
        extends SuperServiceImpl<LogisticsReconDetailSubMapper, LogisticsReconDetailSubEntity>
        implements LogisticsReconDetailSubService {

    /**
     * 按 id 集合分片更新/查询，避免一次性 IN 过多 id 超出 SQL 长度限制。
     */
    private static final int UPDATE_BATCH_SIZE = 1000;

    /** 主表整批匹配认领时可覆盖的前置 match_status */
    private static final List<String> MAIN_CLAIM_FROM_STATUSES = Arrays.asList(
            LogisticsReconDetailMatchStatusEnum.UNMATCHED.getCode(),
            LogisticsReconDetailMatchStatusEnum.FAILED.getCode());

    /** 已有 ref 确认结果、可以安全收敛为终态 MATCHED 的对账状态。 */
    private static final List<String> CONFIRMED_RECONCILIATION_STATUSES = Arrays.asList(
            LogisticsReconReconciliationStatusEnum.CONFIRMED.getCode(),
            LogisticsReconReconciliationStatusEnum.PARTIAL_CONFIRM.getCode());

    private static final long DEFAULT_MATCHING_STALE_MINUTES = 120;

    /** matching 超时窗口；可由 Nacos 配置，非正数自动回退到两小时。 */
    @Value("${tms.logistics-recon.matching-stale-minutes:120}")
    private long matchingStaleMinutes = DEFAULT_MATCHING_STALE_MINUTES;

    /** 超时 MATCHING 打回失败时的原因文案 */
    private static final String STALE_MATCHING_FAIL_REASON = "匹配中断超时，可重试";

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeByMainIds(Collection<String> mainIds) {
        if (CollUtil.isEmpty(mainIds)) {
            return;
        }
        lambdaUpdate()
                .in(LogisticsReconDetailSubEntity::getMainId, mainIds)
                .remove();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<String> batchUpdateMatchStatus(Collection<String> detailSubIds, String matchStatus, String failReason) {
        return batchUpdateMatchStatus(detailSubIds, matchStatus, failReason, null);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<String> batchUpdateMatchStatus(Collection<String> detailSubIds, String matchStatus, String failReason,
                                               Collection<String> fromMatchStatuses) {
        return batchClaimMatchStatus(detailSubIds, matchStatus, failReason, fromMatchStatuses);
    }

    /**
     * 原子认领：同事务内先 SELECT ... FOR UPDATE 锁定符合 fromMatchStatuses 的行，
     * 再仅更新被锁定的这些 id，返回值即本次真正认领成功的 id。
     *
     * @param detailSubIds       待认领费用项 id
     * @param matchStatus        目标 match_status
     * @param failReason         失败原因（仅置 failed 时写入）
     * @param fromMatchStatuses  前置 match_status 条件（为空则不限制）
     * @return 本次真正认领成功的费用项 id
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<String> batchClaimMatchStatus(Collection<String> detailSubIds, String matchStatus, String failReason,
                                              Collection<String> fromMatchStatuses) {
        if (CollUtil.isEmpty(detailSubIds)) {
            return Collections.emptyList();
        }
        String reason = LogisticsReconDetailMatchStatusEnum.FAILED.getCode().equals(matchStatus) ? failReason : "";
        List<String> idList = new ArrayList<>(detailSubIds).stream().distinct().collect(Collectors.toList());
        List<String> claimedIds = new ArrayList<>();
        for (int i = 0; i < idList.size(); i += UPDATE_BATCH_SIZE) {
            List<String> batch = idList.subList(i, Math.min(idList.size(), i + UPDATE_BATCH_SIZE));
            List<String> lockedIds = lambdaQuery()
                    .in(LogisticsReconDetailSubEntity::getId, batch)
                    .in(CollUtil.isNotEmpty(fromMatchStatuses),
                            LogisticsReconDetailSubEntity::getMatchStatus, fromMatchStatuses)
                    .and(LogisticsReconDetailMatchStatusEnum.MATCHING.getCode().equals(matchStatus), status -> status
                            .isNull(LogisticsReconDetailSubEntity::getReconciliationStatus)
                            .or()
                            .eq(LogisticsReconDetailSubEntity::getReconciliationStatus,
                                    LogisticsReconReconciliationStatusEnum.TO_BE_CONFIRM.getCode()))
                    .and(LogisticsReconDetailMatchStatusEnum.MATCHING.getCode().equals(matchStatus), wrapper -> wrapper
                            .isNull(LogisticsReconDetailSubEntity::getMatchFailReason)
                            .or()
                            // 与 isNonRetryable 前缀语义一致：NOT (LIKE 'prefix%')，非 contains
                            .not(w -> w.likeRight(LogisticsReconDetailSubEntity::getMatchFailReason,
                                    LogisticsReconMatchFailReasonSupport.NON_RETRYABLE_PREFIX)))
                    .select(LogisticsReconDetailSubEntity::getId,
                            LogisticsReconDetailSubEntity::getReconciliationStatus)
                    .orderByAsc(LogisticsReconDetailSubEntity::getId)
                    .last("FOR UPDATE")
                    .list().stream()
                    .filter(row -> !LogisticsReconDetailMatchStatusEnum.MATCHING.getCode().equals(matchStatus)
                            || (!LogisticsReconReconciliationStatusEnum.CONFIRMED.getCode().equals(row.getReconciliationStatus())
                            && !LogisticsReconReconciliationStatusEnum.PARTIAL_CONFIRM.getCode().equals(row.getReconciliationStatus())))
                    .map(LogisticsReconDetailSubEntity::getId)
                    .collect(Collectors.toList());
            if (CollUtil.isEmpty(lockedIds)) {
                continue;
            }
            lambdaUpdate()
                    .in(LogisticsReconDetailSubEntity::getId, lockedIds)
                    .set(LogisticsReconDetailSubEntity::getMatchStatus, matchStatus)
                    .set(LogisticsReconDetailSubEntity::getMatchFailReason, reason)
                    .update();
            claimedIds.addAll(lockedIds);
        }
        return claimedIds;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void batchUpdateResolvedCfgCost(Map<String, LogisticsReconMatchDTO.ResolvedCfgCostDTO> resolvedBySubId,
                                           Collection<String> detailSubIds) {
        if (CollUtil.isEmpty(detailSubIds) || CollUtil.isEmpty(resolvedBySubId)) {
            return;
        }
        List<String> idList = detailSubIds.stream()
                .filter(StrUtil::isNotBlank)
                .distinct()
                .filter(resolvedBySubId::containsKey)
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(idList)) {
            return;
        }
        String matchingStatus = LogisticsReconDetailMatchStatusEnum.MATCHING.getCode();
        for (int i = 0; i < idList.size(); i += UPDATE_BATCH_SIZE) {
            List<String> batch = idList.subList(i, Math.min(idList.size(), i + UPDATE_BATCH_SIZE));
            List<LogisticsReconDetailSubEntity> updates = lambdaQuery()
                    .in(LogisticsReconDetailSubEntity::getId, batch)
                    .eq(LogisticsReconDetailSubEntity::getMatchStatus, matchingStatus)
                    .select(LogisticsReconDetailSubEntity::getId)
                    .list().stream()
                    .map(entity -> {
                        LogisticsReconMatchDTO.ResolvedCfgCostDTO resolved = resolvedBySubId.get(entity.getId());
                        if (resolved == null || StrUtil.isBlank(resolved.getCfgCostId())) {
                            return null;
                        }
                        LogisticsReconDetailSubEntity update = new LogisticsReconDetailSubEntity();
                        update.setId(entity.getId());
                        update.setCfgCostId(resolved.getCfgCostId());
                        update.setCfgCostName(StrUtil.blankToDefault(resolved.getCfgCostName(), ""));
                        return update;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (CollUtil.isNotEmpty(updates)) {
                updateBatchById(updates);
            }
        }
    }

    /**
     * 单批认领整单可匹配费用项：单条 {@code FOR UPDATE} 原子认领并置为 matching。
     * <p>可认领范围：未匹配 / 失败，以及 update_time 已超时的 matching（崩溃 / 中断遗留，直接抢占重试，
     * 不再需要独立的超时重置动作）。排除 reconciliation_status=confirmed 的费用项。</p>
     * <p>认领时刷新 update_time 作为心跳起点；行锁在事务提交前持有，并发认领会阻塞后重判条件，
     * 避免"重置→再认领"两步之间的抢占窗口。</p>
     *
     * @param mainId    对账单 id
     * @param batchSize 单批认领上限
     * @return 认领成功的费用项 id
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<String> claimMainSubsMatchingBatch(String mainId, int batchSize) {
        if (StrUtil.isBlank(mainId)) {
            return Collections.emptyList();
        }
        int limit = batchSize > 0 ? Math.min(batchSize, UPDATE_BATCH_SIZE) : UPDATE_BATCH_SIZE;
        LocalDateTime staleThreshold = LocalDateTime.now().minusMinutes(resolveMatchingStaleMinutes());
        List<String> lockedIds = lambdaQuery()
                .eq(LogisticsReconDetailSubEntity::getMainId, mainId)
                .and(w -> w
                        .isNull(LogisticsReconDetailSubEntity::getReconciliationStatus)
                        .or()
                        .eq(LogisticsReconDetailSubEntity::getReconciliationStatus,
                                LogisticsReconReconciliationStatusEnum.TO_BE_CONFIRM.getCode()))
                .and(w -> w
                        .in(LogisticsReconDetailSubEntity::getMatchStatus, MAIN_CLAIM_FROM_STATUSES)
                        .or(q -> q
                                .eq(LogisticsReconDetailSubEntity::getMatchStatus,
                                        LogisticsReconDetailMatchStatusEnum.MATCHING.getCode())
                                .and(stale -> stale
                                        .isNull(LogisticsReconDetailSubEntity::getUpdateTime)
                                        .or()
                                        .lt(LogisticsReconDetailSubEntity::getUpdateTime, staleThreshold))))
                .and(wrapper -> wrapper
                        .isNull(LogisticsReconDetailSubEntity::getMatchFailReason)
                        .or()
                        // 与 isNonRetryable 前缀语义一致：NOT (LIKE 'prefix%')，非 contains
                        .not(w -> w.likeRight(LogisticsReconDetailSubEntity::getMatchFailReason,
                                LogisticsReconMatchFailReasonSupport.NON_RETRYABLE_PREFIX)))
                .select(LogisticsReconDetailSubEntity::getId)
                .orderByAsc(LogisticsReconDetailSubEntity::getId)
                .last("LIMIT " + limit + " FOR UPDATE")
                .list().stream()
                .map(LogisticsReconDetailSubEntity::getId)
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(lockedIds)) {
            return Collections.emptyList();
        }
        lambdaUpdate()
                .in(LogisticsReconDetailSubEntity::getId, lockedIds)
                .set(LogisticsReconDetailSubEntity::getMatchStatus,
                        LogisticsReconDetailMatchStatusEnum.MATCHING.getCode())
                .set(LogisticsReconDetailSubEntity::getMatchFailReason, "")
                .set(LogisticsReconDetailSubEntity::getUpdateTime, LocalDateTime.now())
                .update();
        return lockedIds;
    }

    /**
     * 分批将主单下超时 MATCHING 费用项打回 FAILED，便于提交/执行匹配时重新认领。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public LogisticsReconDTO.MatchTransitionStats failStaleMatchingSubsByMainId(String mainId) {
        LogisticsReconDTO.MatchTransitionStats stats = new LogisticsReconDTO.MatchTransitionStats();
        if (StrUtil.isBlank(mainId)) {
            return stats;
        }
        stats.merge(settleConfirmedMatchingSubsByMainId(mainId));
        long staleMinutes = resolveMatchingStaleMinutes();
        LocalDateTime staleThreshold = LocalDateTime.now().minusMinutes(staleMinutes);
        int total = 0;
        while (true) {
            List<LogisticsReconDetailSubEntity> lockedRows = lambdaQuery()
                    .eq(LogisticsReconDetailSubEntity::getMainId, mainId)
                    .eq(LogisticsReconDetailSubEntity::getMatchStatus,
                            LogisticsReconDetailMatchStatusEnum.MATCHING.getCode())
                    .and(stale -> stale
                            .isNull(LogisticsReconDetailSubEntity::getUpdateTime)
                            .or()
                            .lt(LogisticsReconDetailSubEntity::getUpdateTime, staleThreshold))
                    .and(status -> status
                            .isNull(LogisticsReconDetailSubEntity::getReconciliationStatus)
                            .or()
                            .eq(LogisticsReconDetailSubEntity::getReconciliationStatus,
                                    LogisticsReconReconciliationStatusEnum.TO_BE_CONFIRM.getCode()))
                    .select(LogisticsReconDetailSubEntity::getId,
                            LogisticsReconDetailSubEntity::getMatchFailReason,
                            LogisticsReconDetailSubEntity::getLocalAmount)
                    .orderByAsc(LogisticsReconDetailSubEntity::getId)
                    .last("LIMIT " + UPDATE_BATCH_SIZE + " FOR UPDATE")
                    .list();
            if (CollUtil.isEmpty(lockedRows)) {
                break;
            }
            List<String> uncertainIds = lockedRows.stream()
                    .filter(row -> LogisticsReconMatchFailReasonSupport.isNonRetryable(row.getMatchFailReason()))
                    .map(LogisticsReconDetailSubEntity::getId)
                    .collect(Collectors.toList());
            List<String> retryableIds = lockedRows.stream()
                    .filter(row -> !LogisticsReconMatchFailReasonSupport.isNonRetryable(row.getMatchFailReason()))
                    .map(LogisticsReconDetailSubEntity::getId)
                    .collect(Collectors.toList());
            failStaleMatchingRows(uncertainIds, staleThreshold, null);
            failStaleMatchingRows(retryableIds, staleThreshold, STALE_MATCHING_FAIL_REASON);
            for (LogisticsReconDetailSubEntity row : lockedRows) {
                stats.addFailed(row.getLocalAmount());
            }
            total += lockedRows.size();
            if (lockedRows.size() < UPDATE_BATCH_SIZE) {
                break;
            }
        }
        if (total > 0) {
            log.warn("[failStaleMatchingSubsByMainId] 超时匹配中已打回失败 mainId={} count={} thresholdMinutes={}",
                    mainId, total, staleMinutes);
        }
        return stats;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public LogisticsReconDTO.MatchTransitionStats settleConfirmedMatchingSubsByMainId(String mainId) {
        LogisticsReconDTO.MatchTransitionStats stats = new LogisticsReconDTO.MatchTransitionStats();
        if (StrUtil.isBlank(mainId)) {
            return stats;
        }
        while (true) {
            List<LogisticsReconDetailSubEntity> lockedRows = lambdaQuery()
                    .eq(LogisticsReconDetailSubEntity::getMainId, mainId)
                    .eq(LogisticsReconDetailSubEntity::getMatchStatus,
                            LogisticsReconDetailMatchStatusEnum.MATCHING.getCode())
                    .in(LogisticsReconDetailSubEntity::getReconciliationStatus,
                            CONFIRMED_RECONCILIATION_STATUSES)
                    .select(LogisticsReconDetailSubEntity::getId,
                            LogisticsReconDetailSubEntity::getLocalAmount)
                    .orderByAsc(LogisticsReconDetailSubEntity::getId)
                    .last("LIMIT " + UPDATE_BATCH_SIZE + " FOR UPDATE")
                    .list();
            if (CollUtil.isEmpty(lockedRows)) {
                break;
            }
            List<String> ids = lockedRows.stream()
                    .map(LogisticsReconDetailSubEntity::getId)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toList());
            lambdaUpdate()
                    .in(LogisticsReconDetailSubEntity::getId, ids)
                    .eq(LogisticsReconDetailSubEntity::getMainId, mainId)
                    .eq(LogisticsReconDetailSubEntity::getMatchStatus,
                            LogisticsReconDetailMatchStatusEnum.MATCHING.getCode())
                    .in(LogisticsReconDetailSubEntity::getReconciliationStatus,
                            CONFIRMED_RECONCILIATION_STATUSES)
                    .set(LogisticsReconDetailSubEntity::getMatchStatus,
                            LogisticsReconDetailMatchStatusEnum.MATCHED.getCode())
                    .set(LogisticsReconDetailSubEntity::getMatchFailReason, "")
                    .set(LogisticsReconDetailSubEntity::getUpdateTime, LocalDateTime.now())
                    .update();
            for (LogisticsReconDetailSubEntity row : lockedRows) {
                stats.addMatched(row.getLocalAmount());
            }
            if (lockedRows.size() < UPDATE_BATCH_SIZE) {
                break;
            }
        }
        return stats;
    }

    private void failStaleMatchingRows(List<String> ids, LocalDateTime staleThreshold, String failReason) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        LambdaUpdateChainWrapper<LogisticsReconDetailSubEntity> update = lambdaUpdate()
                .in(LogisticsReconDetailSubEntity::getId, ids)
                .eq(LogisticsReconDetailSubEntity::getMatchStatus,
                        LogisticsReconDetailMatchStatusEnum.MATCHING.getCode())
                .and(stale -> stale
                            .isNull(LogisticsReconDetailSubEntity::getUpdateTime)
                            .or()
                            .lt(LogisticsReconDetailSubEntity::getUpdateTime, staleThreshold))
                .set(LogisticsReconDetailSubEntity::getMatchStatus,
                        LogisticsReconDetailMatchStatusEnum.FAILED.getCode());
        if (failReason != null) {
            update.set(LogisticsReconDetailSubEntity::getMatchFailReason, failReason);
        }
        update.update();
    }

    private long resolveMatchingStaleMinutes() {
        return matchingStaleMinutes > 0 ? matchingStaleMinutes : DEFAULT_MATCHING_STALE_MINUTES;
    }

    @Override
    public int countValidByMainId(String mainId) {
        if (StrUtil.isBlank(mainId)) {
            return 0;
        }
        return baseMapper.countValidByMainId(mainId);
    }

    @Override
    public BigDecimal sumLocalAmountByMainId(String mainId) {
        if (StrUtil.isBlank(mainId)) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = baseMapper.sumLocalAmountByMainId(mainId);
        return total == null ? BigDecimal.ZERO : total;
    }

    @Override
    public List<LogisticsReconDTO.PagingStatsDTO> listPagingStatsByMainIds(List<String> mainIds) {
        if (CollUtil.isEmpty(mainIds)) {
            return Collections.emptyList();
        }
        return baseMapper.listPagingStatsByMainIds(mainIds);
    }
}
