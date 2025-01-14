package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DeliveryPlanDetailPdaExportExcelDTO {
    /**
     * msku
     */
    @ExcelProperty(value = "MSKU")
    @FieldValid(fieldName = "MSKU")
    private String msku;

    /**
     * fnsku
     */
    @ExcelProperty(value = "FNSKU")
    @FieldValid(fieldName = "FNSKU")
    private String fnsku;

    /**
     * 计划数量
     */
    @ExcelProperty(value = "*计划数量")
    @FieldValid(fieldName = "*计划数量",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER,maxLength = 16)
    private String planQty;

    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index = 3)
    private String errorMsg;
}
