package com.erp.model.plm.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * @author jack
 * @version 1.0

 * @date 2025-07-30
 */
@Data
public class LogisticsProductErrorExcelDTO implements Serializable {

    /**
     * SKU
     */
    @ExcelProperty(value = "*SKU", index = 0)
    private String skuNo;

    /**
     * 出口申报价
     */
    @ExcelProperty(value = "*出口申报价", index = 1)
    private String declarePrice;

    /**
     * 出口申报价币种
     */
    @ExcelProperty(value = "*出口申报价币种", index = 2)
    private String declareCurrency;

    /**
     * 错误信息
     */
    @ExcelProperty(value = "错误信息", index = 4)
    private String errorMsg;



}
