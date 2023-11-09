package com.erp.server.tms.service;

import com.erp.model.tms.dto.ShippingCalculationDTO;
import com.erp.model.tms.entity.ShippingTemplateEntity;
import com.erp.model.tms.entity.ShippingTemplateRuleEntity;

import java.math.BigDecimal;

/**
 * @author Will
 * @version 1.0
 * @description: 运费计算接口
 * @date 2023/11/9 17:53
 */
public interface ShippingCalculationService {

    /**
     * @description: 计算最终运费
     * @author Will
     * @date: 2023/11/9 17:56
     * @param entity
     * @param shippingTemplateRule
     * @param weight
     * @return ShippingCalculationDTO
     */
    ShippingCalculationDTO calculationFinalShippingCost (ShippingTemplateEntity entity, ShippingTemplateRuleEntity shippingTemplateRule, BigDecimal weight);
}
