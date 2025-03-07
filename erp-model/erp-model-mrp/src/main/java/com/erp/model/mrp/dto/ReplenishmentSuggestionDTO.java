package com.erp.model.mrp.dto;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.dto.base.SortParamDTO;
import com.erp.model.mrp.entity.SalesEstimateEntity;
import com.erp.model.mrp.entity.SalesInfoEntity;
import com.erp.model.mrp.enums.*;
import com.erp.model.mrp.vo.ReplenishmentSuggestionVO;
import lombok.*;
import org.springframework.util.ObjectUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ReplenishmentSuggestionDTO implements Serializable {


    private static final long serialVersionUID = 7176520727559717436L;

    @Getter
    @Setter
    @EqualsAndHashCode(callSuper = true)
    public static class PagingParamDTO extends SortDTO {

        private static final long serialVersionUID = 3607402030033334190L;
        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String, String> sqlMap;
        /**
         * 建议类型
         */
        @NotBlank(message = "建议类型不能为空")
        private String platformType;
        /**
         * sku类型
         */
        private String skuType;
        /**
         * 是否关注
         */
        private Boolean favorite;
        /**
         * 补货类型
         */
        private String replenishmentType;

        /**
         * 最近日期排序
         */
        private List<SortParamDTO> recentSort;

        /**
         * 历史销量排序
         */
        private SortParamDTO realSaleQtySort;
        /**
         * 备货期日均排序
         */
        private SortParamDTO avgStockingSort;

        /**
         * 备货期排序
         */
        private SortParamDTO stockingSort;
    }


    /**
     * 暂不补货/恢复补货
     */
    @Data
    @NoArgsConstructor
    public static class ReplenishmentDTO {
        /**
         * 主键ids
         */
        @NotEmpty(message = "主键ids不能为空")
        private List<String> ids;

        /**
         * 补货原因
         */
        @NotBlank(message = "原因说明不能为空")
        private String replenishmentRemark;
    }

    /**
     * 批量设置规则参数
     */
    @Data
    @NoArgsConstructor
    public static class UpdateRuleDTO {

        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 备货设置
         */
        @Valid
        private CfgRuleStockUpDTO.UpdateDTO stockUpUpdateDTO;

        /**
         * 销量设置
         */
//        @Valid
//        private CfgRuleSalesQtyDTO.UpdateDetailDTO salesQtyUpdateDTO;

    }

    /**
     * 批量设置规则参数
     */
    @Data
    @NoArgsConstructor
    public static class BatchUpdateRuleDTO {

        /**
         * 是否批量
         */
        private Boolean isBatch = false;

        /**
         * 主键ids
         */
        @NotEmpty(message = "主键ids不能为空")
        private List<String> ids;

        /**
         * 备货设置
         */
        @Valid
        private CfgRuleStockUpDTO.CustomUpdateDTO stockUpUpdateDTO;

        /**
         * 销量设置
         */
        @Valid
        private CfgRuleSalesQtyDTO.UpdateDTO salesQtyUpdateDTO;

    }

    /**
     * 恢复规则
     */
    @Data
    @NoArgsConstructor
    public static class RestoreRuleDTO {

        /**
         * 主键ids
         */
        @NotEmpty(message = "主键ids不能为空")
        private List<String> ids;

        /**
         * 规则选项
         */
        private List<String> ruleTypeList;
    }


    @Data
    @NoArgsConstructor
    public static class UpdateLabelDTO {
        /**
         * 主键id
         */
        private String id;

        /**
         * 标签选项
         */
        private List<String> labelIdList;

    }

    @Data
    @NoArgsConstructor
    public static class BatchSaveLabelDTO {
        /**
         * 主键ids
         */
        @NotEmpty(message = "主键ids不能为空")
        private List<String> ids;

        /**
         * 标签选项
         */
        private List<String> labelIdList;

    }

    @Getter
    @Setter
    public static class DetailParamDTO {

        /**
         * 明细id
         */
        @NotBlank(message = "明细id不能为空")
        private String detailId;

        /**
         * 类型
         * @see ReplenishmentInventoryTypeEnum
         */
        private String type;
        /**
         * 来源类型
         */
        private String sourceType;
        /**
         * 预计可售日期
         */
        private LocalDate estimateSalesDate;

        /**
         * 店铺id
         */
        @NotBlank(message = "店铺不能为空")
        private String shopId;
    }

    @Data
    @NoArgsConstructor
    public static class DeliverySuggestionDTO {

        /**
         * 类型
         */
        private String platformType;
        /**
         * 类型名称
         */
        private String platformTypeName;
        /**
         * 平台
         */
        private String platform;
        /**
         * 平台名称
         */
        private String platformName;
        /**
         * 店铺id
         */
        private String shopId;

        /**
         * 店铺
         */
        private String shopName;

        /**
         * 单号
         */
        private String code;

        /**
         * 数据类型
         */
        private String dataType;
        /**
         * 数据类型名称
         */
        private String dataTypeName;
        /**
         * SKUID
         */
        private String skuId;

        /**
         * SKU
         */
        private String skuNo;

        /**
         * 品名
         */
        private String productName;

        /**
         * 建议发货量
         */
        private Integer suggestDeliveryQty;

        /**
         * 物流方式（系统）
         */
        private String logisticsMethod;
        /**
         * 物流方式名称（系统）
         */
        private String logisticsMethodName;

        /**
         * 建议发货日期（系统）
         */
        private LocalDate suggestDeliveryDate;

        /**
         * 预计可售日期（系统）
         */
        private LocalDate estimateSalesDate;

        /**
         * 物流成本
         */
        private BigDecimal logisticsCost;

        /**
         * 创建人
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private String createTime;

        /**
         * 更新人
         */
        private String updateUserName;

        /**
         * 更新时间
         */
        private String updateTime;
    }

    @Data
    @NoArgsConstructor
    public static class PurchaseSuggestionDTO {

        /**
         * 类型
         */
        private String platformType;
        /**
         * 类型名称
         */
        private String platformTypeName;
        /**
         * 平台
         */
        private String platform;
        /**
         * 平台名称
         */
        private String platformName;
        /**
         * 店铺id
         */
        private String shopId;

        /**
         * 店铺
         */
        private String shopName;

        /**
         * 单号
         */
        private String code;

        /**
         * 数据类型
         */
        private String dataType;
        /**
         * 数据类型名称
         */
        private String dataTypeName;
        /**
         * SKUID
         */
        private String skuId;

        /**
         * SKU
         */
        private String skuNo;

        /**
         * 品名
         */
        private String productName;

        /**
         * 系统建议值
         */
        private Integer suggestPurchaseQty;
        /**
         * 物流方式（系统）
         */
        private String logisticsMethod;
        /**
         * 物流方式名称（系统）
         */
        private String logisticsMethodName;

        /**
         * 建议采购日期（系统）
         */
        private LocalDate suggestPurchaseDate;

        /**
         * 预计入库日期（系统）
         */
        private LocalDate estimateInstockDate;

        /**
         * 预计可售日期（系统）
         */
        private LocalDate estimateSalesDate;

        /**
         * 预计采购成本（系统）
         */
        private BigDecimal purchaseCost;

        /**
         * 创建人
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private String createTime;

        /**
         * 更新人
         */
        private String updateUserName;

        /**
         * 更新时间
         */
        private String updateTime;
    }

    @Data
    @NoArgsConstructor
    public static class ReplenishmentRuleExportDTO {
        /**
         * 备货导出
         */
        private List<CfgRuleStockUpDTO.StockUpExportDTO> stockUpExportList;

        /**
         * 动态备货系数导出
         */
        private List<CfgRuleStockingRatioDTO.StockingRatioExportDTO> stockingRatioExportList;

        /**
         * 默认日销量导出
         */
        private List<CfgRuleSalesFormulaDTO.SalesFormulaExportDTO> defaultSalesQtyExportList;

        /**
         * 动态日销量导出
         */
        private List<CfgRuleSalesFormulaDTO.SalesFormulaExportDTO> dynamicSalesQtyExportList;

        /**
         * 固定日销量导出
         */
        private List<CfgRuleSalesFormulaDTO.SalesFormulaExportDTO> fixedSalesQtyExportList;

        /**
         * 销量去噪导出
         */
        private List<CfgRuleSalesDenoisingDTO.SalesDenoisingExportDTO> salesDenoisingExportList;
    }


    @Getter
    @Setter
    public static class SalesDTO {

        private String skuId;

        private String shopId;

        private String originalSalesQty;
    }

    @Getter
    @Setter
    public static class SalesEstimateExportDTO {
        /**
         * 平台
         */
        private String platform;
        /**
         * 平台名称
         */
        private String platformName;
        /**
         * 店铺id
         */
        private String shopId;

        /**
         * 店铺
         */
        private String shopName;

        /**
         * SKU ID
         */
        private String skuId;
        /**
         * SKU
         */
        private String skuNo;

        /**
         * 日期
         */
        private LocalDate date;

        /**
         * 预估日销量类型
         */
        private String salesInfoEstimateType;

        /**
         * 规则
         */
        private String rule;
        /**
         * 预估销量
         */
        private BigDecimal estimateQty;
        /**
         * 近三日销量
         */
        private BigDecimal avgThreeSalesQty;
        /**
         * 近七日销量
         */
        private BigDecimal avgSevenSalesQty;
        /**
         * 近十四日销量
         */
        private BigDecimal avgFourteenSalesQty;
        /**
         * 近三十日销量
         */
        private BigDecimal avgThirtySalesQty;
        /**
         * 近六十日销量
         */
        private BigDecimal avgSixtySalesQty;
        /**
         * 近九十日销量
         */
        private BigDecimal avgNinetySalesQty;
        /**
         * 近一百八十日销量
         */
        private BigDecimal avgOneHundredAndEightySalesQty;
        /**
         * 近二百七十日销量
         */
        private BigDecimal avgTwoHundredAndSeventySalesQty;
        /**
         * 近三百六十日销量
         */
        private BigDecimal avgThreeHundredAndSixtySalesQty;
        /**
         * 近三日销量
         */
        private BigDecimal threeSalesQty;
        /**
         * 近七日销量
         */
        private BigDecimal sevenSalesQty;
        /**
         * 近十四日销量
         */
        private BigDecimal fourteenSalesQty;
        /**
         * 近三十日销量
         */
        private BigDecimal thirtySalesQty;
        /**
         * 近六十日销量
         */
        private BigDecimal sixtySalesQty;
        /**
         * 近九十日销量
         */
        private BigDecimal ninetySalesQty;
        /**
         * 近一百八十日销量
         */
        private BigDecimal oneHundredAndEightySalesQty;
        /**
         * 近二百七十日销量
         */
        private BigDecimal twoHundredAndSeventySalesQty;
        /**
         * 近三百六十日销量
         */
        private BigDecimal threeHundredAndSixtySalesQty;


        public static ReplenishmentSuggestionDTO.SalesEstimateExportDTO buildSalesInfoEstimateDTO(SalesEstimateEntity entity, ReplenishmentSuggestionVO.View view, String shopName, String platform) {
            StringBuilder salesInfoEstimateType = new StringBuilder();
            String rule;
            String baseRuleName = "日销量";
            CfgRuleSalesFormulaDTO.PercentJsonDTO percentJsonDTO = JSONUtil.toBean(entity.getPercentJson(), CfgRuleSalesFormulaDTO.PercentJsonDTO.class);
            if (CfgRuleSalesFormulaTypeEnum.FIXED.getCode().equals(entity.getType())) {
                salesInfoEstimateType.append(CfgRuleSalesFormulaTypeEnum.FIXED.getName()).append(baseRuleName);
                rule = String.valueOf(entity.getFixedValue());
            } else if (CfgRuleSalesFormulaTypeEnum.DYNAMIC.getCode().equals(entity.getType())) {
                salesInfoEstimateType.append(CfgRuleSalesFormulaTypeEnum.DYNAMIC.getName()).append(baseRuleName);
                rule = TimePeriodEnum.buildRule(percentJsonDTO);
            } else {
                salesInfoEstimateType.append(CfgRuleSalesFormulaTypeEnum.DEFAULT.getName()).append(baseRuleName);
                if (CfgRuleSalesFormulaDefaultTypeEnum.FIXED.getCode().equals(entity.getDefaultType())) {
                    salesInfoEstimateType.append("(").append(CfgRuleSalesFormulaDefaultTypeEnum.FIXED.getName()).append(")");
                    rule = String.valueOf(entity.getFixedValue());
                } else {
                    salesInfoEstimateType.append("(").append(CfgRuleSalesFormulaDefaultTypeEnum.DYNAMIC.getName()).append(")");
                    rule = TimePeriodEnum.buildRule(percentJsonDTO);
                }
            }
            List<CalcSalesInfoDimDTO.SalesVO> salesQtyList = JSON.parseObject(view.getSalesQtyJson(), new TypeReference<List<CalcSalesInfoDimDTO.SalesVO>>() {
            });
            Map<String, BigDecimal> salesQtyMap = salesQtyList.stream()
                    .collect(Collectors.toMap(CalcSalesInfoDimDTO.SalesVO::getType, CalcSalesInfoDimDTO.SalesVO::getQty, BigDecimal::add));
            List<CalcSalesInfoDimDTO.SalesVO> avgSalesQtyList = JSON.parseObject(view.getAvgSalesQtyJson(), new TypeReference<List<CalcSalesInfoDimDTO.SalesVO>>() {
            });
            Map<String, BigDecimal> avgSalesQtyMap = avgSalesQtyList.stream()
                    .collect(Collectors.toMap(CalcSalesInfoDimDTO.SalesVO::getType, CalcSalesInfoDimDTO.SalesVO::getQty, BigDecimal::add));
            ReplenishmentSuggestionDTO.SalesEstimateExportDTO dto = new ReplenishmentSuggestionDTO.SalesEstimateExportDTO();
            dto.setSkuId(view.getSkuId());
            dto.setShopId(view.getShopId());
            dto.setSkuNo(view.getSkuNo());
            dto.setShopName(shopName);
            dto.setPlatformName(platform);
            dto.setDate(entity.getDate());
            dto.setSalesInfoEstimateType(salesInfoEstimateType.toString());
            dto.setRule(rule);
            dto.setEstimateQty(entity.getSalesQty());
            dto.setAvgThreeSalesQty(avgSalesQtyMap.get(TimePeriodEnum.THREE.getName()));
            dto.setAvgSevenSalesQty(avgSalesQtyMap.get(TimePeriodEnum.SEVEN.getName()));
            dto.setAvgThirtySalesQty(avgSalesQtyMap.get(TimePeriodEnum.THIRTY.getName()));
            dto.setAvgFourteenSalesQty(avgSalesQtyMap.get(TimePeriodEnum.FOURTEEN.getName()));
            dto.setAvgSixtySalesQty(avgSalesQtyMap.get(TimePeriodEnum.SIXTY.getName()));
            dto.setAvgNinetySalesQty(avgSalesQtyMap.get(TimePeriodEnum.NINETY.getName()));
            dto.setAvgOneHundredAndEightySalesQty(avgSalesQtyMap.get(TimePeriodEnum.ONE_HUNDRED_AND_EIGHTY.getName()));
            dto.setAvgTwoHundredAndSeventySalesQty(avgSalesQtyMap.get(TimePeriodEnum.TWO_HUNDRED_AND_SEVENTY.getName()));
            dto.setAvgThreeHundredAndSixtySalesQty(avgSalesQtyMap.get(TimePeriodEnum.THREE_HUNDRED_AND_SIXTY.getName()));
            dto.setSevenSalesQty(salesQtyMap.get(TimePeriodEnum.SEVEN.getName()));
            dto.setThreeSalesQty(salesQtyMap.get(TimePeriodEnum.THREE.getName()));
            dto.setFourteenSalesQty(salesQtyMap.get(TimePeriodEnum.FOURTEEN.getName()));
            dto.setThirtySalesQty(salesQtyMap.get(TimePeriodEnum.THIRTY.getName()));
            dto.setNinetySalesQty(salesQtyMap.get(TimePeriodEnum.NINETY.getName()));
            dto.setSixtySalesQty(salesQtyMap.get(TimePeriodEnum.SIXTY.getName()));
            dto.setOneHundredAndEightySalesQty(salesQtyMap.get(TimePeriodEnum.ONE_HUNDRED_AND_EIGHTY.getName()));
            dto.setTwoHundredAndSeventySalesQty(salesQtyMap.get(TimePeriodEnum.TWO_HUNDRED_AND_SEVENTY.getName()));
            dto.setThreeHundredAndSixtySalesQty(salesQtyMap.get(TimePeriodEnum.THREE_HUNDRED_AND_SIXTY.getName()));
            return dto;
        }
    }

    @Setter
    @Getter
    public static class InventoryEstimateExportDTO {
        /**
         * 平台
         */
        private String platform;
        /**
         * 平台名称
         */
        private String platformName;
        /**
         * 店铺id
         */
        private String shopId;

        /**
         * 店铺
         */
        private String shopName;

        /**
         * SKU ID
         */
        private String skuId;
        /**
         * SKU
         */
        private String skuNo;
        /**
         * 日期
         */
        private LocalDate date;
        /**
         * 结余库存
         */
        private BigDecimal balanceInventory;
        /**
         * 预估销量
         */
        private BigDecimal salesQty;
        /**
         * 发货在途_到货量
         */
        private BigDecimal inTransitQty;
        /**
         * 预计发货_到货量
         */
        private BigDecimal deliveryQty;
        /**
         * 预估结余库存
         */
        private BigDecimal estimatedBalanceInventory;
        /**
         * 是否断货
         */
        private String isOutOfStock;

    }

    /**
     * 历史销量，去噪销量
     */
    @Getter
    @Setter
    public static class SalesInfoDenoisingDTO {
        /**
         * sku id
         */
        private String skuId;
        /**
         * sku
         */
        private String skuNo;
        /**
         * 店铺
         */
        private String shopId;
        /**
         * 店铺名字
         */
        private String shopName;
        /**
         * 平台
         */
        private String platform;

        /**
         * 平台
         */
        private String platformName;
        /**
         * 日期
         */
        private LocalDate date;
        /**
         * 历史销量
         */
        private Integer hisSalesQty;
        /**
         * 去噪类型，percentage百分比去噪：fixedValue=固定值去噪，completely=完全去噪  枚举：CfgRuleSalesDenoisingCalcDenoisingTypeEnum
         */
        private String denoisingType;
        /**
         * 去噪类型名字
         */
        private String denoisingTypeName;
        /**
         * 有效值（去噪后的）
         */
        private String effectiveValue;
        /**
         * 去噪销量
         */
        private BigDecimal denoisingQty;

        public static ReplenishmentSuggestionDTO.SalesInfoDenoisingDTO buildSalesInfoDenoisingDTO(ReplenishmentSuggestionVO.View view, String shopName, String platform, LocalDate date,
                                                                                           Map<LocalDate, SalesInfoEntity> calcDenoisingMap, Map<LocalDate, Integer> calcSalesInfoHisMap) {
            ReplenishmentSuggestionDTO.SalesInfoDenoisingDTO dto = new ReplenishmentSuggestionDTO.SalesInfoDenoisingDTO();
            dto.setSkuId(view.getSkuId());
            dto.setSkuNo(view.getSkuNo());
            dto.setShopId(view.getShopId());
            dto.setShopName(shopName);
            dto.setPlatformName(platform);
            dto.setDate(date);
            Integer hisQty = Optional.ofNullable(calcSalesInfoHisMap.get(date)).orElse(0);
            dto.setHisSalesQty(hisQty);
            SalesInfoEntity entity = calcDenoisingMap.get(date);
            if (ObjectUtils.isEmpty(entity)) {
                dto.setDenoisingQty(new BigDecimal(hisQty));
            } else {
                if (Boolean.TRUE.equals(entity.getIsIgnoreOutOfStock())) {
                    dto.setDenoisingTypeName("断货排除");
                } else {
                    dto.setDenoisingTypeName(CfgRuleSalesDenoisingDenoisingTypeEnum.getName(entity.getSalesQtyType()));
                }
                if (!ObjectUtils.isEmpty(entity.getEffectiveValue())) {
                    if (CfgRuleSalesDenoisingDenoisingTypeEnum.PERCENTAGE.getCode().equals(entity.getSalesQtyType())) {
                        dto.setEffectiveValue(entity.getEffectiveValue() + "%");
                    } else {
                        dto.setEffectiveValue(String.valueOf(entity.getEffectiveValue()));
                    }
                }
                dto.setDenoisingQty(entity.getSalesQty());
            }
            return dto;
        }

    }
    @Getter
    @Setter
    public static class ExportResultDTO {

        /**
         * 去噪销量
         */
        private List<ReplenishmentSuggestionDTO.SalesInfoDenoisingDTO> salesInfoDenoisingExportList;

        /**
         * 预估日销量
         */
        private List<ReplenishmentSuggestionDTO.SalesEstimateExportDTO> salesEstimateExportList;
        /**
         * 库存预测
         */
        private List<ReplenishmentSuggestionDTO.InventoryEstimateExportDTO> inventoryEstimateExportList;
        /**
         * 默认日销量导出
         */
        private List<CfgRuleSalesFormulaDTO.SalesFormulaExportDTO> defaultSalesQtyExportList;

        /**
         * 动态日销量导出
         */
        private List<CfgRuleSalesFormulaDTO.SalesFormulaExportDTO> dynamicSalesQtyExportList;

        /**
         * 固定日销量导出
         */
        private List<CfgRuleSalesFormulaDTO.SalesFormulaExportDTO> fixedSalesQtyExportList;

        /**
         * 销量去噪导出
         */
        private List<CfgRuleSalesDenoisingDTO.SalesDenoisingExportDTO> salesDenoisingExportList;
    }

}
