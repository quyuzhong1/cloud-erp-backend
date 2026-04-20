package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.wms.entity.QcSamplingPlanDetailEntity;
import com.erp.model.wms.entity.QcSamplingPlanEntity;
import com.erp.server.wms.mapper.QcSamplingPlanDetailMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.QcSamplingPlanDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2026-03-20
 */
@Slf4j
@Service
public class QcSamplingPlanDetailServiceImpl extends SuperServiceImpl<QcSamplingPlanDetailMapper, QcSamplingPlanDetailEntity> implements QcSamplingPlanDetailService {
    @Resource
    private OperateLogService operateLogService;

    @Override
    public List<QcSamplingPlanDetailEntity> listByMainId(String id) {
        if (CharSequenceUtil.isBlank(id)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().eq(QcSamplingPlanDetailEntity::getMainId, id).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDetail(List<QcSamplingPlanDetailEntity> detailList, QcSamplingPlanEntity qcSamplingPlanEntity) {
        List<QcSamplingPlanDetailEntity> oldList = this.listByMainId(qcSamplingPlanEntity.getId());
        if (CollUtil.isNotEmpty(oldList)) {
            this.removeByMainId(qcSamplingPlanEntity.getId(), detailList.stream().map(QcSamplingPlanDetailEntity::getId).filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList()));
        }
        //填充信息
        if (CollUtil.isEmpty(detailList)) {
            return;
        }
        detailList.forEach(detail -> {
            detail.setMainId(qcSamplingPlanEntity.getId());
        });
        this.saveOrUpdateBatch(detailList);
    }

    @Override
    public void removeByMainId(String id) {
        this.removeByMainId(id, Collections.emptyList());
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeByMainId(String mainId, List<String> idList) {
        this.lambdaUpdate().eq(QcSamplingPlanDetailEntity::getMainId, mainId)
                .notIn(CollUtil.isNotEmpty(idList), QcSamplingPlanDetailEntity::getId, idList).remove();
    }
}
