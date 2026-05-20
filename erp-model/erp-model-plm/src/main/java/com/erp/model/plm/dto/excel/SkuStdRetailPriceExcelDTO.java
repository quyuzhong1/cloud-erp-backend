package com.erp.model.plm.dto.excel;

import java.io.Serializable;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;

import lombok.Data;

@Data
public class SkuStdRetailPriceExcelDTO implements Serializable {

	/**
     * SKU
     */
    @ExcelProperty(value = "*SKU")
    @FieldValid(fieldName = "SKU", isNotBlank = true)
	private String skuNo;
	
	/**
     * 标准零售价
     */
    @ExcelProperty(value = "*标准零售价")
    @FieldValid(fieldName = "标准零售价", isNotBlank = true , formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
	private String stdRetailPriceVat;
	
    /**
     * 币别
     */
    @ExcelProperty(value = "*币别")
    @FieldValid(fieldName = "币别", isNotBlank = true)
    private String currency;
    /**
     * 税率
     */
    @ExcelProperty(value = "税率")
    @FieldValid(fieldName = "税率" , formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String vatRate;

    /**
     * 错误信息
     */
    @ExcelProperty(value = "错误信息")
    private String errorMsg;
    
}
