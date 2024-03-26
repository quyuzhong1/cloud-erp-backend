package com.erp.model.oms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author zdy
 * @ClassName TransferDeclareProductDTO
 * @description: TODO
 * @date 2024年01月30日
 * @version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransferDeclareProductDTO implements Serializable {

    private String soId;
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
    private String skuNo;
    private String skuId;

    /**
     * 中文报关名称
     */
    private String declareChineseName;

    /**
     * 英文报关名称
     */
    private String declareEnglishName;

    /**
     * 目的国申报价
     */
    private BigDecimal declarePrice;

    /**
     * 目的国申报币种
     */
    private String currency;
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
}
