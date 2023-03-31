package com.erp.model.scm.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author Lambda
 * @Classname PurchasePriceDetailImportExcelDTO
 * @Description TODO
 * @Date 2023-03-27 16:59
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class PurchasePriceDetailImportExcelDTO  {

    /**
     * sku
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "*SKU", index = 0)
    @FieldValid(fieldName = "SKU", isNotBlank = true)
    private String  skuNo;

    /**
     * 采购交期
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*采购交期", index = 1)
    private Integer  deliveryDay;


    /**
     * 区间-从
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "区间-从", index = 2)
    private Integer  minQty;


    /**
     * 区间-到
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "区间-到", index = 3)
    private Integer  maxQty;


    /**
     * 含税单价
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "含税单价", index = 4)
    private BigDecimal taxPrice;



    /**
     * 含税单价
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "币种", index = 5)
    @FieldValid(fieldName = "币种", isNotBlank = true)
    private String currency;


    /**
     * 税率
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "税率", index = 6)
    private BigDecimal taxRate;


    /**
     * 税率
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "生效时间", index = 7)
    @FieldValid(fieldName = "生效时间",formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String effectiveDateStr;


    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index = 8)
    private String  errorMsg;


}
