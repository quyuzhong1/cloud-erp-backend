package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 样品期初台账明细导入ExcelDTO
 * @author wuhaotian
 * @Date 2025-09-01
 */
@Data
@NoArgsConstructor
public class SampleInitialLedgerDetailImportExcelDTO implements Serializable {

    /**
     * SKU编号
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*SKU编号", index = 0)
    @FieldValid(fieldName = "*SKU编号", isNotBlank = true)
    private String skuNo;

    /**
     * SKU ID (忽略)
     */
    @com.alibaba.excel.annotation.ExcelIgnore
    private String skuId;

    /**
     * 产品名称 (忽略)
     */
    @com.alibaba.excel.annotation.ExcelIgnore
    private String productName;

    /**
     * 期初数量
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*期初数量", index = 1)
    @FieldValid(fieldName = "*期初数量", isNotBlank = true)
    private String qty;

    /**
     * 明细备注
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "明细备注", index = 2)
    @FieldValid(fieldName = "明细备注", maxLength = 200)
    private String remark;

    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index = 3)
    @ColumnWidth(50)
    private String errorMsg;
}
