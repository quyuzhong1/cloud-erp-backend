package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 销售退货入库批量导入覆盖
 */
@Data
public class SoReturnStockOverwriteImportExcelDTO implements Serializable {

    /**
     * 退货入库单号
     */
    @ExcelProperty(value = "*退货入库单号", index = 0)
    @FieldValid(fieldName = "退货入库单号", isNotBlank = true, maxLength = 50)
    private String code;

    /**
     * 退货客户
     */
    @ExcelProperty(value = "*退货客户", index = 1)
    @FieldValid(fieldName = "退货客户", isNotBlank = true, maxLength = 50)
    private String customerName;

    /**
     * 库存组织
     */
    @ExcelProperty(value = "*库存组织", index = 2)
    @FieldValid(fieldName = "库存组织", isNotBlank = true, maxLength = 100)
    private String inventoryOrgName;

    /**
     * 币种
     */
    @ExcelProperty(value = "*币种", index = 3)
    @FieldValid(fieldName = "币种", isNotBlank = true, maxLength = 50)
    private String currencyStr;

    /**
     * 入库日期
     */
    @ExcelProperty(value = "*入库日期", index = 4)
    @FieldValid(fieldName = "入库日期", isNotBlank = true)
    private String billDateStr;

    @ExcelIgnore
    private LocalDate billDate;

    /**
     * 单据类型
     */
    @ExcelProperty(value = "*单据类型", index = 5)
    @FieldValid(fieldName = "单据类型", isNotBlank = true, maxLength = 50)
    private String typeName;

    /**
     * 退货物流单号
     */
    @ExcelProperty(value = "退货物流单号", index = 6)
    @FieldValid(fieldName = "退货物流单号", maxLength = 100)
    private String returnLogisticCode;

    /**
     * 客户sku
     */
    @ExcelProperty(value = "*客户sku", index = 7)
    @FieldValid(fieldName = "客户sku", isNotBlank = true, maxLength = 100)
    private String platformSkuNo;

    /**
     * SKU
     */
    @ExcelProperty(value = "*SKU", index = 8)
    @FieldValid(fieldName = "SKU", isNotBlank = true)
    private String skuNo;

    /**
     * 上架数量
     */
    @ExcelProperty(value = "*上架数量", index = 9)
    @FieldValid(fieldName = "上架数量", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.INTEGER)
    private String realQtyStr;

    /**
     * 单价
     */
    @ExcelProperty(value = "*单价", index = 10)
    @FieldValid(fieldName = "单价", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String priceStr;

    /**
     * 税率
     */
    @ExcelProperty(value = "*税率", index = 11)
    @FieldValid(fieldName = "税率", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String taxRateStr;

    /**
     * 含税单价
     */
    @ExcelProperty(value = "*含税单价", index = 12)
    @FieldValid(fieldName = "含税单价", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String taxPriceStr;

    /**
     * 错误信息
     */
    @ExcelProperty(value = "错误信息", index = 13)
    private String errorMsg;
}
