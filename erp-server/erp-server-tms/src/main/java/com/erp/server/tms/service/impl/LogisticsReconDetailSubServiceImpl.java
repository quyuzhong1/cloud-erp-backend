package com.erp.server.tms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.tms.dto.LogisticsReconDetailSubDTO;
import com.erp.model.tms.entity.LogisticsReconDetailSubEntity;
import com.erp.model.tms.enums.LogisticsReconDetailMatchStatusEnum;
import com.erp.model.tms.enums.LogisticsReconReconciliationStatusEnum;
import com.erp.server.tms.mapper.LogisticsReconDetailSubMapper;
import com.erp.server.tms.service.LogisticsReconDetailSubService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.hutool.core.util.StrUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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

    private static final List<String> MAIN_CLAIM_FROM_STATUSES = Arrays.asList(
            LogisticsReconDetailMatchStatusEnum.UNMATCHED.getCode(),
            LogisticsReconDetailMatchStatusEnum.FAILED.getCode());

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
     * 这样并发认领时后到的线程会阻塞，待前者提交后这些行已不在 fromMatchStatuses，
     * 从而避免「SELECT 当前 matching 全集」导致的跨线程假阳性。
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

    /**
     * 单批认领整单可匹配费用项：短事务内先查候选 id，再 FOR UPDATE 原子认领。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<String> claimMainSubsMatchingBatch(String mainId, int batchSize) {
        if (StrUtil.isBlank(mainId)) {
            return Collections.emptyList();
        }
        int limit = batchSize > 0 ? Math.min(batchSize, UPDATE_BATCH_SIZE) : UPDATE_BATCH_SIZE;
        List<String> candidateBatch = lambdaQuery()
                .eq(LogisticsReconDetailSubEntity::getMainId, mainId)
                .in(LogisticsReconDetailSubEntity::getMatchStatus, MAIN_CLAIM_FROM_STATUSES)
                .ne(LogisticsReconDetailSubEntity::getReconciliationStatus,
                        LogisticsReconReconciliationStatusEnum.CONFIRMED.getCode())
                .select(LogisticsReconDetailSubEntity::getId)
                .orderByAsc(LogisticsReconDetailSubEntity::getId)
                .last("LIMIT " + limit)
                .list().stream()
                .map(LogisticsReconDetailSubEntity::getId)
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(candidateBatch)) {
            return Collections.emptyList();
        }
        return batchClaimMatchStatus(candidateBatch,
                LogisticsReconDetailMatchStatusEnum.MATCHING.getCode(), null, MAIN_CLAIM_FROM_STATUSES);
    }
}
