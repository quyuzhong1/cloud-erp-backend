package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * @author jack
 * @Date 2025-06-26
 */
@Data
@NoArgsConstructor
public class SampleScrapImportExcelDTO implements Serializable {


    /**
     * 序号
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "*序号", index = 0)
    @FieldValid(fieldName = "*序号",isNotBlank = true )
    private String no;


    /**
     * 报废日期
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*报废日期", index = 1)
    @FieldValid(fieldName = "报废日期",isNotBlank = true)
    private String scrapDateStr;
    @ExcelIgnore
    private LocalDate scrapDate;


    /**
     * 报废人
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*报废人", index = 2)
    @FieldValid(fieldName = "*报废人",isNotBlank = true)
    private String scrapUserName;
    @ExcelIgnore
    private String scrapUserId;

    /**
     * 报废部门
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "*报废部门", index = 3)
    @FieldValid(fieldName = "*报废部门",isNotBlank = true)
    private String scrapDeptName;
    @ExcelIgnore
    private String scrapDeptId;


    /**
     * 备注
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "备注", index = 4)
    @FieldValid(fieldName = "备注",maxLength =200)
    private String remark;


    /**
     * SKU
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*SKU", index = 5)
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
    @ExcelProperty(value = "*使用方", index = 6)
    @FieldValid(fieldName = "*使用方",isNotBlank = true)
    private String useUserName;


    /**
     * 报废数量
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*报废数量", index = 7)
    @FieldValid(fieldName = "*报废数量",isNotBlank = true)
    private String scrapQty;


    /**
     * 明细备注
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "明细备注", index = 8)
    @FieldValid(fieldName = "明细备注",maxLength =200)
    private String detailRemark;

    @ExcelIgnore
    private String sampleLedgerId;


    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index =9)
    @ColumnWidth(50)
    private String  errorMsg;
}
