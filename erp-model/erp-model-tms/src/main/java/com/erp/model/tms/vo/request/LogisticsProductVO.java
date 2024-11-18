package com.erp.model.tms.vo.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogisticsProductVO  implements Serializable {

    //数量
    private Integer quantity;

    //单件重量(单位:g)
    private Integer weight;
    /**
     * 毛重
     */
    private BigDecimal grossWeight;

    //商品链接
    private String url;

    //是否带电
    private Boolean isElectric;
    //是否纯电
    private Boolean onlyBattery;
    //是否液体
    private Boolean isLiquid;

    //备注
    private String remark;

    //配货信息
    private String distributionInfo;



    private String id;


    /**
     * 产品sku表id
     */
    private String skuId;

    private String skuNo;

    /**
     * 产品属性
     */
    private String productProperty;

    /**
     * 产品属性id
     */
    private String productPropertyId;

    /**
     * 报关型号
     */
    private String declareModel;

    /**
     * 报关中文名
     */
    private String declareChineseName;

    /**
     * 报关英文名
     */
    private String declareEnglishName;

    /**
     * 报关申报价格
     */
    private BigDecimal declarePrice;

    /**
     * 海关编码
     */
    private String customsCode;

    /**
     * 报关单位
     */
    private String declareUnit;

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
     * 报关申报价币种符号
     */
    private String declareCurrencySymbol;


    /**
     * 目的国申报价
     */
    private BigDecimal destDeclarePrice;


    /**
     * 目的国币种
     */
    private String destCurrency;


    /**
     * 目的国币种符号
     */
    private String destCurrencySymbol;

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
     * 交易子单号
     * 速卖通必填
     */
    private Long childOrderId;
    /**
     * 仓库发货属性名称
     */
    private String scItemCode;
    /**
     * 仓库发货属性代码
     */
    private Long scItemId;
    /**
     * 仓库发货属性名称
     */
    private String scItemName;
    /**
     * sku名称
     */
    private String skuName;
}
