package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author jack
 * @Date 2025-08-26
 */
@Data
@NoArgsConstructor
public class SampleBorrowDetailImportExcelDTO implements Serializable {

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
    private String useUserName;


    /**
     * 借出数量
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*借用数量", index = 2)
    @FieldValid(fieldName = "*借用数量",isNotBlank = true)
    private String borrowQty;


    /**
     * 明细备注
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "明细备注", index = 3)
    @FieldValid(fieldName = "明细备注",maxLength =200)
    private String detailRemark;


    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index =4)
    @ColumnWidth(50)
    private String  errorMsg;
}
