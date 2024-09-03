package com.erp.model.mrp.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
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
    @FieldValid(fieldName = "平台", isNotBlank = true, maxLength = 50)
    private String platform;

    /**
     * SKU
     */
    @ExcelProperty(value = "*SKU", index = 0)
    @FieldValid(fieldName = "SKU", isNotBlank = true, maxLength = 50)
    private String skuNo;


    /**
     * 店铺
     */
    @ExcelProperty(value = "*店铺", index = 0)
    @FieldValid(fieldName = "店铺", isNotBlank = true, maxLength = 50)
    private String shopName;

    /**
     *默认日销量类型
     */
    @ExcelProperty(value = "*默认日销量类型", index = 0)
    @FieldValid(fieldName = "默认日销量类型", isNotBlank = true, maxLength = 50)
    private String defaultType;

    /**
     * 固定日销量
     */
    @ExcelProperty(value = "固定日销量", index = 0)
    @FieldValid(fieldName = "固定日销量", isNotBlank = true, maxLength = 50)
    private String fixedValue;

    /**
     * 3天日均（%）
     */
    @ExcelProperty(value = "3天日均（%）", index = 0)
    @FieldValid(fieldName = "3天日均（%）", isNotBlank = true, maxLength = 50)
    private String threeDaysRatio;

    /**
     * 7天日均（%）
     */
    @ExcelProperty(value = "7天日均（%）", index = 0)
    @FieldValid(fieldName = "7天日均（%）", isNotBlank = true, maxLength = 50)
    private String sevenDaysRatio;


    /**
     * 14天日均（%）
     */
    @ExcelProperty(value = "14天日均（%）", index = 0)
    @FieldValid(fieldName = "14天日均（%）", isNotBlank = true, maxLength = 50)
    private String fourteenDaysRatio;



    /**
     * 30天日均（%）
     */
    @ExcelProperty(value = "30天日均（%）", index = 0)
    @FieldValid(fieldName = "30天日均（%）", isNotBlank = true, maxLength = 50)
    private String thirtyDaysRatio;



    /**
     * 60天日均（%）
     */
    @ExcelProperty(value = "60天日均（%）", index = 0)
    @FieldValid(fieldName = "60天日均（%）", isNotBlank = true, maxLength = 50)
    private String sixtyDaysRatio;


    /**
     * 90天日均（%）
     */
    @ExcelProperty(value = "90天日均（%）", index = 0)
    @FieldValid(fieldName = "90天日均（%）", isNotBlank = true, maxLength = 50)
    private String ninetyDaysRatio;


    /**
     * 180天日均（%）
     */
    @ExcelProperty(value = "180天日均（%）", index = 0)
    @FieldValid(fieldName = "180天日均（%）", isNotBlank = true, maxLength = 50)
    private String oneHandredEightyDaysRatio;


    /**
     * 270天日均（%）
     */
    @ExcelProperty(value = "270天日均（%）", index = 0)
    @FieldValid(fieldName = "270天日均（%）", isNotBlank = true, maxLength = 50)
    private String twoHandredSeventyDaysRatio;


    /**
     * 360天日均（%）
     */
    @ExcelProperty(value = "360天日均（%）", index = 0)
    @FieldValid(fieldName = "360天日均（%）", isNotBlank = true, maxLength = 50)
    private String threeHandredSixtyDaysRatio;

    /**
     * 错误数据
     */
    private String errorMsg;
}
