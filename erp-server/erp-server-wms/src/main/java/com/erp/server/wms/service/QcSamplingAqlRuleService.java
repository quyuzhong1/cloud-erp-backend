package com.erp.server.wms.service;
import com.erp.model.wms.dto.AqlSamplingRequest;
import com.erp.model.wms.dto.AqlSamplingResponse;
import com.erp.model.wms.entity.QcSamplingAqlRuleEntity;
import com.common.business.service.SuperService;

/**
 * <p>
 * GB/T2828.1-2012 AQL判定数主表 服务类
 * </p>
 *
 * @author zdy
 * @since 2026-03-19
 */
public interface QcSamplingAqlRuleService extends SuperService<QcSamplingAqlRuleEntity> {

    AqlSamplingResponse calculateSamplingPlan(AqlSamplingRequest request);
}
