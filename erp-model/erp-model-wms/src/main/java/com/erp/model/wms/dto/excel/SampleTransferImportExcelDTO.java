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
 * 样品转移单导入Excel DTO
 * @author wuhaotian
 * @Date 2025-10-28
 */
@Data
@NoArgsConstructor
public class SampleTransferImportExcelDTO implements Serializable {

    /**
     * 序号
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "*序号", index = 0)
    @FieldValid(fieldName = "*序号", isNotBlank = true)
    private String no;

    /**
     * 转移日期
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*转移日期", index = 1)
    @FieldValid(fieldName = "*转移日期", isNotBlank = true)
    private String transferDateStr;
    @ExcelIgnore
    private LocalDate transferDate;

    /**
     * 转入人
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*转入人", index = 2)
    @FieldValid(fieldName = "*转入人", isNotBlank = true)
    private String transferInUserName;
    @ExcelIgnore
    private String transferInUserId;

    /**
     * 转入部门
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "*转入部门", index = 3)
    @FieldValid(fieldName = "*转入部门", isNotBlank = true)
    private String transferInDeptName;
    @ExcelIgnore
    private String transferInDeptId;

    /**
     * 转出人
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*转出人", index = 4)
    @FieldValid(fieldName = "*转出人", isNotBlank = true)
    private String transferOutUserName;
    @ExcelIgnore
    private String transferOutUserId;

    /**
     * 转出部门
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "*转出部门", index = 5)
    @FieldValid(fieldName = "*转出部门", isNotBlank = true)
    private String transferOutDeptName;
    @ExcelIgnore
    private String transferOutDeptId;

    /**
     * 备注
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "备注", index = 6)
    @FieldValid(fieldName = "备注", maxLength = 200)
    private String remark;

    /**
     * SKU
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*SKU", index = 7)
    @FieldValid(fieldName = "*SKU", isNotBlank = true)
    private String skuNo;
    @ExcelIgnore
    private String skuId;
    @ExcelIgnore
    private String productName;

    /**
     * 使用方
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*使用方", index = 8)
    @FieldValid(fieldName = "*使用方", isNotBlank = true)
    private String useUserName;

    /**
     * 转移数量
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*转移数量", index = 9)
    @FieldValid(fieldName = "*转移数量", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER)
    private String transferQty;

    /**
     * 明细备注
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "备注", index = 10)
    @FieldValid(fieldName = "备注", maxLength = 200)
    private String detailRemark;

    @ExcelIgnore
    private String sampleLedgerId;

    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index = 11)
    @ColumnWidth(50)
    private String errorMsg = "";
}

