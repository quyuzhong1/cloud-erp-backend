package com.erp.model.oms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description
 * @Author jack
 * @Date 2025-07-10
 */
@Data
@NoArgsConstructor
public class ProductCustomsExcelDTO {

    @ColumnWidth(20)
    @ExcelProperty(value = "*SKU", index = 0)
    @FieldValid(fieldName = "*SKU", isNotBlank = true )
    private String skuNo;



    @ColumnWidth(30)
    @ExcelProperty(value = "*国家", index = 1)
    @FieldValid(fieldName = "*国家", isNotBlank = true )
    private String countryName;


    @ColumnWidth(30)
    @ExcelProperty(value = "目的国清关英文名", index = 2)
    @FieldValid(fieldName = "目的国清关英文名")
    private String destinationCustomsEnName;

    @ColumnWidth(30)
    @ExcelProperty(value = "*目的国申报价$", index = 3)
    @FieldValid(fieldName = "*目的国申报价$", isNotBlank = true ,formatPattern= FieldFormatPatternTypeEnum.AMOUNT2)
    private String toDeclarePrice;

    @ColumnWidth(30)
    @ExcelProperty(value = "目的国海关编码", index = 4)
    @FieldValid(fieldName = "目的国海关编码")
    private String destinationCustomsCode;

    @ColumnWidth(30)
    @ExcelProperty(value = "目的国关税税率%", index = 5)
    @FieldValid(fieldName = "目的国关税税率%",formatPattern= FieldFormatPatternTypeEnum.AMOUNT2)
    private String taxRate;

    @ColumnWidth(30)
    @ExcelProperty(value = "目的国增值税税率%", index = 6)
    @FieldValid(fieldName = "目的国增值税税率%",formatPattern= FieldFormatPatternTypeEnum.AMOUNT2)
    private String destinationVatRate;

    @ColumnWidth(30)
    @ExcelProperty(value = "目的国附加关税税率%", index = 7)
    @FieldValid(fieldName = "目的国附加关税税率%",formatPattern= FieldFormatPatternTypeEnum.AMOUNT2)
    private String destinationAdditionalDutyRate;

    @ColumnWidth(30)
    @ExcelProperty(value = "目的国反倾销税税率%", index = 8)
    @FieldValid(fieldName = "目的国反倾销税税率%",formatPattern= FieldFormatPatternTypeEnum.AMOUNT2)
    private String destinationAntiDumpingDutyRate;

    @ColumnWidth(30)
    @ExcelProperty(value = "目的国其他税率%", index = 9)
    @FieldValid(fieldName = "目的国其他税率%",formatPattern= FieldFormatPatternTypeEnum.AMOUNT2)
    private String destinationOtherTaxRate;

    @ColumnWidth(30)
    @ExcelProperty(value = "错误信息", index = 10)
    private String errorMsg;
}
