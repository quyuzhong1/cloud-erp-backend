package com.erp.model.mrp.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import com.erp.model.mrp.enums.CfgRuleSalesFormulaDefaultTypeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 默认销量
 * @author will
 * @date 2024/9/2 19:29
 */
@Data
public class DefaultSalesQtyImportExcelDTO implements Serializable {


    /**
     * 平台
     */
    @ExcelProperty(value = "*平台", index = 0)
    @FieldValid(fieldName = "平台", isNotBlank = true, maxLength = 32)
    private String platform;

    /**
     * SKU
     */
    @ExcelProperty(value = "*SKU", index = 1)
    @FieldValid(fieldName = "SKU", isNotBlank = true, maxLength = 32)
    private String skuNo;


    /**
     * 店铺
     */
    @ExcelProperty(value = "*店铺", index = 2)
    @FieldValid(fieldName = "店铺", isNotBlank = true, maxLength = 32)
    private String shopName;

    /**
     *默认日销量类型
     */
    @ExcelProperty(value = "*默认日销量类型", index = 3)
    @FieldValid(fieldName = "默认日销量类型", isNotBlank = true, maxLength = 32,enumClass = CfgRuleSalesFormulaDefaultTypeEnum.class)
    private String defaultTypeName;

    /**
     * 固定日销量
     */
    @ExcelProperty(value = "固定日销量", index = 4)
    @FieldValid(fieldName = "固定日销量",formatPattern= FieldFormatPatternTypeEnum.AMOUNT)
    private String fixedValue;

    /**
     * 3天日均（%）
     */
    @ExcelProperty(value = "3天日均（%）", index = 5)
    @FieldValid(fieldName = "3天日均（%）",formatPattern= FieldFormatPatternTypeEnum.AMOUNT)
    private String threeDaysRatio;

    /**
     * 7天日均（%）
     */
    @ExcelProperty(value = "7天日均（%）", index = 6)
    @FieldValid(fieldName = "7天日均（%）",formatPattern= FieldFormatPatternTypeEnum.AMOUNT)
    private String sevenDaysRatio;


    /**
     * 14天日均（%）
     */
    @ExcelProperty(value = "14天日均（%）", index = 7)
    @FieldValid(fieldName = "14天日均（%）",formatPattern= FieldFormatPatternTypeEnum.AMOUNT)
    private String fourteenDaysRatio;



    /**
     * 30天日均（%）
     */
    @ExcelProperty(value = "30天日均（%）", index = 8)
    @FieldValid(fieldName = "30天日均（%）",formatPattern= FieldFormatPatternTypeEnum.AMOUNT)
    private String thirtyDaysRatio;



    /**
     * 60天日均（%）
     */
    @ExcelProperty(value = "60天日均（%）", index = 9)
    @FieldValid(fieldName = "60天日均（%）",formatPattern= FieldFormatPatternTypeEnum.AMOUNT)
    private String sixtyDaysRatio;


    /**
     * 90天日均（%）
     */
    @ExcelProperty(value = "90天日均（%）", index = 10)
    @FieldValid(fieldName = "90天日均（%）",formatPattern= FieldFormatPatternTypeEnum.AMOUNT)
    private String ninetyDaysRatio;


    /**
     * 180天日均（%）
     */
    @ExcelProperty(value = "180天日均（%）", index = 11)
    @FieldValid(fieldName = "180天日均（%）",formatPattern= FieldFormatPatternTypeEnum.AMOUNT)
    private String oneHundredEightyDaysRatio;


    /**
     * 270天日均（%）
     */
    @ExcelProperty(value = "270天日均（%）", index = 12)
    @FieldValid(fieldName = "270天日均（%）",formatPattern= FieldFormatPatternTypeEnum.AMOUNT)
    private String twoHundredSeventyDaysRatio;


    /**
     * 360天日均（%）
     */
    @ExcelProperty(value = "360天日均（%）", index = 13)
    @FieldValid(fieldName = "360天日均（%）",formatPattern= FieldFormatPatternTypeEnum.AMOUNT)
    private String threeHundredSixtyDaysRatio;

    /**
     * 错误数据
     */
    private String errorMsg;
}
