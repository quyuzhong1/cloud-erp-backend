package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DeliveryPlanDetailExportExcelDTO {
    /**
     * sku
     */
    @ExcelProperty(value = "*第三方仓sku", index = 0)
    @FieldValid(fieldName = "第三方仓sku", isNotBlank = true)
    private String skuNo;

    /**
     * 计划数量
     */
    @ExcelProperty(value = "*计划备货量", index = 1)
    @FieldValid(fieldName = "计划备货量",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER,maxLength = 16)
    private String planQty;

    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index = 2)
    private String errorMsg;
}
