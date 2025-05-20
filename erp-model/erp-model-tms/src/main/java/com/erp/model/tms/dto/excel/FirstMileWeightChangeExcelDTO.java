package com.erp.model.tms.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class FirstMileWeightChangeExcelDTO implements Serializable {

    /**
     * 来源单
     */
    @ExcelProperty(value = "来源单号", index = 1)
    @FieldValid(fieldName = "来源单号",maxLength = 32)
    private String sourceCode;

    /**
     * 业务单号
     */
    @ExcelProperty(value = "业务单号", index = 2)
    @FieldValid(fieldName = "业务单号",maxLength = 32)
    private String businessCode;

    /**
     * 物流运单号
     */
    @ExcelProperty(value = "物流运单号", index = 3)
    @FieldValid(fieldName = "物流运单号",maxLength = 50)
    private String transportNo;
    /**
     * 箱号
     */
    @ExcelProperty(value = "*箱号", index = 4)
    @FieldValid(fieldName = "箱号", isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER,maxLength = 16)
    private String  boxNoStr;
    @ExcelIgnore
    private Integer boxNo;
    /**
     * 平台SKU
     */
    @ExcelProperty(value = "*平台SKU", index = 5)
    @FieldValid(fieldName = "平台SKU）",isNotBlank = true,maxLength = 200)
    private String  platformSkuNo;
    /**
     * SKU 编号
     */
    @ExcelProperty(value = "*SKU", index = 6)
    @FieldValid(fieldName = "SKU",isNotBlank = true, maxLength =64 )
    private String skuNo;
    /**
     * 单产品重量
     */
    @ExcelProperty(value = "单产品重量(kg)", index = 7)
    @FieldValid(fieldName = "单产品重量")
    private String productWeightStr;
    @ExcelIgnore
    private BigDecimal productWeight;
    /**
     * 出库重量
     */
    @ExcelProperty(value = "出库重量(kg)", index = 8)
    @FieldValid(fieldName = "出库重量")
    private String outStockWeightStr;
    @ExcelIgnore
    private BigDecimal outStockWeight;
    /**
     * 出库尺寸(cm)
     */
    @ExcelProperty(value = "出库尺寸(cm)", index = 9)
    @FieldValid(fieldName = "出库尺寸")
    private String outStockSizeStr;
    @ExcelIgnore
    private BigDecimal length;
    @ExcelIgnore
    private BigDecimal width;
    @ExcelIgnore
    private BigDecimal height;

    /**
     * 错误信息
     */
    @ExcelProperty(value = "错误信息")
    private String errorMsg;
}
