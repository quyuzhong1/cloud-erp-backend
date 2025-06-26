package com.erp.model.scm.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 采购对账明细导入
 * @author will
 * @date 2025/6/13 15:05
 */
@Data
public class PoReconciliationDetailImportExcelDTO implements Serializable {

    /**
     * 单据单号
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "*单据单号", index = 0)
    @FieldValid(fieldName = "单据单号",maxLength = 50,isNotBlank = true)
    private String sourceCode;
    /**
     * SKU
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*SKU", index = 1)
    @FieldValid(fieldName = "SKU",isNotBlank = true,maxLength = 50 )
    private String skuNo;


    /**
     * 折扣率
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "折扣率", index = 2)
    @FieldValid(fieldName = "折扣率",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String discountRate;


    /**
     * 含税单价
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "含税单价", index = 3)
    @FieldValid(fieldName = "含税单价",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String taxPrice;

    /**
     * 税率
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "税率", index = 4)
    @FieldValid(fieldName = "税率",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String taxRate;

    /**
     * 预付金额
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "预付金额", index = 5)
    @FieldValid(fieldName = "预付金额",formatPattern = FieldFormatPatternTypeEnum.AMOUNT_NORMAL)
    private String prepayAmount;

    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index = 6)
    @ColumnWidth(50)
    private String  errorMsg;
}
