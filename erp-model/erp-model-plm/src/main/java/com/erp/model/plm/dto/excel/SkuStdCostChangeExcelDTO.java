package com.erp.model.plm.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;


/**
 * SKU标准成本变更
 *
 * @author Jim
 * {@code @date:} 2024/08/11
 */
@Data
public class SkuStdCostChangeExcelDTO implements Serializable {


    @ExcelProperty(value = "*SKU", index = 0)
    @FieldValid(fieldName = "SKU", maxLength = 32)
    private String sku;

    @ExcelProperty(value = "*标准成本(不含税)", index = 1)
    @FieldValid(fieldName = "标准成本(不含税)",isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String stdCostPrice;

    @ExcelProperty(value = "生效日期", index = 2)
    @FieldValid(fieldName = "生效日期", formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String effectiveDate;

    @ExcelProperty(value = "币别", index = 3)
    @FieldValid(fieldName = "币别", isNotBlank = true)
    private String currency;

    @ExcelProperty(value = "错误信息", index = 4)
    private String errorMsg;

}
