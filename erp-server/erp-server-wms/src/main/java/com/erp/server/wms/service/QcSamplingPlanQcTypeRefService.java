package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.entity.QcSamplingPlanEntity;
import com.erp.model.wms.entity.QcSamplingPlanQcTypeRefEntity;

import java.util.List;

/**
 * <p>
 * 抽样方案质检类型关联表 服务类
 * </p>
 *
 * @author zdy
 * @since 2026-03-20
 */
public interface QcSamplingPlanQcTypeRefService extends SuperService<QcSamplingPlanQcTypeRefEntity> {


    List<QcSamplingPlanQcTypeRefEntity> listByMainId(String id);

    void updateDetail(List<QcSamplingPlanQcTypeRefEntity> qcTypeList, QcSamplingPlanEntity qcSamplingPlanEntity);
}
