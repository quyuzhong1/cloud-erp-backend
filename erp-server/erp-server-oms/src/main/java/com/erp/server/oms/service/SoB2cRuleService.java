package com.erp.server.oms.service;

import com.common.business.service.SuperService;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.SoB2cEntity;

/**
 * <p>
 * B2C销售订单表 规则处理类
 * </p>
 *
 */
public interface SoB2cRuleService extends SuperService<SoB2cEntity> {

    boolean handleAutoSubmitDelivery(String soId, String name);

    boolean handleAutoLogisticsAction(String soId, SoB2cDTO.RuleResultDTO logisticsRuleResult);
}
