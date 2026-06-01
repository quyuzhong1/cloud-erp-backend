package com.erp.server.tms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.erp.model.tms.dto.LogisticsReconRefLogisticsBillDTO;
import com.erp.model.tms.entity.LogisticsReconRefLogisticsBillEntity;
import com.erp.server.tms.mapper.LogisticsReconRefLogisticsBillMapper;
import com.erp.server.tms.service.LogisticsReconRefLogisticsBillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

    @Override
    public List<LogisticsReconRefLogisticsBillDTO.ListDTO> listByDetailIds(Collection<String> detailIds) {
        if (CollUtil.isEmpty(detailIds)) {
            return Collections.emptyList();
        }
        List<String> ids = detailIds instanceof List ? (List<String>) detailIds : new ArrayList<>(detailIds);
        return baseMapper.listByDetailIds(ids);
    }

    @Override
    public List<LogisticsReconRefLogisticsBillDTO.ListDTO> listByMainId(String mainId) {
        if (mainId == null || mainId.isEmpty()) {
            return Collections.emptyList();
        }
        return baseMapper.listByMainId(mainId);
    }

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
    public void removeByDetailIds(Collection<String> detailIds) {
        if (CollUtil.isEmpty(detailIds)) {
            return;
        }
        lambdaUpdate()
                .in(LogisticsReconRefLogisticsBillEntity::getDetailId, detailIds)
                .remove();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeByDetailSubIds(Collection<String> detailSubIds) {
        if (CollUtil.isEmpty(detailSubIds)) {
            return;
        }
        lambdaUpdate()
                .in(LogisticsReconRefLogisticsBillEntity::getDetailSubId, detailSubIds)
                .remove();
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
