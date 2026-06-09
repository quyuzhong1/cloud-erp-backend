package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * B2B客户装箱明细导入
 */
@Data
@NoArgsConstructor
public class B2bCustomerPackingImportExcelDTO implements Serializable {

    @ExcelProperty(value = "序号", index = 0)
    @FieldValid(fieldName = "序号", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER, maxLength = 9)
    private String boxSeq;

    @ExcelProperty(value = "箱唛号", index = 1)
    @FieldValid(fieldName = "箱唛号", maxLength = 50)
    private String boxMarkNo;

    @ExcelProperty(value = "箱唛参考号", index = 2)
    @FieldValid(fieldName = "箱唛参考号", maxLength = 50)
    private String boxMarkRefNo;

    @ExcelProperty(value = "标签尺寸", index = 3)
    @FieldValid(fieldName = "标签尺寸", maxLength = 20)
    private String labelSize;

    @ExcelProperty(value = "贴标要求", index = 4)
    @FieldValid(fieldName = "贴标要求", maxLength = 200)
    private String labelingRequirement;

    @ExcelProperty(value = "SKU", index = 5)
    @FieldValid(fieldName = "SKU", isNotBlank = true)
    private String skuNo;

    @ExcelProperty(value = "装箱数量", index = 6)
    @FieldValid(fieldName = "装箱数量", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER, maxLength = 9)
    private String packingQty;

    @ExcelProperty(value = "错误数据", index = 7)
    private String errorMsg;
}
