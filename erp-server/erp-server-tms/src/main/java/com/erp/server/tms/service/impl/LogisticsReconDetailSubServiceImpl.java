package com.erp.server.tms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.tms.dto.LogisticsReconDetailSubDTO;
import com.erp.model.tms.dto.LogisticsReconMatchDTO;
import com.erp.model.tms.entity.LogisticsReconDetailSubEntity;
import com.erp.model.tms.enums.LogisticsReconDetailMatchStatusEnum;
import com.erp.model.tms.enums.LogisticsReconReconciliationStatusEnum;
import com.erp.server.tms.mapper.LogisticsReconDetailSubMapper;
import com.erp.server.tms.service.LogisticsReconDetailSubService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.hutool.core.util.StrUtil;
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

    /**
     * matching 超时分钟数：超过则视为崩溃/中断遗留，允许被重新认领直接抢占重试。
     * 在跑的任务由每个 chunk 刷新 update_time 续期，只要单 chunk 执行时长不超过该值即不会被误抢占。
     */
    private static final long MATCHING_STALE_MINUTES = 120;

    @Override
    public List<LogisticsReconDetailSubDTO.ListDTO> listByDetailIds(Collection<String> detailIds) {
        if (CollUtil.isEmpty(detailIds)) {
            return Collections.emptyList();
        }
        List<String> ids = detailIds instanceof List ? (List<String>) detailIds : new ArrayList<>(detailIds);
        List<LogisticsReconDetailSubDTO.ListDTO> merged = new ArrayList<>();
        for (int i = 0; i < ids.size(); i += UPDATE_BATCH_SIZE) {
            List<String> batch = ids.subList(i, Math.min(ids.size(), i + UPDATE_BATCH_SIZE));
            List<LogisticsReconDetailSubDTO.ListDTO> list = baseMapper.listByDetailIds(batch);
            if (CollUtil.isNotEmpty(list)) {
                merged.addAll(list);
            }
        }
        merged.forEach(item -> {
            item.setMatchStatusName(LogisticsReconDetailMatchStatusEnum.getName(item.getMatchStatus()));
            item.setReconciliationStatusName(
                    LogisticsReconReconciliationStatusEnum.getName(item.getReconciliationStatus()));
        });
        return merged;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeByDetailIds(Collection<String> detailIds) {
        if (CollUtil.isEmpty(detailIds)) {
            return;
        }
        lambdaUpdate()
                .in(LogisticsReconDetailSubEntity::getDetailId, detailIds)
                .remove();
    }

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
    public void batchUpdateMatchStatus(Collection<String> detailSubIds, String matchStatus, String failReason) {
        batchUpdateMatchStatus(detailSubIds, matchStatus, failReason, null);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void batchUpdateMatchStatus(Collection<String> detailSubIds, String matchStatus, String failReason,
                                       Collection<String> fromMatchStatuses) {
        batchClaimMatchStatus(detailSubIds, matchStatus, failReason, fromMatchStatuses);
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
                    .select(LogisticsReconDetailSubEntity::getId)
                    .orderByAsc(LogisticsReconDetailSubEntity::getId)
                    .last("FOR UPDATE")
                    .list().stream()
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
        LocalDateTime staleThreshold = LocalDateTime.now().minusMinutes(MATCHING_STALE_MINUTES);
        List<String> lockedIds = lambdaQuery()
                .eq(LogisticsReconDetailSubEntity::getMainId, mainId)
                .ne(LogisticsReconDetailSubEntity::getReconciliationStatus,
                        LogisticsReconReconciliationStatusEnum.CONFIRMED.getCode())
                .and(w -> w
                        .in(LogisticsReconDetailSubEntity::getMatchStatus, MAIN_CLAIM_FROM_STATUSES)
                        .or(q -> q
                                .eq(LogisticsReconDetailSubEntity::getMatchStatus,
                                        LogisticsReconDetailMatchStatusEnum.MATCHING.getCode())
                                .lt(LogisticsReconDetailSubEntity::getUpdateTime, staleThreshold)))
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
}
