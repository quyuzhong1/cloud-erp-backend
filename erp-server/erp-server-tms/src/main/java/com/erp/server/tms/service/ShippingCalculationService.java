package com.erp.server.tms.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.ShippingCalculationDTO;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.ShippingTemplateEntity;
import com.erp.model.tms.entity.ShippingTemplateOtherCostEntity;
import com.erp.model.tms.entity.ShippingTemplateRuleEntity;

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
     * @description: 分页查询
     * @author Will
     * @date: 2023/11/10 17:36
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<ShippingCalculationDTO.ListDTO> paging(PagingDTO<ShippingCalculationDTO.PagingParamDTO> dto);
    /**
     * @param dto
     * @return Boolean
     * @description: 导出
     * @author Will
     * @date: 2023/11/10 17:36
     */
    Boolean exportExcel(ShippingCalculationDTO.PagingParamDTO dto);

    /**
     * @description: 最终运费
     * @author Will
     * @date: 2023/11/10 10:54
     * @param entity
     * @param shippingTemplateRule
     * @param weight
     * @return ShippingCalculationDTO
     */
    ShippingCalculationDTO.ViewDTO calculationFinalShippingCost(ShippingTemplateEntity entity, ShippingTemplateRuleEntity shippingTemplateRule
            , BigDecimal weight);

    /**
     * @description: 最终运费
     * @author Will
     * @date: 2024/1/20 15:31
     * @param entity
     * @param shippingTemplateRule
     * @param channelEntity
     * @param weight
     * @param length
     * @param width
     * @param height
     * @return ViewDTO
     */
    ShippingCalculationDTO.ViewDTO calculationFinalShippingCost(ShippingTemplateEntity entity, ShippingTemplateRuleEntity shippingTemplateRule, LogisticsChannelEntity channelEntity
            , BigDecimal weight, BigDecimal length, BigDecimal width, BigDecimal height);
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
    BigDecimal calculationSignatureCost(List<ShippingTemplateOtherCostEntity> otherCostList,Boolean isApiSign);
    /**
     * @description: 保险费
     * @author Will
     * @date: 2023/11/10 10:55
     * @param otherCostList
     * @return BigDecimal
     */
    BigDecimal calculationPremiumCost(List<ShippingTemplateOtherCostEntity> otherCostList,Boolean isApiInsurance);
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
            , ShippingCalculationDTO.ViewDTO shippingCalculationDTO);
    /**
     * @description: 折扣额
     * @author Will
     * @date: 2023/11/10 10:55
     * @param otherCostList
     * @param shippingCalculationDTO
     * @return BigDecimal
     */
    BigDecimal calculationDiscountCost(List<ShippingTemplateOtherCostEntity> otherCostList
            , ShippingCalculationDTO.ViewDTO shippingCalculationDTO);

    /**
     * @description: 查询分区城市
     * @author Will
     * @date: 2023/11/14 17:25
     * @param dto
     * @return List<String>
     */
    List<String> listRegionCity(ShippingCalculationDTO.ListRegionCityParamDTO dto);

    /**
     * orderId
     * @author yl
     * @date 2023-12-08 17:55
     * @param orderId b2c 销售订单id
     * @return 
     */
    ShippingCalculationDTO.CostCalculationResultDTO listChannelCost(String orderId);

    PagingVO<ShippingCalculationDTO.ListDTO> exportShippingCalculation(PagingDTO<ShippingCalculationDTO.PagingParamDTO> dto);
}
