package com.erp.model.plm.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;

import java.io.Serializable;


/**
 * SKU标准成本修改
 *
 * @author Jim
 * {@code @date:} 2024/08/11
 */
@Data
public class SkuStdCostUpdateExcelDTO implements Serializable {


    @ExcelProperty(value = "*SKU", index = 0)
    @FieldValid(fieldName = "SKU", maxLength = 32)
    private String skuNo;

    @ExcelProperty(value = "*标准成本(不含税)", index = 1)
    @FieldValid(fieldName = "标准成本(不含税)",isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String stdCostPrice;

    @ExcelProperty(value = "生效日期", index = 2)
    @FieldValid(fieldName = "生效日期", formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String effectiveDateStr;

    @ExcelProperty(value = "币别", index = 3)
    @FieldValid(fieldName = "币别", isNotBlank = true)
    private String currency;

    @ExcelProperty(value = "错误信息", index = 4)
    private String errorMsg;

    /**
     * 对应SKU ID
     */
    @ExcelIgnore
    private String skuId;

    /**
     * 导入的行号
     */
    @ExcelIgnore
    private Integer indexRowNum;

}
