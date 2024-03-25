package com.erp.model.tms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.CurrencyEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class FmLogisticsBillFeeExcelDTO implements Serializable {

    @ExcelProperty(value = "*来源单")
    @FieldValid(fieldName = "来源单",isNotBlank = true,maxLength = 32)
    private String outstockCode;

    @ExcelProperty(value = "运单号")
    @FieldValid(fieldName = "来源单",isNotBlank = true,maxLength = 32)
    private String transportNo;

    @ExcelProperty(value = "实际重量")
    private BigDecimal counterNo;

    @ExcelProperty(value = "实际体积重")
    private BigDecimal logisticStatusName;

    @ExcelProperty(value = "费用名称")
    private String statusTime;

    @ExcelProperty(value = "预估费用")
    private BigDecimal currencyTrack;

    /**
     * 错误信息
     */
    private String errorMsg;
}
