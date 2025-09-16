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
 * 样品期初台账Excel导入DTO
 * @author wuhaotian
 * @Date 2025-08-25
 */
@Data
@NoArgsConstructor
public class SampleInitialLedgerImportExcelDTO implements Serializable {

    /**
     * 序号
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "*序号", index = 0)
    @FieldValid(fieldName = "*序号", isNotBlank = true)
    private String no;

    /**
     * 单据日期
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*日期", index = 1)
    @FieldValid(fieldName = "*日期", isNotBlank = true)
    private String billDateStr;
    @ExcelIgnore
    private LocalDate billDate;

    /**
     * 归属人
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*归属人", index = 2)
    @FieldValid(fieldName = "*归属人", isNotBlank = true)
    private String userName;
    @ExcelIgnore
    private String userId;

    /**
     * 归属部门
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "*归属部门", index = 3)
    @FieldValid(fieldName = "*归属部门", isNotBlank = true)
    private String deptName;
    @ExcelIgnore
    private String deptId;



    /**
     * SKU
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*SKU", index = 4)
    @FieldValid(fieldName = "*SKU", isNotBlank = true)
    private String skuNo;
    @ExcelIgnore
    private String skuId;
    @ExcelIgnore
    private String productName;

    /**
     * 数量
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*数量", index = 5)
    @FieldValid(fieldName = "*数量", isNotBlank = true)
    private String qty;

    /**
     * 备注
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "备注", index = 6)
    @FieldValid(fieldName = "备注", maxLength = 200)
    private String remark;
    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index = 7)
    @ColumnWidth(50)
    private String errorMsg;
}
