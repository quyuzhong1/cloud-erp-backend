package com.erp.model.oms.dto.excel;

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
public class LogisticsProductExcelDTO {
    @ColumnWidth(20)
    @ExcelProperty(value = "*sku", index = 0)
    @FieldValid(fieldName = "*sku", isNotBlank = true )
    private String skuNo;

    @ColumnWidth(30)
    @ExcelProperty(value = "中文报关名", index = 1)
    @FieldValid(fieldName = "中文报关名")
    private String declareChineseName;

    @ColumnWidth(30)
    @ExcelProperty(value = "英文报关名", index = 2)
    @FieldValid(fieldName = "英文报关名")
    private String declareEnglishName;

    @ColumnWidth(30)
    @ExcelProperty(value = "报关型号", index = 3)
    @FieldValid(fieldName = "报关型号")
    private String declareModel;

    @ColumnWidth(30)
    @ExcelProperty(value = "出口申报价", index = 4)
    @FieldValid(fieldName = "出口申报价",formatPattern= FieldFormatPatternTypeEnum.AMOUNT)
    private String declarePrice;

    @ColumnWidth(30)
    @ExcelProperty(value = "出口申报价币种", index = 5)
    @FieldValid(fieldName = "出口申报价币种")
    private String declareCurrency;

    @ColumnWidth(30)
    @ExcelProperty(value = "报关单位", index = 6)
    @FieldValid(fieldName = "报关单位")
    private String declareUnit;

    @ColumnWidth(30)
    @ExcelProperty(value = "中国海关编码", index = 7)
    @FieldValid(fieldName = "中国海关编码")
    private String customsCode;

    @ColumnWidth(30)
    @ExcelProperty(value = "申报要素", index = 8)
    @FieldValid(fieldName = "申报要素")
    private String declareElement;

    @ColumnWidth(30)
    @ExcelProperty(value = "境内货源地", index = 9)
    @FieldValid(fieldName = "境内货源地")
    private String sourceCargo;

    @ColumnWidth(30)
    @ExcelProperty(value = "征免", index = 10)
    @FieldValid(fieldName = "征免")
    private String exemption;

    @ColumnWidth(30)
    @ExcelProperty(value = "原产国", index = 11)
    @FieldValid(fieldName = "原产国")
    private String sourceCountry;

    @ColumnWidth(30)
    @ExcelProperty(value = "组合品申报", index = 12)
    @FieldValid(fieldName = "组合品申报")
    private String combinationDeclareType;

    @ColumnWidth(30)
    @ExcelProperty(value = "第一数量", index = 13)
    @FieldValid(fieldName = "第一数量" ,formatPattern = FieldFormatPatternTypeEnum.AMOUNT,maxLength = 16)
    private String firstQtyStr;

    @ColumnWidth(30)
    @ExcelProperty(value = "第二数量", index = 14)
    @FieldValid(fieldName = "第二数量" ,formatPattern = FieldFormatPatternTypeEnum.AMOUNT,maxLength = 16)
    private String secondQtyStr;

    @ColumnWidth(30)
    @ExcelProperty(value = "错误信息", index = 15)
    private String errorMsg;

}
