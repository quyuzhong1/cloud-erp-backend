package com.erp.model.oms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author zdy
 * @ClassName TransferDeclareProductDTO
 * @description: 物流申报明细
 * @date 2024年01月30日
 * @version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LogisticsDeclareProductDTO implements Serializable {

    private String soId;
    private String soCode;
    /**
     * 销售订单明细id
     */
    private String soDetailId;

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
    private String declareCn;

    /**
     * 英文报关名称
     */
    private String declareEn;

    /**
     * 出口申报价/报关申报价
     */
    private BigDecimal fromDeclarePrice;
    /**
     * 报关申报价币种
     */
    private String fromCurrency;
    /**
     *出口申报价币种符号
     */
    private String fromCurrencySymbol;
    /**
     * 目的国申报价
     */
    private BigDecimal toDeclarePrice;

    /**
     * 目的国币种
     */
    private String toCurrency;
    /**
     * 目的国币种符号
     */
    private String toCurrencySymbol;

    /**
     * 毛重 g
     */
    private BigDecimal grossWeight;
    /**
     * 重量 g
     */
    private Integer weight;
    /**
     * 申报价值 =目的国申报价 * 数量
     */
    private BigDecimal amount;
    /**
     * 是否含电
     */
    private Boolean isElectric;

    /**
     * 海关编码
     */
    private String toCustomsCode;
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
