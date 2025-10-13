package com.erp.model.oms.dto;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * @author jack
 * @Date 2025-08-26
 */
@Data
@NoArgsConstructor
public class ExhibitionOrderImportDetailExcelDTO implements Serializable {

    /**
     * SKU
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*SKU", index = 0)
    @FieldValid(fieldName = "*SKU",isNotBlank = true)
    private String skuNo;

    /**
     * 使用方
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*使用方", index = 1)
    @FieldValid(fieldName = "*使用方",isNotBlank = true)
    private String useUserName ="";


    /**
     * 销售数量
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*销售数量", index = 2)
    @FieldValid(fieldName = "*销售数量",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER)
    private String qty;

    /**
     * 销售单价
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "销售单价", index = 3)
    @FieldValid(fieldName = "销售单价",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.AMOUNT4)
    private String price;
    /**
     * 税率(%)
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "税率(%)", index = 4)
    @FieldValid(fieldName = "税率(%)",formatPattern = FieldFormatPatternTypeEnum.AMOUNT2)
    private String taxRate;

    /**
     *  含税单价
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "含税单价", index = 5)
    @FieldValid(fieldName = "含税单价",formatPattern = FieldFormatPatternTypeEnum.AMOUNT4)
    private String taxPrice;
    /**
     * 是否赠品
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*是否赠品", index = 6)
    @FieldValid(fieldName = "*是否赠品",isNotBlank = true)
    private String isGift;

    /**
     * 明细备注
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "明细备注", index = 7)
    @FieldValid(fieldName = "明细备注",maxLength =200)
    private String remark;

    @ExcelIgnore
    private String sampleLedgerId;

    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index =8)
    @ColumnWidth(50)
    private String  errorMsg = "";
}
