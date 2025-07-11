package com.erp.model.plm.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description
 * @Author yl
 * @Date 2023-11-08 12:21
 */
@Data
@NoArgsConstructor
public class UpdateDeclarePriceExcelDTO {
    @ColumnWidth(20)
    @ExcelProperty(value = "*sku", index = 0)
    @FieldValid(fieldName = "*sku", isNotBlank = true )
    private String skuNo;

    @ColumnWidth(30)
    @ExcelProperty(value = "*出口申报价", index = 1)
    @FieldValid(fieldName = "*出口申报价", isNotBlank = true ,formatPattern= FieldFormatPatternTypeEnum.AMOUNT)
    private String declarePrice;

    @ColumnWidth(30)
    @ExcelProperty(value = "*出口申报价币种", index = 2)
    @FieldValid(fieldName = "*出口申报价币种", isNotBlank = true )
    private String declareCurrency;

    @ColumnWidth(30)
    @ExcelProperty(value = "错误信息", index = 3)
    private String errorMsg;

}
