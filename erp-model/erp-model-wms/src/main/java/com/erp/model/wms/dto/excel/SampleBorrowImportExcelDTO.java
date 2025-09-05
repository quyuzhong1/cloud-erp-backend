package com.erp.model.wms.dto.excel;

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
public class SampleBorrowImportExcelDTO implements Serializable {


    /**
     * 序号
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "*序号", index = 0)
    @FieldValid(fieldName = "*序号",isNotBlank = true )
    private String no;


    /**
     * 借用日期
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*借用日期", index = 1)
    @FieldValid(fieldName = "*借用日期",isNotBlank = true)
    private String borrowDateStr;
    @ExcelIgnore
    private LocalDate borrowDate;
    /**
     * 预计退回日期
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*预计退回日期", index = 2)
    @FieldValid(fieldName = "*预计退回日期",isNotBlank = true)
    private String estimatedReturnDateStr;
    @ExcelIgnore
    private LocalDate estimatedReturnDate;

    /**
     * 借用人
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*借入人", index = 3)
    @FieldValid(fieldName = "*借入人",isNotBlank = true)
    private String borrowUserName;
    @ExcelIgnore
    private String borrowUserId;

    /**
     * 借用部门
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "*借入部门", index = 4)
    @FieldValid(fieldName = "*借入部门",isNotBlank = true)
    private String borrowDeptName;
    @ExcelIgnore
    private String borrowDeptId;


    /**
     * 借出人
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*借出人", index = 5)
    @FieldValid(fieldName = "*借出人",isNotBlank = true)
    private String lendUserName;
    @ExcelIgnore
    private String lendUserId;

    /**
     * 借出部门
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "*借出部门", index = 6)
    @FieldValid(fieldName = "*借出部门",isNotBlank = true)
    private String lendDeptName;
    @ExcelIgnore
    private String lendDeptId;


    /**
     * 备注
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "备注", index = 7)
    @FieldValid(fieldName = "备注",maxLength =200)
    private String remark;


    /**
     * SKU
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*SKU", index = 8)
    @FieldValid(fieldName = "*SKU",isNotBlank = true)
    private String skuNo;
    @ExcelIgnore
    private String skuId;
    @ExcelIgnore
    private String productName;


    /**
     * 使用方
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*使用方", index = 9)
    @FieldValid(fieldName = "*使用方",isNotBlank = true)
    private String useUserName;


    /**
     * 借用数量
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*借用数量", index = 10)
    @FieldValid(fieldName = "*借用数量",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER)
    private String borrowQty;


    /**
     * 明细备注
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "明细备注", index = 11)
    @FieldValid(fieldName = "明细备注",maxLength =200)
    private String detailRemark;

    @ExcelIgnore
    private String sampleLedgerId;

    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index =12)
    @ColumnWidth(50)
    private String  errorMsg;
}
