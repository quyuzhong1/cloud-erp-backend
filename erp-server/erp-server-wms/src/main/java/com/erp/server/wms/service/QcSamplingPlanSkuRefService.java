package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.SamplingPlanSkuRefDTO;
import com.erp.model.wms.entity.QcSamplingPlanEntity;
import com.erp.model.wms.entity.QcSamplingPlanSkuRefEntity;

import java.util.List;

/**
 * <p>
 * 抽样方案SKU白名单关联表 服务类
 * </p>
 *
 * @author zdy
 * @since 2026-03-20
 */
public interface QcSamplingPlanSkuRefService extends SuperService<QcSamplingPlanSkuRefEntity> {

    List<QcSamplingPlanSkuRefEntity> listByMainId(String id);

    List<QcSamplingPlanSkuRefEntity> listByMainIds(List<String> mainIds);

    void updateDetail(List<QcSamplingPlanSkuRefEntity> skuRefDTOList, QcSamplingPlanEntity qcSamplingPlanEntity);

    void removeByMainId(String id);

    List<SamplingPlanSkuRefDTO.SkuDTO> listSkuByMainIds(List<String> ids);
}
