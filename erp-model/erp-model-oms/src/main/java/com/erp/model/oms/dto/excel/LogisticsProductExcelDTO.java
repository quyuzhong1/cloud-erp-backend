package com.erp.model.oms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
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
    @ExcelProperty(value = "产品分类", index = 0)
    @FieldValid(fieldName = "产品分类", isNotBlank = true )
    private String  categoryName;

    @ColumnWidth(20)
    @ExcelProperty(value = "SKU", index = 1)
    @FieldValid(fieldName = "SKU", isNotBlank = true )
    private String skuNo;


    @ColumnWidth(20)
    @ExcelProperty(value = "SPU(型号)", index = 2)
    @FieldValid(fieldName = "SPU(型号)")
    private String spuNo;



    @ColumnWidth(30)
    @ExcelProperty(value = "品名", index = 3)
    @FieldValid(fieldName = "品名")
    private String productName;

    @ColumnWidth(30)
    @ExcelProperty(value = "中文报关名", index = 4)
    @FieldValid(fieldName = "中文报关名")
    private String declareChineseName;


    @ColumnWidth(30)
    @ExcelProperty(value = "产品属性", index = 5)
    @FieldValid(fieldName = "产品属性")
    private String productPropertyName;

    @ColumnWidth(30)
    @ExcelProperty(value = "报关型号", index = 6)
    @FieldValid(fieldName = "报关型号")
    private String declareModel;


    @ColumnWidth(30)
    @ExcelProperty(value = "报关申报价", index = 7)
    @FieldValid(fieldName = "报关申报价",formatPattern= FieldFormatPatternTypeEnum.AMOUNT)
    private String declarePrice;

    @ColumnWidth(30)
    @ExcelProperty(value = "报关申报价币种", index = 8)
    @FieldValid(fieldName = "报关申报价币种")
    private String declareCurrency;

    @ColumnWidth(30)
    @ExcelProperty(value = "目的国申报价", index = 9)
    @FieldValid(fieldName = "目的国申报价",formatPattern= FieldFormatPatternTypeEnum.AMOUNT)
    private String destDeclarePrice;

    @ColumnWidth(30)
    @ExcelProperty(value = "目的国申报价币种", index = 10)
    @FieldValid(fieldName = "目的国申报价币种")
    private String destCurrency;

    @ColumnWidth(30)
    @ExcelProperty(value = "报关HSCODE", index = 11)
    @FieldValid(fieldName = "报关HSCODE")
    private String customsCode;

    @ColumnWidth(30)
    @ExcelProperty(value = "申报要素", index = 12)
    @FieldValid(fieldName = "申报要素")
    private String declareElement;

    @ColumnWidth(30)
    @ExcelProperty(value = "原产国", index = 13)
    @FieldValid(fieldName = "原产国")
    private String sourceCountry;

    @ColumnWidth(30)
    @ExcelProperty(value = "sku状态", index = 14)
    @FieldValid(fieldName = "sku状态")
    private String approveStatusName;


    @ColumnWidth(30)
    @ExcelProperty(value = "品牌", index = 15)
    @FieldValid(fieldName = "品牌")
    private String brandName;


    @ColumnWidth(30)
    @ExcelProperty(value = "产品经理", index = 16)
    @FieldValid(fieldName = "产品经理")
    private String chargeName;

    @ColumnWidth(30)
    @ExcelProperty(value = "创建时间", index = 17)
    @FieldValid(fieldName = "创建时间")
    private String createTime;

    @ColumnWidth(30)
    @ExcelProperty(value = "错误信息", index = 18)
    private String errorMsg;
}
