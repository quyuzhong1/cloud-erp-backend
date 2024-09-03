package com.erp.model.mrp.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import lombok.Data;

import java.io.Serializable;

/**
 * 固定销量
 * @author will
 * @date 2024/9/2 19:29
 */
@Data
public class FixedSalesQtyImportExcelDTO implements Serializable {


    /**
     * 平台
     */
    @ExcelProperty(value = "平台", index = 0)
    @FieldValid(fieldName = "平台", isNotBlank = true, maxLength = 50)
    private String platform;

    /**
     * SKU
     */
    @ExcelProperty(value = "SKU", index = 0)
    @FieldValid(fieldName = "SKU", isNotBlank = true, maxLength = 50)
    private String skuNo;


    /**
     * 店铺
     */
    @ExcelProperty(value = "店铺", index = 0)
    @FieldValid(fieldName = "店铺", isNotBlank = true, maxLength = 50)
    private String shopName;

    /**
     * 错误数据
     */
    private String errorMsg;
}
