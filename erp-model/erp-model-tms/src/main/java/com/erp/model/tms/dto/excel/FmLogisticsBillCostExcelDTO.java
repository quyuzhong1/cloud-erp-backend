package com.erp.model.tms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class FmLogisticsBillCostExcelDTO implements Serializable {

    @ExcelProperty(value = "来源单")
    private String outstockCode;

    @ExcelProperty(value = "运单号")
    private String transportNo;

    @ExcelProperty(value = "实际重量")
    private BigDecimal weightLogistics;

    @ExcelProperty(value = "实际体积重")
    private BigDecimal volumeWeightLogistics;

    @ExcelProperty(value = "费用名称")
    private String costName;

    @ExcelProperty(value = "预估费用")
    private BigDecimal cost;

    /**
     * 错误信息
     */
    @ExcelProperty(value = "错误信息")
    private String errorMsg;
}
