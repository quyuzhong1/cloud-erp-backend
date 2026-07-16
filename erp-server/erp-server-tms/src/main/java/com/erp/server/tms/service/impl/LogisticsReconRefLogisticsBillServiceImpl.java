package com.erp.server.tms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.erp.model.tms.entity.LogisticsReconRefLogisticsBillEntity;
import com.erp.server.tms.mapper.LogisticsReconRefLogisticsBillMapper;
import com.erp.server.tms.service.LogisticsReconRefLogisticsBillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 物流商对账单 - 关联关系 服务实现类
 * </p>
 *
 * @author Will
 * @since 2026-05-29
 */
@Slf4j
@Service
public class LogisticsReconRefLogisticsBillServiceImpl
        extends SuperServiceImpl<LogisticsReconRefLogisticsBillMapper, LogisticsReconRefLogisticsBillEntity>
        implements LogisticsReconRefLogisticsBillService {

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveBatchByDetail(List<LogisticsReconRefLogisticsBillEntity> refList) {
        if (CollUtil.isEmpty(refList)) {
            return;
        }
        List<String> detailIds = refList.stream()
                .map(LogisticsReconRefLogisticsBillEntity::getDetailId)
                .distinct()
                .collect(Collectors.toList());
        removeByDetailIds(detailIds);

        LoginUser user = UserContext.getDefaultLoginUser();
        LocalDateTime now = LocalDateTime.now();
        refList.forEach(ref -> {
            if (ref.getMatchTime() == null) {
                ref.setMatchTime(now);
            }
            if (ref.getMatchUserId() == null && user != null) {
                ref.setMatchUserId(user.getUid());
                ref.setMatchUserName(user.getUserName());
            }
        });
        super.saveBatch(refList);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveBatchByDetailSub(List<LogisticsReconRefLogisticsBillEntity> refList) {
        if (CollUtil.isEmpty(refList)) {
            return;
        }
        List<String> detailSubIds = refList.stream()
                .map(LogisticsReconRefLogisticsBillEntity::getDetailSubId)
                .distinct()
                .collect(Collectors.toList());
        removeByDetailSubIds(detailSubIds);

        LoginUser user = UserContext.getDefaultLoginUser();
        LocalDateTime now = LocalDateTime.now();
        refList.forEach(ref -> {
            if (ref.getMatchTime() == null) {
                ref.setMatchTime(now);
            }
            if (ref.getMatchUserId() == null && user != null) {
                ref.setMatchUserId(user.getUid());
                ref.setMatchUserName(user.getUserName());
            }
        });
        super.saveBatch(refList);
    }

    /**
     * 按 id 集合分片删除，避免一次性 IN 过多 id 超出 SQL 长度限制。
     */
    private static final int REMOVE_BATCH_SIZE = 1000;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeByDetailIds(Collection<String> detailIds) {
        if (CollUtil.isEmpty(detailIds)) {
            return;
        }
        List<String> idList = new ArrayList<>(detailIds);
        for (int i = 0; i < idList.size(); i += REMOVE_BATCH_SIZE) {
            List<String> batch = idList.subList(i, Math.min(idList.size(), i + REMOVE_BATCH_SIZE));
            lambdaUpdate()
                    .in(LogisticsReconRefLogisticsBillEntity::getDetailId, batch)
                    .remove();
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeByDetailSubIds(Collection<String> detailSubIds) {
        if (CollUtil.isEmpty(detailSubIds)) {
            return;
        }
        List<String> idList = new ArrayList<>(detailSubIds);
        for (int i = 0; i < idList.size(); i += REMOVE_BATCH_SIZE) {
            List<String> batch = idList.subList(i, Math.min(idList.size(), i + REMOVE_BATCH_SIZE));
            lambdaUpdate()
                    .in(LogisticsReconRefLogisticsBillEntity::getDetailSubId, batch)
                    .remove();
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeByMainIds(Collection<String> mainIds) {
        if (CollUtil.isEmpty(mainIds)) {
            return;
        }
        lambdaUpdate()
                .in(LogisticsReconRefLogisticsBillEntity::getMainId, mainIds)
                .remove();
    }
}
