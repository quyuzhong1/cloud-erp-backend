package com.erp.server.tms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.tms.dto.ExtendJsonDTO;
import com.erp.model.tms.dto.ShippingCalculationDTO;
import com.erp.model.tms.dto.ShippingTemplateDTO;
import com.erp.model.tms.entity.ShippingTemplateCostSettingEntity;
import com.erp.model.tms.entity.ShippingTemplateEntity;
import com.erp.model.tms.entity.ShippingTemplateOtherCostEntity;
import com.erp.model.tms.entity.ShippingTemplateRuleEntity;
import com.erp.model.tms.enums.ShippingBillingMethodEnum;
import com.erp.model.tms.enums.ShippingCostNameEnum;
import com.erp.model.tms.enums.ShippingSideEnum;
import com.erp.server.tms.mapper.ShippingTemplateOtherCostMapper;
import com.erp.server.tms.service.ShippingCalculationService;
import com.erp.server.tms.service.ShippingTemplateCostSettingService;
import com.erp.server.tms.service.ShippingTemplateOtherCostService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author Will
 * @version 1.0
 * @description: 运费计算实现
 * @date 2023/11/9 17:53
 */
@Service
@Slf4j
public class ShippingCalculationServiceImpl  implements ShippingCalculationService {

    @Resource
    private ShippingTemplateOtherCostService shippingTemplateOtherCostService;

    @Resource
    private ShippingTemplateCostSettingService shippingTemplateCostSettingService;

    @Resource
    private ShippingTemplateOtherCostMapper shippingTemplateOtherCostMapper;


    @Override
    public PagingVO<ShippingCalculationDTO.ListDTO> paging(PagingDTO<ShippingCalculationDTO.PagingParamDTO> pagingDTO) {
        ShippingCalculationDTO.PagingParamDTO params = pagingDTO.getParams();
        params.setPermissionSql(pagingDTO.getPermissionSql());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<ShippingCalculationDTO.ListDTO> pageData = this.shippingTemplateOtherCostMapper.paging(query, params);
        //清空明细数据
        List<ShippingCalculationDTO.ListDTO> records = pageData.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new PagingVO(pageData);
        }
        return new PagingVO(pageData);
    }

    @Override
    public Boolean exportExcel(ShippingCalculationDTO.PagingParamDTO params, HttpServletResponse response) {
        List<ShippingCalculationDTO.ListDTO> resultList = this.shippingTemplateOtherCostMapper.listByExportExcel(params);
        if (CollectionUtils.isEmpty(resultList)) {
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
        }
        String name = "运费计算列表";
        StringBuffer sb = new StringBuffer();
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        String excelPath = "excel/shippingCalculation.xlsx";
        try {
            new ExcelPrintUtils().patchExport(resultList, response, sb.toString(), excelPath);
        } catch (IOException e) {
            log.error("运费模板列表导出出错 >>>>>{}", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public  ShippingCalculationDTO.ViewDTO calculationFinalShippingCost(ShippingTemplateEntity entity, ShippingTemplateRuleEntity shippingTemplateRule
            , BigDecimal weight) {
        ShippingCalculationDTO.ViewDTO shippingCalculationDTO = new ShippingCalculationDTO.ViewDTO();

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
        BigDecimal discountCost = calculationDiscountCost(otherCostList, shippingCalculationDTO);
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
        Boolean isFlag = Boolean.TRUE;
        JSONObject jsonObject = JSONUtil.parseObj(otherCostEntity.getExtendJson());

        //最长边
        BigDecimal longestEdge = Arrays.asList(length, width, height).stream().max(Comparator.comparing(obj -> obj)).orElse(BigDecimal.ZERO);
        //最短边
        BigDecimal shortestEdge = Arrays.asList(length, width, height).stream().min(Comparator.comparing(obj -> obj)).orElse(BigDecimal.ZERO);
        //中间边长
        BigDecimal edge = Arrays.asList(length, width, height).stream().filter(obj -> MathUtil.compareTo(longestEdge, obj) > MathUtil.ZERO && MathUtil.compareTo(obj, shortestEdge) > MathUtil.ZERO).findFirst().orElse(null);
        //次边长
        BigDecimal minorEdge = ObjectUtil.isEmpty(edge) ? longestEdge : edge;
        //三边和
        BigDecimal edgelSum = Arrays.asList(length, width, height).stream().reduce(BigDecimal.ZERO,BigDecimal::add);

        for (ShippingTemplateCostSettingEntity costSettingEntity : costSettingList) {
            BigDecimal cost = (BigDecimal) jsonObject.get(costSettingEntity.getCode());

            if (ShippingSideEnum.LONGEST_EDGE.getCode().equals(costSettingEntity.getCode())) {
                isFlag =  MathUtil.compareTo(cost,longestEdge) > MathUtil.ZERO ? Boolean.FALSE :Boolean.TRUE;
                break;
            }
            if (ShippingSideEnum.MINOR_EDGE.getCode().equals(costSettingEntity.getCode())) {
                isFlag =  MathUtil.compareTo(cost,minorEdge) > MathUtil.ZERO ? Boolean.FALSE :Boolean.TRUE;
                break;
            }
            if (ShippingSideEnum.EDGEL_SUM.getCode().equals(costSettingEntity.getCode())) {
                isFlag =  MathUtil.compareTo(cost,edgelSum) > MathUtil.ZERO ? Boolean.FALSE :Boolean.TRUE;
                break;
            }
            if (ShippingSideEnum.ANY_EDGE.getCode().equals(costSettingEntity.getCode())) {
                isFlag =  (MathUtil.compareTo(cost,length) > MathUtil.ZERO
                            || MathUtil.compareTo(cost,width) > MathUtil.ZERO
                            || MathUtil.compareTo(cost,height) > MathUtil.ZERO) ? Boolean.FALSE :Boolean.TRUE;
                break;
            }
        }
        if (!isFlag) {
            return BigDecimal.ZERO;
        }
        //折扣费
        BigDecimal oversizeSurchargeCost = otherCostList.stream().filter(obj -> obj.getDictCode().equals(ShippingCostNameEnum.DISCOUNT_RATE.getCode()))
                .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCostSettingValue())).orElse(BigDecimal.ZERO);
        return oversizeSurchargeCost;
    }

    @Override
    public BigDecimal calculationFuelSurchargeCost(List<ShippingTemplateOtherCostEntity> otherCostList, ShippingCalculationDTO.ViewDTO shippingCalculationDTO) {
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
        //燃油附加费率
        BigDecimal fuelSurchargeCost = MathUtil.multiply(totalOtherCost, otherCostEntity.getCostSettingValue());

        return fuelSurchargeCost;
    }

    @Override
    public BigDecimal calculationDiscountCost(List<ShippingTemplateOtherCostEntity> otherCostList, ShippingCalculationDTO.ViewDTO shippingCalculationDTO) {
        if (CollectionUtils.isEmpty(otherCostList)) {
            return BigDecimal.ZERO;
        }
        //折扣费
        ShippingTemplateOtherCostEntity otherCostEntity = otherCostList.stream().filter(obj -> obj.getDictCode().equals(ShippingCostNameEnum.DISCOUNT_RATE.getCode()))
                .findFirst().orElse(new ShippingTemplateOtherCostEntity());
        //费用设置值
        List<ShippingTemplateCostSettingEntity> costSettingList = shippingTemplateCostSettingService.listByOtherCostIds(Arrays.asList(otherCostEntity.getId()));
        if (CollectionUtils.isNotEmpty(costSettingList)) {
            throw new ServiceException(ApiError.ERROR_SHIPPING_COST_SETTING_NOT_EXIST,ShippingCostNameEnum.DISCOUNT_RATE.getName());
        }
        //其他费用值JSON
        JSONObject jsonObject = JSONUtil.parseObj(shippingCalculationDTO);
        //费用合计值
        BigDecimal totalOtherCost = costSettingList.stream().map(obj -> (BigDecimal) jsonObject.get(obj.getCode())).reduce(BigDecimal.ZERO, BigDecimal::add);
        //折扣费
        BigDecimal discountCost = MathUtil.multiply(totalOtherCost, MathUtil.subtract(MathUtil.BigDecimal_1,otherCostEntity.getCostSettingValue()));

        return discountCost;
    }

}
