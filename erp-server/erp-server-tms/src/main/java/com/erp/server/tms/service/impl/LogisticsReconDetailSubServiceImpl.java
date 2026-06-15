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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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

    @Override
    public List<LogisticsReconDetailSubDTO.ListDTO> listByDetailIds(Collection<String> detailIds) {
        if (CollUtil.isEmpty(detailIds)) {
            return Collections.emptyList();
        }
        List<String> ids = detailIds instanceof List ? (List<String>) detailIds : new ArrayList<>(detailIds);
        List<LogisticsReconDetailSubDTO.ListDTO> list = baseMapper.listByDetailIds(ids);
        list.forEach(item -> {
            item.setMatchStatusName(LogisticsReconDetailMatchStatusEnum.getName(item.getMatchStatus()));
            item.setReconciliationStatusName(
                    LogisticsReconReconciliationStatusEnum.getName(item.getReconciliationStatus()));
        });
        return list;
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

    /**
     * 按 id 集合分片更新匹配状态，避免一次性 IN 过多 id 超出 SQL 长度限制。
     */
    private static final int UPDATE_BATCH_SIZE = 1000;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void batchUpdateMatchStatus(Collection<String> detailSubIds, String matchStatus, String failReason) {
        batchUpdateMatchStatus(detailSubIds, matchStatus, failReason, null);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void batchUpdateMatchStatus(Collection<String> detailSubIds, String matchStatus, String failReason,
                                       Collection<String> fromMatchStatuses) {
        if (CollUtil.isEmpty(detailSubIds)) {
            return;
        }
        String reason = LogisticsReconDetailMatchStatusEnum.FAILED.getCode().equals(matchStatus) ? failReason : "";
        List<String> idList = new ArrayList<>(detailSubIds);
        for (int i = 0; i < idList.size(); i += UPDATE_BATCH_SIZE) {
            List<String> batch = idList.subList(i, Math.min(idList.size(), i + UPDATE_BATCH_SIZE));
            LambdaUpdateChainWrapper<LogisticsReconDetailSubEntity> updateChain = lambdaUpdate()
                    .in(LogisticsReconDetailSubEntity::getId, batch);
            if (CollUtil.isNotEmpty(fromMatchStatuses)) {
                updateChain.in(LogisticsReconDetailSubEntity::getMatchStatus, fromMatchStatuses);
            }
            updateChain
                    .set(LogisticsReconDetailSubEntity::getMatchStatus, matchStatus)
                    .set(LogisticsReconDetailSubEntity::getMatchFailReason, reason)
                    .update(new LogisticsReconDetailSubEntity());
        }
    }
}
