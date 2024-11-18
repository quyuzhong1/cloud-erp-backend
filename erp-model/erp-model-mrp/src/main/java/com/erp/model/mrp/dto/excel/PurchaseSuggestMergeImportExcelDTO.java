package com.erp.model.mrp.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 采购建议（合并）
 * @author will
 * @date 2024/9/2 19:30
 */
@Data
public class PurchaseSuggestMergeImportExcelDTO implements Serializable {


    /**
     * 编号
     */
    @ExcelProperty(value = "*编号", index = 0)
    @FieldValid(fieldName = "编号", isNotBlank = true, maxLength = 32)
    private String code;

    /**
     * 计划修正值
     */
    @ExcelProperty(value = "*计划修正值", index = 1)
    @FieldValid(fieldName = "计划修正值", isNotBlank = true, formatPattern= FieldFormatPatternTypeEnum.POSITIVEINTEGER)
    private String planPurchaseQty;

    /**
     * 发货备货数
     */
    @ExcelProperty(value = "*发货备货数", index = 2)
    @FieldValid(fieldName = "发货备货数", isNotBlank = true, formatPattern= FieldFormatPatternTypeEnum.POSITIVEINTEGER)
    private String purchaseStockUpQty;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注", index = 3)
    @FieldValid(fieldName = "备注", maxLength = 100)
    private String remark;

    /**
     * 错误数据
     */
    private String errorMsg;
}
