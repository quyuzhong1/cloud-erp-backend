package com.erp.model.plm.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author jack
 * @Date 2025-10-16
 */
@Data
@NoArgsConstructor
public class CfgMoldReturnImportExcelDTO implements Serializable {

    /**
     * 模具编码
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "*模具编码", index = 0)
    @FieldValid(fieldName = "*模具编码",isNotBlank = true )
    private String moldCode;
    @ExcelIgnore
    private String moldName;
    @ExcelIgnore
    private String moldId;
    @ExcelIgnore
    private String supplierId;
    @ExcelIgnore
    private String supplierCode;
    @ExcelIgnore
    private String supplierName;


    /**
     * 策略状态
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*策略状态", index = 1)
    @FieldValid(fieldName = "*策略状态",isNotBlank = true)
    private String disabledName;
    @ExcelIgnore
    private Boolean disabled;
    /**
     * 返还标准
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*返还标准", index = 2)
    @FieldValid(fieldName = "*返还标准",isNotBlank = true)
    private String countDimName;
    @ExcelIgnore
    private String countDim;

    /**
     * 开始日期
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "开始日期", index = 3)
    @FieldValid(fieldName = "开始日期")
    private String startDateStr;
    @ExcelIgnore
    private LocalDate startDate;


    /**
     * 结束日期
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "结束日期", index = 4)
    @FieldValid(fieldName = "结束日期")
    private String endDateStr;
    @ExcelIgnore
    private LocalDate endDate;

    /**
     * 返还数量上限
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "*返还数量上限", index = 5)
    @FieldValid(fieldName = "*返还数量上限",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER)
    private Integer returnQtyLimit;



    /**
     * 返还金额
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*返还金额", index = 6)
    @FieldValid(fieldName = "*返还金额",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.AMOUNT4)
    private BigDecimal returnPrice;


    /**
     * 备注
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "备注", index = 7)
    @FieldValid(fieldName = "备注")
    private String mainRemark;

    /**
     * 明细备注
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "明细备注", index = 8)
    @FieldValid(fieldName = "明细备注")
    private String remark;

    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index =9)
    @ColumnWidth(50)
    private String  errorMsg = "";
}
