package com.erp.model.mrp.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 动态备货系数
 * @author will
 * @date 2024/9/2 19:30
 */
@Data
public class StockingRatioImportExcelDTO implements Serializable {


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
     * 规则名称
     */
    @ExcelProperty(value = "*规则名称", index = 3)
    @FieldValid(fieldName = "规则名称", isNotBlank = true, maxLength = 10, formatPattern = FieldFormatPatternTypeEnum.NAME)
    private String name;

    /**
     * 开始日期
     */
    @ExcelProperty(value = "*开始日期", index = 4)
    @FieldValid(fieldName = "开始日期", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String startDateStr;

    /**
     * 结束日期
     */
    @ExcelProperty(value = "*结束日期", index = 5)
    @FieldValid(fieldName = "结束日期", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String endDateStr;

    /**
     * 备货系数
     */
    @ExcelProperty(value = "*备货系数", index = 6)
    @FieldValid(fieldName = "备货系数", isNotBlank = true,formatPattern= FieldFormatPatternTypeEnum.NUMBER_99)
    private String stockingRatioStr;

    /**
     * 错误数据
     */
    private String errorMsg;
}
