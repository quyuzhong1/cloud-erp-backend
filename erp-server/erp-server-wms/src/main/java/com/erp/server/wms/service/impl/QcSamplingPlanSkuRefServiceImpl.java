package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.SamplingPlanSkuRefDTO;
import com.erp.model.wms.entity.QcSamplingPlanEntity;
import com.erp.model.wms.entity.QcSamplingPlanSkuRefEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.QcSamplingPlanSkuRefMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.QcSamplingPlanSkuRefService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 抽样方案SKU白名单关联表 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2026-03-20
 */
@Slf4j
@Service
public class QcSamplingPlanSkuRefServiceImpl extends SuperServiceImpl<QcSamplingPlanSkuRefMapper, QcSamplingPlanSkuRefEntity> implements QcSamplingPlanSkuRefService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Override
    public List<QcSamplingPlanSkuRefEntity> listByMainId(String id) {
        if (CharSequenceUtil.isBlank(id)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().eq(QcSamplingPlanSkuRefEntity::getMainId, id).list();
    }

    @Override
    public List<QcSamplingPlanSkuRefEntity> listByMainIds(List<String> mainIds) {
        if (CollUtil.isEmpty(mainIds)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(QcSamplingPlanSkuRefEntity::getMainId, mainIds).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDetail(List<QcSamplingPlanSkuRefEntity> skuRefEntities, QcSamplingPlanEntity qcSamplingPlanEntity) {
        List<QcSamplingPlanSkuRefEntity> oldList = this.listByMainId(qcSamplingPlanEntity.getId());
        if (CollUtil.isNotEmpty(oldList)) {
            this.removeByMainId(qcSamplingPlanEntity.getId(), skuRefEntities.stream().map(QcSamplingPlanSkuRefEntity::getId).filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList()));
        }
        //填充信息
        if (CollUtil.isEmpty(skuRefEntities)) {
            return;
        }
        List<String> skuIds = skuRefEntities.stream().map(QcSamplingPlanSkuRefEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(skuIds);
        skuRefEntities.forEach(item -> {
            SkuVO skuVO = skuVOS.stream().filter(sku -> sku.getSkuId().equals(item.getSkuId())).findFirst().orElse(null);
            if (Objects.nonNull(skuVO)) {
                item.setSkuNo(skuVO.getSkuNo());
                item.setProductName(skuVO.getSkuName());
            }
            item.setMainId(qcSamplingPlanEntity.getId());
        });
        this.saveOrUpdateBatch(skuRefEntities);
    }

    @Override
    public void removeByMainId(String id) {
        this.removeByMainId(id, Collections.emptyList());
    }

    @Override
    public List<SamplingPlanSkuRefDTO.SkuDTO> listSkuByMainIds(List<String> ids) {
        return baseMapper.listSkuByMainIds(ids);
    }

    private void removeByMainId(String mainId, List<String> idList) {
        this.lambdaUpdate().eq(QcSamplingPlanSkuRefEntity::getMainId, mainId)
                .ne(CollUtil.isNotEmpty(idList), QcSamplingPlanSkuRefEntity::getId, idList).remove();
    }

}
