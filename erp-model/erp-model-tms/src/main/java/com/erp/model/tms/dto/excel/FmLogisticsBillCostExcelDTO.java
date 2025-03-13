package com.erp.model.tms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.CurrencyEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class FmLogisticsBillCostExcelDTO implements Serializable {

    @ExcelProperty(value = "来源单号")
    @FieldValid(fieldName = "来源单号",maxLength = 32)
    private String outstockCode;

    @ExcelProperty(value = "物流运单号")
    private String transportNo;

    @ExcelProperty(value = "业务单号")
    @FieldValid(fieldName = "业务单号",maxLength = 32)
    private String businessCode;

    @ExcelProperty(value = "*费用名称")
    @FieldValid(fieldName = "费用名称",isNotBlank = true,maxLength = 32)
    private String costName;

    @ExcelProperty(value = "*预估费用金额")
    @FieldValid(fieldName = "预估费用金额",isNotBlank = true,maxLength = 32)
    private BigDecimal cost;
    
    @ExcelProperty(value = "*币种")
    @FieldValid(fieldName = "币种",isNotBlank = true,enumClass = CurrencyEnum.class)
    private String currency;

    /**
     * 错误信息
     */
    @ExcelProperty(value = "错误信息")
    private String errorMsg;
}
