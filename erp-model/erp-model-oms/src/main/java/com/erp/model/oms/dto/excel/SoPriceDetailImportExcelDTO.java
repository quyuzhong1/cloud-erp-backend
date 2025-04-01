package com.erp.model.oms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 销售价目明细导入
 * @author will
 * @date 2025/3/25 14:58
 */
@Data
@NoArgsConstructor
public class SoPriceDetailImportExcelDTO {

    /**
     * sku
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "*SKU", index = 0)
    @FieldValid(fieldName = "SKU", isNotBlank = true, maxLength =64)
    private String  skuNo;

    /**
     * 区间-从
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*区间-从", index = 1)
    @FieldValid(fieldName = "区间-从",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.INTEGER)
    private Integer  minQty;

    /**
     * 区间-到
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*区间-到", index = 2)
    @FieldValid(fieldName = "区间-到",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.INTEGER)
    private Integer  maxQty;

    /**
     * 含税单价
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*含税单价", index = 3)
    @FieldValid(fieldName = "含税单价", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.NUMBER)
    private BigDecimal taxPrice;

    /**
     * 税率
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*税率", index = 4)
    @FieldValid(fieldName = "税率", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.NUMBER)
    private BigDecimal taxRate;


    /**
     * 税率
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*生效时间", index = 5)
    @FieldValid(fieldName = "生效时间", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String effectiveDateStr;

    /**
     * 税率
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*失效时间", index = 6)
    @FieldValid(fieldName = "失效时间", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String expireDateStr;


    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index = 7)
    private String  errorMsg;


}
