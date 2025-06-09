package com.erp.model.tms.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;


/**
 * 头程对账标准导入
 *
 * @author Jim
 * {@code @date:} 2024/03/22
 */
@Data
public class FirstMileReconciliationStandardExcelDTO implements Serializable {


    @ExcelProperty(value = "序号", index = 0)
    @FieldValid(fieldName = "序号",maxLength = 32)
    private String no;


    @ExcelProperty(value = "来源单号", index = 1)
    @FieldValid(fieldName = "来源单号",maxLength = 32)
    private String sourceCode;

    @ExcelProperty(value = "物流运单号", index = 2)
    @FieldValid(fieldName = "物流运单号",maxLength = 32)
    private String transportNo;

    @ExcelProperty(value = "业务单号", index = 3)
    @FieldValid(fieldName = "业务单号",maxLength = 32)
    private String businessCode;

    /**
     * 实际实重
     */
    @ExcelProperty(value = "实际实重", index = 4)
    @FieldValid(fieldName = "实际实重", formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String actualWeight;

    /**
     * 实际体积重
     */
    @ExcelProperty(value = "实际体积重", index = 5)
    @FieldValid(fieldName = "实际体积重", formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String volumeWeight;

    /**
     * 费用项
     */
    @ExcelProperty(value = "*费用项", index = 6)
    @FieldValid(fieldName = "费用项", isNotBlank = true,maxLength = 200)
    private String costName;

    /**
     * 费用金额
     */
    @ExcelProperty(value = "*费用金额", index = 7)
    @FieldValid(fieldName = "费用金额", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.AMOUNT_NORMAL)
    private String costValue;
    
    /**
     * 币种
     */
    @ExcelProperty(value = "*币种", index = 8)
    @FieldValid(fieldName = "币种", isNotBlank = true)
    private String currency;


    /**
     * 错误信息
     */
    @ExcelProperty(value = "错误信息", index = 9)
    private String errorMsg;
    @ExcelIgnore
    private String logisticsBillId;
    @ExcelIgnore
    private String costId;
    @ExcelIgnore
    private BigDecimal grossWeigh = BigDecimal.ZERO;
    @ExcelIgnore
    private BigDecimal skuCost = BigDecimal.ZERO;
}
