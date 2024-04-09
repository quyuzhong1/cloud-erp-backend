package com.erp.model.plm.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * @description: 组合产品导入DTO
 * @author Will
 * @date: 2023/8/17 14:19
 */
@Data
public class BomCombinationImportExcelDTO implements Serializable {


    /**
     * 组合产品编码
     */
    @ExcelProperty(value = "*组合产品编码", index = 0)
    @FieldValid(fieldName = "组合产品编码", isNotBlank = true ,maxLength = 50)
    private String parentSkuNo;

    /**
     * 组合产品名称
     */
    @ExcelProperty(value = "*组合产品名称", index = 1)
    @FieldValid(fieldName = "组合产品名称",isNotBlank = true ,maxLength = 100)
    private String name;

    /**
     * 组合SKU
     */
    @ExcelProperty(value = "*组合SKU", index = 2)
    @FieldValid(fieldName = "组合SKU", isNotBlank = true ,maxLength = 64)
    private String skuNo;

    /**
     * 组合数量
     */
    @ExcelProperty(value = "*组合数量", index = 3)
    @FieldValid(fieldName = "组合数量", isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER)
    private String qty;

    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index = 4)
    private String  errorMsg;

}
