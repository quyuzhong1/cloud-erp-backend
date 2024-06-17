package com.erp.model.oms.dto;

import com.erp.model.oms.enums.CalculateRuleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * @author zdy
 * @ClassName SplitSkuDTO
 * @description: TODO
 * @date 2024年01月30日
 * @version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SplitSkuDTO {
    /**
     * 长度cm
     */
    private BigDecimal length;
    /**
     * 宽度
     */
    private BigDecimal width;
    /**
     * 高度
     */
    private BigDecimal height;
    /**
     * 销售订单id
     */
    private String soId;
    /**
     * 销售订单编码
     */
    private String soCode;
    /**
     * 销售订单明细id
     */
    private String soDetailId;

    /**
     * 来源id[中转报关单据]
     */
    private String declareId;

    /**
     * 来源明细id[中转报关单据]
     */
    private String declareDetailId;

    /**
     * 数量
     */
    private Integer qty;
    /**
     * 产品sku编号
     */
    private String skuId;
    /**
     * 产品sku编号
     */
    private String skuNo;

    /**
     * 中文报关名称
     */
    private String declareChineseName;

    /**
     * 英文报关名称
     */
    private String declareEnglishName;

    /**
     * 出口申报价/报关申报价
     */
    private BigDecimal declarePrice;
    /**
     * 目的国申报价
     */
    private BigDecimal destDeclarePrice;

    /**
     * 目的国申报币种
     */
//    private String currency;
    /**
     *出口申报价币种符号
     */
    private String declareCurrencySymbol;
    /**
     * 毛重
     */
    private BigDecimal grossWeight;
    /**
     * 重量 g
     */
    private Integer weight;
    /**
     * 是否含电
     */
    private Boolean isElectric;

    /**
     * 海关编码
     */
    private String customsCode;
    /**
     * 报关单位
     */
    private String declareUnit;


    /**
     * 报关型号
     */
    private String declareModel;

    /**
     * 申报要素
     */
    private String declareElement;
    /**
     * 英文材质
     */
    private String englishMaterial;
    /**
     * 英文用途
     */
    private String englishUsage;


    /**
     * 报关申报价币种
     */
    private String declareCurrency;


    /**
     * 目的国币种
     */
    private String destCurrency;


    /**
     * 目的国币种符号
     */
    private String currencySymbol;

    /**
     * 征免
     */
    private String exemption;

    /**
     * 境内货源地
     */
    private String sourceCargo;


    /**
     * 原产国
     */
    private String sourceCountry;

    /**
     * 组合品申报类型
     */
    private String combinationDeclareType;
    /**
     * 属性
     */
    private String productProperty;


    /**
     * 属性id
     */
    private String productPropertyId;

    public SplitSkuDTO(String skuId, String skuNo) {
        this.skuId = skuId;
        this.skuNo = skuNo;
        this.qty = 0;
        this.length = BigDecimal.ZERO;
        this.width = BigDecimal.ZERO;
        this.height = BigDecimal.ZERO;
        this.grossWeight = BigDecimal.ZERO;
    }

    public static BigDecimal calculateSplitSkuDTOGrossWeight(List<SplitSkuDTO> skuList, String grossWeight) {
        BigDecimal totalGrossWeight;
        //毛重
        if (StringUtils.isNotEmpty(grossWeight) && CalculateRuleEnum.MAX.getCode().equalsIgnoreCase(grossWeight)) {
            totalGrossWeight = skuList.stream().map(SplitSkuDTO::getGrossWeight)
                    .filter(Objects::nonNull)
                    .max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        } else if (StringUtils.isNotEmpty(grossWeight) && CalculateRuleEnum.MIN.getCode().equalsIgnoreCase(grossWeight)) {
            totalGrossWeight = skuList.stream().map(SplitSkuDTO::getGrossWeight)
                    .filter(Objects::nonNull)
                    .min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        } else {
            totalGrossWeight = skuList.stream()
                    .filter(Objects::nonNull)
                    .map(e -> e.getGrossWeight().multiply(new BigDecimal(e.getQty())))
                    .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        }
        return totalGrossWeight;
    }

    public static BigDecimal calculateSplitSkuDTOHeight(List<SplitSkuDTO> skuList, String height) {
        BigDecimal totalHeight;
        //高
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(height) && CalculateRuleEnum.MAX.getCode().equalsIgnoreCase(height)) {
            totalHeight = skuList.stream().map(SplitSkuDTO::getHeight)
                    .filter(Objects::nonNull)
                    .max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        } else if (org.apache.commons.lang3.StringUtils.isNotEmpty(height) && CalculateRuleEnum.MIN.getCode().equalsIgnoreCase(height)) {
            totalHeight = skuList.stream().map(SplitSkuDTO::getHeight)
                    .filter(Objects::nonNull)
                    .min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        } else {
            totalHeight = skuList.stream()
                    .filter(Objects::nonNull)
                    .map(e -> e.getHeight().multiply(new BigDecimal(e.getQty())))
                    .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        }
        return totalHeight;
    }

    public static BigDecimal calculateSplitSkuDTOWidth(List<SplitSkuDTO> skuList, String width) {
        BigDecimal maxWidth;
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(width) && CalculateRuleEnum.SUM.getCode().equalsIgnoreCase(width)) {
            maxWidth = skuList.stream().map(e -> e.getWidth().multiply(new BigDecimal(e.getQty())))
                    .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        } else if (org.apache.commons.lang3.StringUtils.isNotEmpty(width) && CalculateRuleEnum.MIN.getCode().equalsIgnoreCase(width)) {
            maxWidth = skuList.stream().map(SplitSkuDTO::getWidth)
                    .filter(Objects::nonNull)
                    .min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        } else {
            maxWidth = skuList.stream().map(SplitSkuDTO::getWidth)
                    .filter(Objects::nonNull)
                    .max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        }
        return maxWidth;
    }

    public static BigDecimal calculateSplitSkuDTOLength(List<SplitSkuDTO> skuList, String length) {
        BigDecimal maxLength;
        //长
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(length) && CalculateRuleEnum.SUM.getCode().equalsIgnoreCase(length)) {
            maxLength = skuList.stream()
                    .map(e -> e.getLength().multiply(new BigDecimal(e.getQty())))
                    .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        } else if (org.apache.commons.lang3.StringUtils.isNotEmpty(length) && CalculateRuleEnum.MIN.getCode().equalsIgnoreCase(length)) {
            maxLength = skuList.stream().map(SplitSkuDTO::getLength)
                    .filter(Objects::nonNull)
                    .min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        } else {
            maxLength = skuList.stream().map(SplitSkuDTO::getLength)
                    .filter(Objects::nonNull)
                    .max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        }
        return maxLength;
    }
}
