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
 * 样品调整单Excel导入DTO
 * @author wuhaotian
 * @Date 2025-11-14
 */
@Data
@NoArgsConstructor
public class SampleAdjustmentImportExcelDTO implements Serializable {

    /**
     * 序号
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "*序号", index = 0)
    @FieldValid(fieldName = "*序号", isNotBlank = true)
    private String no;

    /**
     * 调整日期
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*调整日期", index = 1)
    @FieldValid(fieldName = "*调整日期", isNotBlank = true)
    private String adjustmentDateStr;
    @ExcelIgnore
    private LocalDate adjustmentDate;

    /**
     * 调整类型
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*调整类型", index = 2)
    @FieldValid(fieldName = "*调整类型", isNotBlank = true)
    private String adjustmentTypeName;
    @ExcelIgnore
    private String adjustmentType;

    /**
     * 调整人
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*调整人", index = 3)
    @FieldValid(fieldName = "*调整人", isNotBlank = true)
    private String adjustmentUserName;
    @ExcelIgnore
    private String adjustmentUserId;

    /**
     * 调整部门
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "*调整部门", index = 4)
    @FieldValid(fieldName = "*调整部门", isNotBlank = true)
    private String adjustmentDeptName;
    @ExcelIgnore
    private String adjustmentDeptId;

    /**
     * 备注
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "备注", index = 5)
    @FieldValid(fieldName = "备注", maxLength = 200)
    private String remark;

    /**
     * SKU
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*SKU", index = 6)
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
    @ExcelProperty(value = "*使用方", index = 7)
    @FieldValid(fieldName = "*使用方", isNotBlank = true)
    private String useUserName;
    @ExcelIgnore
    private String useUserId;

    /**
     * 实际数量
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*实际数量", index = 8)
    @FieldValid(fieldName = "*实际数量", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.INTEGER)
    private String actualQty;
    @ExcelIgnore
    private Integer actualQtyInt;

    /**
     * 明细备注
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "备注", index = 9)
    @FieldValid(fieldName = "明细备注", maxLength = 200)
    private String detailRemark;

    @ExcelIgnore
    private String sampleLedgerId;

    @ExcelIgnore
    private Integer ledgerQtyInt;

    @ExcelIgnore
    private Integer differenceQtyInt;

    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index = 10)
    @ColumnWidth(50)
    private String errorMsg = "";
}

