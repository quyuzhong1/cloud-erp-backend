package com.erp.model.plm.dto.excel;

import java.io.Serializable;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;

import lombok.Data;

@Data
public class SkuStdRetailPriceExcelDTO implements Serializable {

	/**
     * 周期
     */
    @ExcelProperty(value = "*SKU")
    @FieldValid(fieldName = "SKU", isNotBlank = true)
	private String skuNo;
	
	/**
     * 周期
     */
    @ExcelProperty(value = "*标准零售价")
    @FieldValid(fieldName = "标准零售价", isNotBlank = true , formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
	private String stdRetailPriceVat;
	
    /**
     * 库存SKU
     */
    @ExcelProperty(value = "*币别")
    @FieldValid(fieldName = "币别", isNotBlank = true)
    private String currency;
    /**
     * 仓库名称
     */
    @ExcelProperty(value = "*税率")
    @FieldValid(fieldName = "税率", isNotBlank = true , formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String vatRate;

    /**
     * 错误信息
     */
    @ExcelProperty(value = "错误信息")
    private String errorMsg;
    
}
