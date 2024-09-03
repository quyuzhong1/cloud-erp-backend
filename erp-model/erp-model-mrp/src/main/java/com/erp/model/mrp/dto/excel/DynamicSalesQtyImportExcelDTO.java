package com.erp.model.mrp.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import lombok.Data;

import java.io.Serializable;

/**
 * 动态销量
 * @author will
 * @date 2024/9/2 19:29
 */
@Data
public class DynamicSalesQtyImportExcelDTO implements Serializable {


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
     * 规则名称
     */
    @ExcelProperty(value = "*规则名称", index = 0)
    @FieldValid(fieldName = "规则名称", isNotBlank = true, maxLength = 50)
    private String name;

    /**
     * 开始日期
     */
    @ExcelProperty(value = "*开始日期", index = 0)
    @FieldValid(fieldName = "开始日期", isNotBlank = true, maxLength = 50)
    private String startDateStr;

    /**
     * 结束日期
     */
    @ExcelProperty(value = "*结束日期", index = 0)
    @FieldValid(fieldName = "结束日期", isNotBlank = true, maxLength = 50)
    private String endDateStr;


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
