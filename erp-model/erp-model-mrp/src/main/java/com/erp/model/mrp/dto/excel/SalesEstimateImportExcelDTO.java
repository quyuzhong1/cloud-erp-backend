package com.erp.model.mrp.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import lombok.Data;

import java.io.Serializable;

/**
 * 运营销量预估
 * @author will
 * @date 2024/9/5 10:46
 */
@Data
public class SalesEstimateImportExcelDTO implements Serializable {

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
     * 当月销量预估
     */
    @ExcelProperty(value = "当月销量预估", index = 0)
    @FieldValid(fieldName = "当月销量预估",  maxLength = 50)
    private String currentMonthSalesQty;


    /**
     * 下月销量预估
     */
    @ExcelProperty(value = "下月销量预估", index = 0)
    @FieldValid(fieldName = "下月销量预估", maxLength = 50)
    private String nextMonthSales;

    /**
     * 后月销量预估
     */
    @ExcelProperty(value = "后月销量预估", index = 0)
    @FieldValid(fieldName = "后月销量预估",  maxLength = 50)
    private String followingMonthSales;

    /**
     * 错误数据
     */
    private String errorMsg;
}
