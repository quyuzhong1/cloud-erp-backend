package com.erp.model.tms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.CurrencyEnum;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import com.erp.model.tms.enums.PriceBinaryEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: 运费模板导入DTO
 * @date 2023/11/8 16:11
 */
@Data
public class ShippingTemplateExcelDTO implements Serializable {

    /**
     * 模板名称
     */
    @ExcelProperty(value = "*模板名称")
    @FieldValid(fieldName = "模板名称",isNotBlank = true,maxLength = 50)
    private String  name;

    /**
     * 币种
     */
    @ExcelProperty(value = "*币种")
    @FieldValid(fieldName = "币种",isNotBlank = true,enumClass = CurrencyEnum.class)
    private String currency;

    /**
     * *重量单位
     */
    @ExcelProperty(value = "*重量单位")
    @FieldValid(fieldName = "重量单位",isNotBlank = true,fieldValues = "kg,g")
    private String weightUnit;

    /**
     * 价格进制
     */
    @ExcelProperty(value = "*价格进制")
    @FieldValid(fieldName = "价格进制",isNotBlank = true,enumClass = PriceBinaryEnum.class)
    private String priceBinary;

    /**
     * 材积设置
     */
    @ExcelProperty(value = "*材积设置")
    @FieldValid(fieldName = "材积设置",isNotBlank = true,maxLength = 32,formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER)
    private String volumeSetting;

    /**
     * 生效日期
     */
    @ExcelProperty(value = "*生效日期")
    @FieldValid(fieldName = "生效日期",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String effectiveDate;

    /**
     * 失效日期
     */
    @ExcelProperty(value = "失效日期")
    @FieldValid(fieldName = "失效日期",formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String expireDate;

    /**
     * 起始地
     */
    @ExcelProperty(value = "*起始地")
    @FieldValid(fieldName = "起始地",isNotBlank = true)
    private String fromCountry;

    /**
     * 目的地
     */
    @ExcelProperty(value = "*目的地")
    private String toCountry;

    /**
     * 城市分区
     */
    @ExcelProperty(value = "*城市分区")
    private String region;

    /**
     * 目的仓库
     */
    @ExcelProperty(value = "*目的仓库")
    private String toWarehouseName;

    /**
     * 开始重量
     */
    @ExcelProperty(value = "*开始重量")
    @FieldValid(fieldName = "开始重量",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String startWeight;

    /**
     * 结束重量
     */
    @ExcelProperty(value = "*结束重量")
    @FieldValid(fieldName = "结束重量",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String endWeight;

    /**
     * 首重
     */
    @ExcelProperty(value = "*首重")
    private String firstWeight;

    /**
     * 首重运费
     */
    @ExcelProperty(value = "*首重运费")
    private String firstWeightShippingCost;

    /**
     * 续重单位重量
     */
    @ExcelProperty(value = "*续重单位重量")
    private String additionalUnitWeight;

    /**
     * 续重单价
     */
    @ExcelProperty(value = "*续重单价")
    private String additionalPrice;

    /**
     * 运费单价
     */
    @ExcelProperty(value = "*运费单价")
    private String shippingPrice;

    /**
     * 挂号费
     */
    @ExcelProperty(value = "挂号费")
    @FieldValid(fieldName = "挂号费",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String registrationCost;

    /**
     * 操作费
     */
    @ExcelProperty(value = "操作费")
    @FieldValid(fieldName = "操作费",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String operatingCost;

    /**
     * 最低收费
     */
    @ExcelProperty(value = "最低收费")
    @FieldValid(fieldName = "最低收费",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String minCost;

    /**
     * 折扣费率
     */
    @ExcelProperty(value = "折扣费率")
    @FieldValid(fieldName = "折扣费率",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String discountRate;

    /**
     * 签名费
     */
    @ExcelProperty(value = "签名费")
    @FieldValid(fieldName = "签名费",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String signatureCost;

    /**
     * 燃油附加费率
     */
    @ExcelProperty(value = "燃油附加费率")
    @FieldValid(fieldName = "燃油附加费率",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String fuelSurchargeRate;

    /**
     * 保险费
     */
    @ExcelProperty(value = "保险费")
    @FieldValid(fieldName = "保险费",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String premiumCost;

    /**
     * 错误信息
     */
    private String errorMsg;
}
