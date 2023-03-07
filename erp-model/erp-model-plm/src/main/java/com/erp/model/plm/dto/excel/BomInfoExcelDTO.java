package com.erp.model.plm.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/21 10:15
 */
@Data
public class BomInfoExcelDTO implements Serializable {

    /**
     * BOM类型
     */
    @ExcelProperty(value = "*BOM类型", index = 0)
    @FieldValid(fieldName = "BOM类型",isNotBlank = true,fieldValues = "单品BOM,销售套装BOM")
    private String typeName;

    /**
     * 父级SKU
     */
    @ExcelProperty(value = "*父级SKU", index = 1)
    @FieldValid(fieldName = "父级SKU",isNotBlank = true)
    private String parentSku;

    /**
     * 子SKU
     */
    @ExcelProperty(value = "*子SKU", index = 2)
    @FieldValid(fieldName = "子SKU",isNotBlank = true)
    private String childSku;

    /**
     * 用量
     */
    @ExcelProperty(value = "*用量", index = 3)
    @FieldValid(fieldName = "用量",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER)
    private String quantityStr;

    /**
     * 错误信息
     */
    @ExcelProperty(value = "错误信息", index = 4)
    private String errorMsg;



}
