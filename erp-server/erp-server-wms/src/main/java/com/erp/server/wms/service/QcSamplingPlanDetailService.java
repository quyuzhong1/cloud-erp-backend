package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.entity.QcSamplingPlanDetailEntity;
import com.erp.model.wms.entity.QcSamplingPlanEntity;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author zdy
 * @since 2026-03-20
 */
public interface QcSamplingPlanDetailService extends SuperService<QcSamplingPlanDetailEntity> {

    List<QcSamplingPlanDetailEntity> listByMainId(String id);

    void updateDetail(List<QcSamplingPlanDetailEntity> detailList, QcSamplingPlanEntity qcSamplingPlanEntity);
}
