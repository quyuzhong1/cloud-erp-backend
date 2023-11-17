package com.erp.server.tms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
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
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.tms.dto.ExtendJsonDTO;
import com.erp.model.tms.dto.ShippingCalculationDTO;
import com.erp.model.tms.dto.ShippingTemplateDTO;
import com.erp.model.tms.entity.ShippingTemplateCostSettingEntity;
import com.erp.model.tms.entity.ShippingTemplateEntity;
import com.erp.model.tms.entity.ShippingTemplateOtherCostEntity;
import com.erp.model.tms.entity.ShippingTemplateRuleEntity;
import com.erp.model.tms.enums.*;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.tms.mapper.ShippingRegionCityMapper;
import com.erp.server.tms.mapper.ShippingTemplateMapper;
import com.erp.server.tms.mapper.ShippingTemplateOtherCostMapper;
import com.erp.server.tms.service.DictBasicService;
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
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

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

    @Resource
    private ShippingRegionCityMapper shippingRegionCityMapper;

    @Resource
    private SysUserFeign sysUserFeign;

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
        //处理数据
        handleData(records,params);
        return new PagingVO(pageData);
    }

    private void handleData(List<ShippingCalculationDTO.ListDTO> records,ShippingCalculationDTO.PagingParamDTO params) {
        //币别
        List<String> currencyIdList = records.stream().map(ShippingCalculationDTO.ListDTO::getCurrency).collect(Collectors.toList());
        List<CurrencyDTO.ViewDTO>  currencyList = sysUserFeign.listByCurrency(currencyIdList);

        //其他费用
        List<String> templateIdList = records.stream().map(obj -> obj.getTemplateEntity().getId()).distinct().collect(Collectors.toList());
        List<ShippingTemplateOtherCostEntity> otherCostList = shippingTemplateOtherCostService.listByMainIds(templateIdList);

        for (ShippingCalculationDTO.ListDTO listDTO : records) {
            //币种符号
            String currencySymbol = currencyList.stream().filter(obj -> obj.getId().equals(listDTO.getCurrency())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getSymbol())).orElse("");
            listDTO.setCurrencySymbol(currencySymbol);

            //有效期
            String effectivePeriod = StrUtil.format("{}至{}",listDTO.getEffectiveDate(),listDTO.getExpireDate());
            listDTO.setEffectivePeriod(effectivePeriod);

            /**
             * 体积重=长*宽*高/材积设置
             * 若渠道为计费重：则取值实际重量和体积重量最大值作为计费重量
             * 若渠道为实际重：则取值实际重量作为计算重量
             * 若渠道为体积重：则取值体积重量作为计费重量
             */
            //体积重
            BigDecimal volumeWeight = MathUtil.divide(MathUtil.multiply(MathUtil.multiply(params.getLength(),params.getWeight()),params.getHeight()),new BigDecimal(listDTO.getVolumeSetting()));
            //重量
            BigDecimal weight = params.getWeight();
            if (ShippingFeeRuleEnum.BILLING_WEIGHT.getCode().equals(listDTO.getFeeRule())) {
                weight = MathUtil.compareTo(volumeWeight, params.getWeight()) > MathUtil.ZERO ? volumeWeight : params.getWeight();
            }
            if (ShippingFeeRuleEnum.VOLUME_WEIGHT.getCode().equals(listDTO.getFeeRule())) {
                weight = volumeWeight;
            }

            //重量单位比例
            BigDecimal ratio = BigDecimal.ONE;
            if (!StrUtil.equals(params.getWeightUnit(),listDTO.getWeightUnit())) {
                if ("kg".equals(params.getWeightUnit())) {
                    //kg
                    ratio = new BigDecimal(1000);
                } else{
                    //g
                    ratio = new BigDecimal(0.001);
                }
            }
            //其他费用
            List<ShippingTemplateOtherCostEntity> costEntityList = otherCostList.stream().filter(obj -> obj.getMainId().equals(listDTO.getTemplateEntity().getId())).collect(Collectors.toList());
            ShippingCalculationDTO.ViewDTO shippingCalculationDTO = calculationFinalShippingCost(listDTO.getTemplateEntity(), listDTO.getTemplateRuleEntity(), costEntityList
                    , weight, params.getLength(), params.getWidth(), params.getHeight());
            listDTO.setShippingCost(MathUtil.multiply(shippingCalculationDTO.getShippingCost(),ratio));
            listDTO.setRegistrationCost(MathUtil.multiply(shippingCalculationDTO.getRegistrationCost(),ratio));
            listDTO.setOperatingCost(MathUtil.multiply(shippingCalculationDTO.getOperatingCost(),ratio));
            listDTO.setTotalShippingCost(MathUtil.multiply(shippingCalculationDTO.getTotalShippingCost(),ratio));
            //其他费用
            ShippingCalculationDTO.OtherCostDTO otherCostDTO = new ShippingCalculationDTO.OtherCostDTO();
            otherCostDTO.setDiscountCost(MathUtil.multiply(shippingCalculationDTO.getDiscountCost(),ratio));
            otherCostDTO.setPremiumCost(MathUtil.multiply(shippingCalculationDTO.getPremiumCost(),ratio));
            otherCostDTO.setSignatureCost(MathUtil.multiply(shippingCalculationDTO.getSignatureCost(),ratio));
            otherCostDTO.setOversizeSurchargeCost(MathUtil.multiply(shippingCalculationDTO.getOversizeSurchargeCost(),ratio));
            otherCostDTO.setFuelSurchargeCost(MathUtil.multiply(shippingCalculationDTO.getFuelSurchargeCost(),ratio));
            listDTO.setOtherCostDTO(otherCostDTO);
            //其他费用字符串
            String otherCostStr = JSONUtil.parseObj(otherCostDTO).entrySet().stream().filter(obj -> MathUtil.compareTo(obj.getValue(), MathUtil.ZERO) > MathUtil.ZERO).map(obj -> ShippingOtherCostNameEnum.getName(obj.getKey()).concat(":").concat(obj.getValue().toString())).collect(Collectors.joining(";"));
            listDTO.setOtherCostStr(otherCostStr);
        }
    }

    @Override
    public Boolean exportExcel(ShippingCalculationDTO.PagingParamDTO params, HttpServletResponse response) {
        List<ShippingCalculationDTO.ListDTO> resultList = this.shippingTemplateOtherCostMapper.listByExportExcel(params);
        if (CollectionUtils.isEmpty(resultList)) {
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
        }
        //处理数据
        handleData(resultList,params);
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


        //查询其他费用
        List<ShippingTemplateOtherCostEntity> otherCostList = shippingTemplateOtherCostService.listByMainId(entity.getId());
        if (CollectionUtils.isEmpty(otherCostList)) {
            throw new ServiceException(ApiError.ERROR_SHIPPING_OTHER_COST_NOT_EXIST);
        }
        ShippingCalculationDTO.ViewDTO shippingCalculationDTO = calculationFinalShippingCost(entity, shippingTemplateRule, otherCostList, weight, null, null, null);
        return shippingCalculationDTO;
    }

    /**
     * @description: 费用计算
     * @author Will
     * @date: 2023/11/16 16:54
     * @param entity
     * @param shippingTemplateRule
     * @param otherCostList
     * @param weight
     * @param length
     * @param width
     * @param height
     * @return ViewDTO
     */
    private ShippingCalculationDTO.ViewDTO calculationFinalShippingCost (ShippingTemplateEntity entity, ShippingTemplateRuleEntity shippingTemplateRule
            ,List<ShippingTemplateOtherCostEntity> otherCostList, BigDecimal weight,BigDecimal length,BigDecimal width,BigDecimal height) {

        ShippingCalculationDTO.ViewDTO shippingCalculationDTO = new ShippingCalculationDTO.ViewDTO();
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
        BigDecimal oversizeSurchargeCost = calculationOversizeSurchargeCost(otherCostList,length,width,height);
        shippingCalculationDTO.setOversizeSurchargeCost(oversizeSurchargeCost);
        //燃油附加费
        BigDecimal fuelSurchargeCost = calculationFuelSurchargeCost(otherCostList,shippingCalculationDTO);
        shippingCalculationDTO.setFuelSurchargeCost(fuelSurchargeCost);
        //折扣费
        BigDecimal discountCost = calculationDiscountCost(otherCostList, shippingCalculationDTO);
        shippingCalculationDTO.setDiscountCost(discountCost);
        /**
         * 最终运费（运费计算） ：运费 + 挂号费 + 操作费 + 燃油附加费+其他费用【超尺寸+签名费+保险费】-折扣费
         * 价格进制进行处理
         */
        BigDecimal totalShippingCost = shippingCost
                .add(shippingTemplateRule.getOperatingCost())
                .add(shippingTemplateRule.getRegistrationCost())
                .add(signatureCost)
                .add(premiumCost)
                .add(oversizeSurchargeCost)
                .add(fuelSurchargeCost)
                .subtract(discountCost);
        shippingCalculationDTO.setTotalShippingCost(handlePriceBinary(totalShippingCost,entity.getPriceBinary()));

        /**
         * 最终运费（运费试算） ：运费+挂号费+操作费
         * 价格进制进行处理
         */
        BigDecimal totalTrialShippingCost = shippingCost
                .add(shippingTemplateRule.getOperatingCost())
                .add(shippingTemplateRule.getRegistrationCost());
        shippingCalculationDTO.setTotalTrialShippingCost(handlePriceBinary(totalTrialShippingCost,entity.getPriceBinary()));
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
            //续重费用
            BigDecimal additionalWeightShippingCost = BigDecimal.ZERO;
            if (MathUtil.compareTo(weight,shippingTemplateRule.getFirstWeight()) > MathUtil.ZERO) {
                //续重比例（进一）
                BigDecimal weightRatio = MathUtil.divide(MathUtil.subtract(weight, shippingTemplateRule.getFirstWeight()), shippingTemplateRule.getAdditionalUnitWeight(),0,BigDecimal.ROUND_UP);
                //续重费用
                additionalWeightShippingCost = weightRatio.multiply(shippingTemplateRule.getAdditionalPrice());
            }
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
            return BigDecimal.ZERO;
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
        if (CollectionUtils.isEmpty(costSettingList)) {
            return BigDecimal.ZERO;
        }
        //其他费用值JSON
        JSONObject jsonObject = JSONUtil.parseObj(shippingCalculationDTO);
        //费用合计值
        BigDecimal totalOtherCost = costSettingList.stream().map(obj -> (BigDecimal) jsonObject.get(obj.getCode())).reduce(BigDecimal.ZERO, BigDecimal::add);
        //燃油附加费率
        BigDecimal fuelSurchargeCost = MathUtil.multiply(totalOtherCost, MathUtil.divide(otherCostEntity.getCostSettingValue(),MathUtil.BigDecimal_100) );

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
        if (CollectionUtils.isEmpty(costSettingList)) {
            return BigDecimal.ZERO;
        }
        //其他费用值JSON
        JSONObject jsonObject = JSONUtil.parseObj(shippingCalculationDTO);
        //费用合计值
        BigDecimal totalOtherCost = costSettingList.stream().map(obj -> (BigDecimal) jsonObject.get(obj.getCode())).reduce(BigDecimal.ZERO, BigDecimal::add);
        //折扣费
        BigDecimal discountCost = MathUtil.multiply(totalOtherCost, MathUtil.subtract(MathUtil.BigDecimal_1,MathUtil.divide(otherCostEntity.getCostSettingValue(),MathUtil.BigDecimal_100)));

        return discountCost;
    }

    @Override
    public List<String> listRegionCity(ShippingCalculationDTO.ListRegionCityParamDTO dto) {
        return shippingRegionCityMapper.listRegionCity(dto);
    }

    /**
     * @description: 价格进制调整
     * @author Will
     * @date: 2023/11/17 9:17
     * @param cost
     * @param priceBinary
     * @return BigDecimal
     */
    private BigDecimal handlePriceBinary (BigDecimal cost,String priceBinary) {

        //保留两位小数四金五入
        if (PriceBinaryEnum.TWO_DECIMAL_PLACES.getCode().equals(priceBinary)) {
            cost = cost.setScale(2);
        }
        //保留一位小数四舍五入
        if (PriceBinaryEnum.ONE_DECIMAL_PLACES.getCode().equals(priceBinary)) {
            cost = cost.setScale(1);
        }
        //向下取整，小数舍弃
        if (PriceBinaryEnum.NO_DECIMALS.getCode().equals(priceBinary)) {
            cost = cost.setScale(0,BigDecimal.ROUND_DOWN);
        }
        //0.5进制
        if (PriceBinaryEnum.BINARY.getCode().equals(priceBinary)) {
            BigDecimal integerPart = new BigDecimal(cost.intValue());
            BigDecimal decimalPart = cost.subtract(integerPart);
            if (MathUtil.compareTo(decimalPart,0.5) > MathUtil.ZERO ) {
                //超过0.5，进1
                cost = cost.setScale(0,BigDecimal.ROUND_UP);
            }
            if (MathUtil.compareTo(decimalPart,0) > MathUtil.ZERO && MathUtil.compareTo(decimalPart,0.5) < MathUtil.ZERO ){
                //未到0.5，进0.5
                cost = cost.setScale(0,BigDecimal.ROUND_UP);
            }
        }
        //向上取整，小数进1
        if (PriceBinaryEnum.ROUND_UP.getCode().equals(priceBinary)) {
            cost = cost.setScale(0,BigDecimal.ROUND_UP);
        }
        //保留整数，四舍五入
        if (PriceBinaryEnum.PRESERVE_INTEGERS.getCode().equals(priceBinary)) {
            cost = cost.setScale(0);

        }
        return  cost;
    }

}
