package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.wms.entity.QcSamplingPlanEntity;
import com.erp.model.wms.entity.QcSamplingPlanQcTypeRefEntity;
import com.erp.server.wms.mapper.QcSamplingPlanQcTypeRefMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.QcSamplingPlanQcTypeRefService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 抽样方案质检类型关联表 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2026-03-20
 */
@Slf4j
@Service
public class QcSamplingPlanQcTypeRefServiceImpl extends SuperServiceImpl<QcSamplingPlanQcTypeRefMapper, QcSamplingPlanQcTypeRefEntity> implements QcSamplingPlanQcTypeRefService {
    @Resource
    private OperateLogService operateLogService;

    @Override
    public List<QcSamplingPlanQcTypeRefEntity> listByMainId(String id) {
        if (CharSequenceUtil.isBlank(id)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().eq(QcSamplingPlanQcTypeRefEntity::getMainId, id).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDetail(List<QcSamplingPlanQcTypeRefEntity> qcTypeList, QcSamplingPlanEntity qcSamplingPlanEntity) {
        List<QcSamplingPlanQcTypeRefEntity> oldList = this.listByMainId(qcSamplingPlanEntity.getId());
        if (CollUtil.isNotEmpty(oldList)) {
            this.removeByMainId(qcSamplingPlanEntity.getId(), oldList.stream().map(QcSamplingPlanQcTypeRefEntity::getId).filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList()));
        }
        //填充信息
        if (CollUtil.isEmpty(qcTypeList)) {
            return;
        }
        qcTypeList.forEach(detail -> {
            detail.setMainId(qcSamplingPlanEntity.getId());
        });
        this.saveOrUpdateBatch(qcTypeList);
    }

    @Override
    public void removeByMainId(String id) {
        this.removeByMainId(id, Collections.emptyList());
    }

    /**
     * 获取已启用的质检列表
     * @param qcType
     * @return
     */
    @Override
    public List<QcSamplingPlanQcTypeRefEntity> listByQcType(String qcType) {
        if (CharSequenceUtil.isBlank(qcType)){
            return Collections.emptyList();
        }
        return this.lambdaQuery()
                .eq(QcSamplingPlanQcTypeRefEntity::getQcType,qcType)
                .eq(QcSamplingPlanQcTypeRefEntity::getDisabled, Boolean.FALSE).list();
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeByMainId(String mainId, List<String> idList) {
        this.lambdaUpdate().eq(QcSamplingPlanQcTypeRefEntity::getMainId, mainId)
                .notIn(CollUtil.isNotEmpty(idList), QcSamplingPlanQcTypeRefEntity::getId, idList).remove();
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(List<QcSamplingPlanQcTypeRefEntity> qcSamplingPlanQcTypeRefEntityList, QcSamplingPlanEntity qcSamplingPlanEntity) {
        // TODO 验证数据 & 数据赋值
        qcSamplingPlanQcTypeRefEntityList.forEach(entity -> {
            entity.setMainId(qcSamplingPlanEntity.getId());
        });
        super.saveOrUpdateBatch(qcSamplingPlanQcTypeRefEntityList);
    }
}
