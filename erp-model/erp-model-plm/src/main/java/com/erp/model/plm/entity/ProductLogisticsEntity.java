package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.entity.BaseEntity;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @Description 产品物流信息表
 * @Author Luo_WG
 * @Date 2022/9/22 16:03
 **/
@TableName(value ="product_logistics")
@Data
public class ProductLogisticsEntity extends BaseEntity<ProductLogisticsEntity> implements Serializable {

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

    /**
     * 输入电压
     */
    @TableField(value = "input_voltage")
    private BigDecimal inputVoltage;
    /**
     * 输出电压
     */
    @TableField(value = "output_voltage")
    private BigDecimal outputVoltage;

    /**
     * 电压单位  dict type=voltageUnit
     */
    @TableField(value = "voltage_unit")
    private String voltageUnit;

    /**
     * 输入电流
     */
    @TableField(value = "input_electric")
    private BigDecimal inputElectric;
    /**
     * 输出电流
     */
    @TableField(value = "output_electric")
    private BigDecimal outputElectric;

    /**
     * 电流单位 dict type=electricUnit
     */
    @TableField(value = "electric_unit")
    private String electricUnit;

    /**
     * 输入功率
     */
    @TableField(value = "input_power")
    private BigDecimal inputPower;
    /**
     * 输出功率
     */
    @TableField(value = "output_power")
    private BigDecimal outputPower;

    /**
     * 功率单位 dict type=powerUnit
     */
    @TableField(value = "power_unit")
    private String powerUnit;

    /**
     * 输入电池容量
     */
    @TableField(value = "input_battery_capacity")
    private BigDecimal inputBatteryCapacity;
    /**
     * 输出电池容量
     */
    @TableField(value = "output_battery_capacity")
    private BigDecimal outputBatteryCapacity;

    /**
     * 电池容量单位 dict type=batteryCapacityUnit
     */
    @TableField(value = "battery_capacity_unit")
    private String batteryCapacityUnit;

    /**
     * 审核时间
     */
    @TableField("approve_time")
    private LocalDateTime approveTime;
    /**
     * 审核人名称
     */
    @TableField("approve_user_name")
    private String approveUserName;
    /**
     * 审核人id
     */
    @TableField("approve_user_id")
    private String approveUserId;

    /**
     * 单据审核状态
     */
    @TableField("approve_status")
    private ApproveStatusEnum approveStatus;

    /**
     * 第一数量
     */
    @TableField("first_qty")
    private BigDecimal firstQty;

    /**
     * 第二数量
     */
    @TableField("second_qty")
    private BigDecimal secondQty;

    @TableField(exist = false)
    private String skuNo;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}