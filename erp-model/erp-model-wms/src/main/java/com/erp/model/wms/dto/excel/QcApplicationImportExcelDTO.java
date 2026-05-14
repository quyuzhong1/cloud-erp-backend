package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 *  质检申请导入DTO
 * @author will
 * @date 2026/3/24 14:08
 */
@Data
@NoArgsConstructor
public class QcApplicationImportExcelDTO implements Serializable {

    /**
     * SKU
     */
    @ExcelProperty(value = "*SKU", index = 0)
    @FieldValid(fieldName = "SKU", isNotBlank = true)
    private String skuNo;

    /**
     * 总数量
     */
    @ExcelProperty(value = "*总数量", index = 1)
    @FieldValid(fieldName = "总数量", isNotBlank = true,maxLength = 10,formatPattern= FieldFormatPatternTypeEnum.INTEGER)
    private String qtyStr;

    /**
     * 供应商
     */
    @ExcelProperty(value = "*供应商", index = 2)
    @FieldValid(fieldName = "供应商")
    private String supplierName;

    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index = 10)
    private String errorMsg = "";
}

