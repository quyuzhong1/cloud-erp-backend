package com.erp.model.oms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


/**
 * 寄样费用导入Excel实体
 * @author will
 * @date 2025/12/3 10:11
 */
@Data
@NoArgsConstructor
public class KolSampleCostImportExcelDTO implements Serializable {


    /**
     * 销售单号
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*销售单号", index = 0)
    @FieldValid(fieldName = "销售单号",isNotBlank = true)
    private String soCode;

    /**
     * 费用项
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*费用项", index = 1)
    @FieldValid(fieldName = "费用项",isNotBlank = true)
    private String feeType;


    /**
     * 原币金额
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "*原币金额", index = 2)
    @FieldValid(fieldName = "原币金额",isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String amountStr;

    /**
     * 汇率
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*汇率", index = 3)
    @FieldValid(fieldName = "*汇率",isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String exchangeRateStr;

    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index = 4)
    @ColumnWidth(50)
    private String  errorMsg = "";
}

