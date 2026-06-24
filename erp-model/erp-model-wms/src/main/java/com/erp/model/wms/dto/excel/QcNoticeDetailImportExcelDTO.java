package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 *
 * @author jack
 */
@Data
@NoArgsConstructor
public class QcNoticeDetailImportExcelDTO {
    /**
     * sku
     */
    @ExcelProperty(value = "*SKU")
    @FieldValid(fieldName = "SKU", isNotBlank = true,maxLength = 64)
    private String skuNo;

    /**
     * 送检数量
     */
    @ExcelProperty(value = "*送检数量")
    @FieldValid(fieldName = "送检数量",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER,maxLength = 32)
    private String qcNoticeQty;
    /**
     * 错误信息
     */
    @ExcelProperty(value = "错误数据")
    private String errorMsg;

}
