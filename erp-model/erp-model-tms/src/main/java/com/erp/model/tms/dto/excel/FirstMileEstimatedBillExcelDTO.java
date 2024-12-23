package com.erp.model.tms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.CurrencyEnum;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 头程暂估账单
 * @date 2024-08-21
 * @author tanmujin
 */
@Data
public class FirstMileEstimatedBillExcelDTO implements Serializable {
    /**
     * 业务单号
     */
    @ExcelProperty(value = "业务单号", index = 0)
    @FieldValid(fieldName = "业务单号", maxLength = 32)
    private String businessCode;

    /**
     * 物流运单号
     */
    @ExcelProperty(value = "物流运单号", index = 1)
    @FieldValid(fieldName = "物流运单号", maxLength = 32, isNotBlank = true)
    private String transportNo;

    /**
     * 来源单号
     */
    @ExcelProperty(value = "来源单号", index = 2)
    @FieldValid(fieldName = "来源单号", maxLength = 32)
    private String sourceCode;

    /**
     * 物流跟踪号
     */
    @ExcelProperty(value = "物流跟踪号", index = 3)
    @FieldValid(fieldName = "物流跟踪号", maxLength = 32)
    private String trackNo;

    /**
     * 费用名称
     */
    @ExcelProperty(value = "*费用名称", index = 4)
    @FieldValid(fieldName = "费用名称", maxLength = 32, isNotBlank = true)
    private String costName;

    /**
     * 预付费用
     */
    @ExcelProperty(value = "*预付费用", index = 5)
    @FieldValid(fieldName = "预付费用", maxLength = 32, isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.AMOUNT2)
    private BigDecimal costValue;
    /**
     * 币种
     */
    @ExcelProperty(value = "*币种", index = 6)
    @FieldValid(fieldName = "币种",isNotBlank = true,enumClass = CurrencyEnum.class)
    private String  currency;

    /**
     * 错误信息
     */
    private String errorMsg;
}
