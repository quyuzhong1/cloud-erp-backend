package com.erp.model.plm.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author jack
 * @Date 2025-10-14
 */
@Data
@NoArgsConstructor
public class MoldRefSkuImportExcelDTO implements Serializable {


    /**
     * 模具编码
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "*模具编码", index = 0)
    @FieldValid(fieldName = "*模具编码",isNotBlank = true )
    private String moldCode;
    @ExcelIgnore
    private String moldName;
    @ExcelIgnore
    private String moldId;
    @ExcelIgnore
    private String code;


    /**
     * SKU
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*SKU", index = 1)
    @FieldValid(fieldName = "*SKU",isNotBlank = true)
    private String skuNo;
    @ExcelIgnore
    private String skuId;
    @ExcelIgnore
    private String productName;
    /**
     * 单模产量
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*单模产量", index = 2)
    @FieldValid(fieldName = "*单模产量",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER)
    private Integer outputQty;

    /**
     * 用量
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*用量", index = 3)
    @FieldValid(fieldName = "*用量",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER)
    private Integer skuQty;

    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index =4)
    @ColumnWidth(50)
    private String  errorMsg = "";
}
