package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 销售退货入库单批量更新（主表）
 */
@Data
public class SoReturnStockUpdateImportExcelDTO implements Serializable {

    @ExcelProperty(value = "*退货入库单号", index = 0)
    @FieldValid(fieldName = "退货入库单号", isNotBlank = true, maxLength = 50)
    private String code;

    @ExcelProperty(value = "*退货客户", index = 1)
    @FieldValid(fieldName = "退货客户", isNotBlank = true, maxLength = 50)
    private String customerName;

    @ExcelProperty(value = "*库存组织", index = 2)
    @FieldValid(fieldName = "库存组织", isNotBlank = true, maxLength = 100)
    private String inventoryOrgName;

    @ExcelProperty(value = "币种", index = 3)
    @FieldValid(fieldName = "币种", maxLength = 50)
    private String currencyStr;

    @ExcelProperty(value = "入库日期", index = 4)
    @FieldValid(fieldName = "入库日期")
    private String billDateStr;

    @ExcelIgnore
    private LocalDate billDate;

    @ExcelProperty(value = "单据类型", index = 5)
    @FieldValid(fieldName = "单据类型", maxLength = 50)
    private String typeName;

    @ExcelIgnore
    private String typeCode;

    @ExcelProperty(value = "退货物流单号", index = 6)
    @FieldValid(fieldName = "退货物流单号", maxLength = 100)
    private String returnLogisticCode;

    @ExcelProperty(value = "错误信息", index = 7)
    private String errorMsg;
}
