package com.erp.server.tms.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.tms.dto.ShippingCalculationDTO;
import com.erp.model.tms.dto.ShippingTemplateDTO;
import com.erp.model.tms.entity.ShippingTemplateEntity;
import com.erp.model.tms.entity.ShippingTemplateOtherCostEntity;
import com.erp.model.tms.entity.ShippingTemplateRuleEntity;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: 运费计算接口
 * @date 2023/11/9 17:53
 */
public interface ShippingCalculationService {

    /**
     * @description: 最终运费
     * @author Will
     * @date: 2023/11/10 10:54
     * @param entity
     * @param shippingTemplateRule
     * @param weight
     * @return ShippingCalculationDTO
     */
    ShippingCalculationDTO calculationFinalShippingCost(ShippingTemplateEntity entity, ShippingTemplateRuleEntity shippingTemplateRule
            , BigDecimal weight);
    /**
     * @description: 运费
     * @author Will
     * @date: 2023/11/10 10:55
     * @param entity
     * @param shippingTemplateRule
     * @param weight
     * @return BigDecimal
     */
    BigDecimal calculationShippingCost(ShippingTemplateEntity entity, ShippingTemplateRuleEntity shippingTemplateRule, BigDecimal weight);
    /**
     * @description: 签名费
     * @author Will
     * @date: 2023/11/10 10:55
     * @param otherCostList
     * @return BigDecimal
     */
    BigDecimal calculationSignatureCost(List<ShippingTemplateOtherCostEntity> otherCostList);
    /**
     * @description: 保险费
     * @author Will
     * @date: 2023/11/10 10:55
     * @param otherCostList
     * @return BigDecimal
     */
    BigDecimal calculationPremiumCost(List<ShippingTemplateOtherCostEntity> otherCostList);
    /**
     * @description: 超尺寸附加费
     * @author Will
     * @date: 2023/11/10 10:55
     * @param otherCostList
     * @param length
     * @param width
     * @param height
     * @return BigDecimal
     */
    BigDecimal calculationOversizeSurchargeCost(List<ShippingTemplateOtherCostEntity> otherCostList
            , BigDecimal length, BigDecimal width, BigDecimal height);
    /**
     * @description: 燃油附加费
     * @author Will
     * @date: 2023/11/10 10:55
     * @param otherCostList
     * @param shippingCalculationDTO
     * @return BigDecimal
     */
    BigDecimal calculationFuelSurchargeCost(List<ShippingTemplateOtherCostEntity> otherCostList
            , ShippingCalculationDTO shippingCalculationDTO);
    /**
     * @description: 折扣额
     * @author Will
     * @date: 2023/11/10 10:55
     * @param otherCostList
     * @param shippingCalculationDTO
     * @return BigDecimal
     */
    BigDecimal calculationDiscountCost(List<ShippingTemplateOtherCostEntity> otherCostList
            , ShippingCalculationDTO shippingCalculationDTO);

    PagingVO<ShippingTemplateDTO.ListDTO> paging(PagingDTO<ShippingTemplateDTO.PagingParamDTO> dto);
}
