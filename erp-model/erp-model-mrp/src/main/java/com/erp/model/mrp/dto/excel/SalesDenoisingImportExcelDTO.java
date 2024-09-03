package com.erp.model.mrp.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import lombok.Data;

import java.io.Serializable;

/**
 * 其他出库单导入
 *
 * @author Jim
 * {@code @date:} 2024/03/22
 */
@Data
public class SalesDenoisingImportExcelDTO implements Serializable {


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
     * 去噪类型
     */
    @ExcelProperty(value = "*去噪类型", index = 0)
    @FieldValid(fieldName = "去噪类型", isNotBlank = true, maxLength = 50)
    private String denoisingType;

    /**
     * 百分比去噪（%）
     */
    @ExcelProperty(value = "百分比去噪（%）", index = 0)
    @FieldValid(fieldName = "百分比去噪（%）", isNotBlank = true, maxLength = 50)
    private String percentageValue;

    /**
     * 固定值去噪
     */
    @ExcelProperty(value = "固定值去噪", index = 0)
    @FieldValid(fieldName = "固定值去噪", isNotBlank = true, maxLength = 50)
    private String fixedValue;

    /**
     * 错误数据
     */
    private String errorMsg;
}
