package com.erp.model.tms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.CurrencyEnum;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @description: 期初头程分摊导入明细
 * @author zdy
 * @date: 2024/3/21 14:52
 */
@Data
public class InventorySkuCostDetailExcelDTO implements Serializable {


    /**
     * SKU
     */
    @ExcelProperty(value = "*SKU", index = 0)
    @FieldValid(fieldName = "SKU",isNotBlank = true,maxLength = 100)
    private String  skuNo;
    /**
     * 产品名称
     */
    @ExcelProperty(value = "产品名称", index = 1)
    @FieldValid(fieldName = "产品名称",maxLength = 200)
    private String  productName;
    /**
     * 单位(Pcs)
     */
    @ExcelProperty(value = "单位(Pcs)", index = 2)
    @FieldValid(fieldName = "单位",maxLength = 10)
    private String  unit;
    /**
     * *产品成本
     */
    @ExcelProperty(value = "*产品成本", index = 3)
    @FieldValid(fieldName = "产品成本",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.AMOUNT6)
    private String productCost;
    /**
     * 币种(默认CNY)
     */
    @ExcelProperty(value = "币种(默认CNY)", index = 4)
    @FieldValid(fieldName = "币种",enumClass = CurrencyEnum.class)
    private String currency;
    /**
     * 错误信息
     */
    @ExcelProperty(value = "错误数据", index = 5)
    private String errorMsg;
}
