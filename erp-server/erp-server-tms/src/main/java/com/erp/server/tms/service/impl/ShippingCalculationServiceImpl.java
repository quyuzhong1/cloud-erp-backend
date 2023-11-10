package com.erp.server.tms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.tms.dto.ExtendJsonDTO;
import com.erp.model.tms.dto.ShippingCalculationDTO;
import com.erp.model.tms.entity.ShippingTemplateCostSettingEntity;
import com.erp.model.tms.entity.ShippingTemplateEntity;
import com.erp.model.tms.entity.ShippingTemplateOtherCostEntity;
import com.erp.model.tms.entity.ShippingTemplateRuleEntity;
import com.erp.model.tms.enums.ShippingBillingMethodEnum;
import com.erp.model.tms.enums.ShippingCostNameEnum;
import com.erp.server.tms.service.ShippingCalculationService;
import com.erp.server.tms.service.ShippingTemplateCostSettingService;
import com.erp.server.tms.service.ShippingTemplateOtherCostService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author Will
 * @version 1.0
 * @description: 运费计算实现
 * @date 2023/11/9 17:53
 */
@Service
public class ShippingCalculationServiceImpl implements ShippingCalculationService {

    @Resource
    private ShippingTemplateOtherCostService shippingTemplateOtherCostService;

    @Resource
    private ShippingTemplateCostSettingService shippingTemplateCostSettingService;

    @Override
    public  ShippingCalculationDTO calculationFinalShippingCost(ShippingTemplateEntity entity, ShippingTemplateRuleEntity shippingTemplateRule
            , BigDecimal weight) {
        ShippingCalculationDTO shippingCalculationDTO = new ShippingCalculationDTO();

        //查询其他费用
        List<ShippingTemplateOtherCostEntity> otherCostList = shippingTemplateOtherCostService.listByMainId(entity.getId());
        if (CollectionUtils.isEmpty(otherCostList)) {
            throw new ServiceException(ApiError.ERROR_SHIPPING_OTHER_COST_NOT_EXIST);
        }
        //运费
        BigDecimal shippingCost = calculationShippingCost(entity, shippingTemplateRule, weight);
        shippingCalculationDTO.setShippingCost(shippingCost);
        //操作费
        shippingCalculationDTO.setOperatingCost(shippingTemplateRule.getOperatingCost());
        //挂号费
        shippingCalculationDTO.setRegistrationCost(shippingTemplateRule.getRegistrationCost());
        //签名费
        BigDecimal signatureCost = calculationSignatureCost(otherCostList);
        shippingCalculationDTO.setSignatureCost(signatureCost);
        //保险费
        BigDecimal premiumCost = calculationPremiumCost(otherCostList);
        shippingCalculationDTO.setPremiumCost(premiumCost);
        //超尺寸附加费
        BigDecimal oversizeSurchargeCost = calculationOversizeSurchargeCost(otherCostList,null,null,null);
        shippingCalculationDTO.setOversizeSurchargeCost(oversizeSurchargeCost);
        //燃油附加费
        BigDecimal fuelSurchargeCost = calculationFuelSurchargeCost(otherCostList,shippingCalculationDTO);
        shippingCalculationDTO.setFuelSurchargeCost(fuelSurchargeCost);
        //折扣费
        BigDecimal discountCost = calculationDiscountCost(otherCostList, shippingTemplateRule);
        shippingCalculationDTO.setDiscountCost(discountCost);
        /**
         * 最终运费 ：运费 + 挂号费 + 操作费 + 燃油附加费+其他费用【超尺寸+签名费+保险费】-折扣费
         */
        BigDecimal totalShippingCost = shippingCost
                .add(shippingTemplateRule.getOperatingCost())
                .add(shippingTemplateRule.getRegistrationCost())
                .add(signatureCost)
                .add(premiumCost)
                .add(oversizeSurchargeCost)
                .add(fuelSurchargeCost)
                .subtract(discountCost);
        shippingCalculationDTO.setTotalShippingCost(totalShippingCost);
        return shippingCalculationDTO;
    }


    /**
     * 运费
     */
    @Override
    public BigDecimal calculationShippingCost(ShippingTemplateEntity entity, ShippingTemplateRuleEntity shippingTemplateRule, BigDecimal weight) {

        /**
         *  最终运费 = 运费 + 挂号费 + 操作费 + 燃油附加费+其他费用【超尺寸+签名费+保险费】-折扣费
         *  运费【首重+续重】=首重费用+（收费重量-首重）/续重单位重量*单价【与最低收费对比，小于最低收费取值最低收费，大于最低收费则直接取值费用】
         *  （收费重量-首重）/续重单位重量 可能存在小数则直接进1取整数计算
         *  运费【重量段】=对应重量段的价格*收费重量【与最低收费对比，小于最低收费取值最低收费，大于最低收费则直接取值费用】
         */
        //运费
        BigDecimal shippingCost ;
        if (ShippingBillingMethodEnum.ENUM_SEVERAL_WEIGHT.getCode().equals(entity.getBillingMethod())) {
            //首重费用
            BigDecimal firstWeightShippingCost = shippingTemplateRule.getFirstWeightShippingCost();
            //续重比例（进一）
            BigDecimal weightRatio = MathUtil.divide(MathUtil.subtract(weight, shippingTemplateRule.getFirstWeight()), shippingTemplateRule.getAdditionalUnitWeight(),2,BigDecimal.ROUND_UP);
            //续重费用
            BigDecimal additionalWeightShippingCost = weightRatio.multiply(shippingTemplateRule.getAdditionalPrice());

            shippingCost = MathUtil.add(firstWeightShippingCost,additionalWeightShippingCost);
        } else {
            //验证录入重量是否在开始重量和结束重量之间
            if (MathUtil.compareTo(shippingTemplateRule.getStartWeight(),weight) >= MathUtil.ZERO || MathUtil.compareTo(weight, shippingTemplateRule.getEndWeight()) > MathUtil.ZERO) {
                throw new ServiceException(ApiError.ERROR_SHIPPING_WEIGHT_NOT_INTERVAL,weight,shippingTemplateRule.getStartWeight(),shippingTemplateRule.getEndWeight());
            }
            shippingCost = MathUtil.multiply(shippingTemplateRule.getShippingPrice(),weight);
        }
        shippingCost = MathUtil.compareTo(shippingCost,shippingTemplateRule.getMinCost()) > MathUtil.ZERO ? shippingCost : shippingTemplateRule.getMinCost();
        return shippingCost;
    }

    @Override
    public BigDecimal calculationSignatureCost(List<ShippingTemplateOtherCostEntity> otherCostList) {
        if (CollectionUtils.isEmpty(otherCostList)) {
            return BigDecimal.ZERO;
        }
         //签名费
        BigDecimal signatureCost = otherCostList.stream().filter(obj -> obj.getDictCode().equals(ShippingCostNameEnum.SIGNATURE_COST.getCode()))
                .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCostSettingValue())).orElse(BigDecimal.ZERO);
        return signatureCost;
    }

    @Override
    public BigDecimal calculationPremiumCost(List<ShippingTemplateOtherCostEntity> otherCostList) {
        if (CollectionUtils.isEmpty(otherCostList)) {
            return BigDecimal.ZERO;
        }
        //保险费
        BigDecimal premiumCost = otherCostList.stream().filter(obj -> obj.getDictCode().equals(ShippingCostNameEnum.PREMIUM_COST.getCode()))
                .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCostSettingValue())).orElse(BigDecimal.ZERO);
        return premiumCost;
    }

    @Override
    public BigDecimal calculationOversizeSurchargeCost(List<ShippingTemplateOtherCostEntity> otherCostList, BigDecimal length, BigDecimal width, BigDecimal height) {
        if (CollectionUtils.isEmpty(otherCostList)) {
            return BigDecimal.ZERO;
        }
        if (ObjectUtil.isEmpty(length) && ObjectUtil.isEmpty(width) && ObjectUtil.isEmpty(height)) {
            return BigDecimal.ZERO;
        }

        //超尺寸附加费
        ShippingTemplateOtherCostEntity otherCostEntity = otherCostList.stream().filter(obj -> obj.getDictCode().equals(ShippingCostNameEnum.OVERSIZE_SURCHARGE_COST.getCode()))
                .findFirst().orElse(new ShippingTemplateOtherCostEntity());
        //费用设置值
        List<ShippingTemplateCostSettingEntity> costSettingList = shippingTemplateCostSettingService.listByOtherCostIds(Arrays.asList(otherCostEntity.getId()));
        if (CollectionUtils.isNotEmpty(costSettingList)) {
            throw new ServiceException(ApiError.ERROR_SHIPPING_COST_SETTING_NOT_EXIST,ShippingCostNameEnum.OVERSIZE_SURCHARGE_COST.getName());
        }
        //是否符合条件
        Boolean isFlag = Boolean.FALSE;
        JSONObject jsonObject = JSONUtil.parseObj(otherCostEntity.getExtendJson());

        for (ShippingTemplateCostSettingEntity costSettingEntity : costSettingList) {
            BigDecimal cost = (BigDecimal) jsonObject.get(costSettingEntity.getCode());
        }

        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calculationFuelSurchargeCost(List<ShippingTemplateOtherCostEntity> otherCostList, ShippingCalculationDTO shippingCalculationDTO) {
        if (CollectionUtils.isEmpty(otherCostList)) {
            return BigDecimal.ZERO;
        }
        //燃油附加费
        ShippingTemplateOtherCostEntity otherCostEntity = otherCostList.stream().filter(obj -> obj.getDictCode().equals(ShippingCostNameEnum.FUEL_SURCHARGE_RATE.getCode()))
                .findFirst().orElse(new ShippingTemplateOtherCostEntity());
        //费用设置值
        List<ShippingTemplateCostSettingEntity> costSettingList = shippingTemplateCostSettingService.listByOtherCostIds(Arrays.asList(otherCostEntity.getId()));
        if (CollectionUtils.isNotEmpty(costSettingList)) {
            throw new ServiceException(ApiError.ERROR_SHIPPING_COST_SETTING_NOT_EXIST,ShippingCostNameEnum.FUEL_SURCHARGE_RATE.getName());
        }
        //其他费用值JSON
        JSONObject jsonObject = JSONUtil.parseObj(shippingCalculationDTO);
        //费用合计值
        BigDecimal totalOtherCost = costSettingList.stream().map(obj -> (BigDecimal) jsonObject.get(obj.getCode())).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal fuelSurchargeCost = MathUtil.multiply(totalOtherCost, MathUtil.subtract(MathUtil.BigDecimal_1,otherCostEntity.getCostSettingValue()));

        return fuelSurchargeCost;
    }

    @Override
    public BigDecimal calculationDiscountCost(List<ShippingTemplateOtherCostEntity> otherCostList, ShippingTemplateRuleEntity shippingTemplateRule) {

        return BigDecimal.ZERO;
    }


}
