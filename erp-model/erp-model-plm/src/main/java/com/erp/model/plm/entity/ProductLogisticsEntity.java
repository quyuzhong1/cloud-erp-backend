package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Description 产品物流信息表
 * @Author Luo_WG
 * @Date 2022/9/22 16:03
 **/
@TableName(value ="product_logistics")
@Data
public class ProductLogisticsEntity extends BaseEntity implements Serializable {

    /**
     * 产品sku表id
     */
    @TableField(value = "sku_id")
    private String skuId;

    /**
     * 产品属性
     */
    @TableField(value = "product_property")
    private String productProperty;

    /**
     * 产品属性id
     */
    @TableField(value = "product_property_id")
    private String productPropertyId;

    /**
     * 报关型号
     */
    @TableField(value = "declare_model")
    private String declareModel;

    /**
     * 报关中文名
     */
    @TableField(value = "declare_chinese_name")
    private String declareChineseName;

    /**
     * 报关英文名
     */
    @TableField(value = "declare_english_name")
    private String declareEnglishName;

    /**
     * 报关申报价格
     */
    @TableField(value = "declare_price", fill = FieldFill.INSERT_UPDATE)
    private BigDecimal declarePrice;

    /**
     * 海关编码
     */
    @TableField(value = "customs_code")
    private String customsCode;

    /**
     * 报关单位
     */
    @TableField(value = "declare_unit")
    private String declareUnit;

    /**
     * 申报要素
     */
    @TableField(value = "declare_element")
    private String declareElement;

    /**
     * 英文材质
     */
    @TableField(value = "english_material")
    private String englishMaterial;

    /**
     * 英文用途
     */
    @TableField(value = "english_usage")
    private String englishUsage;


    /**
     * 报关申报价币种
     */
    @TableField(value = "declare_currency")
    private String declareCurrency;

    /**
     * 报关申报价币种符号
     */
    @TableField(value = "declare_currency_symbol")
    private String declareCurrencySymbol;


    /**
     * 目的国申报价
     */
    @TableField(value = "dest_declare_price")
    private BigDecimal destDeclarePrice;


    /**
     * 目的国币种
     */
    @TableField(value = "dest_currency")
    private String destCurrency;


    /**
     * 目的国币种符号
     */
    @TableField(value = "dest_currency_symbol")
    private String destCurrencySymbol;

    /**
     * 征免
     */
    @TableField(value = "exemption")
    private String exemption;

    /**
     * 境内货源地
     */
    @TableField(value = "source_cargo")
    private String sourceCargo;


    /**
     * 原产国
     */
    @TableField(value = "source_country")
    private String sourceCountry;

    /**
     * 组合品申报类型
     */
    @TableField(value = "combination_declare_type")
    private String combinationDeclareType;





    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}