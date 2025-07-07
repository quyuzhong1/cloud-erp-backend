package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import lombok.Data;

import java.io.Serializable;

/**
 * @author hyj
 * @date 2024/4/16 15:24
 */
@Data
public class VirtualAdjustDetailExcelDTO implements Serializable {

    /**
     * SKU
     */
    @ExcelProperty(value = "*SKU", index = 0)
    @FieldValid(fieldName = "sku", isNotBlank = true,maxLength = 32)
    private String skuNo;
    @ExcelIgnore
    private String skuId;

    /**
     * 产品名称
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "产品名称", index = 1)
    @FieldValid(fieldName = "产品名称")
    private String productName;

    /**
     * 虚拟仓库
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "*虚拟仓库", index = 2)
    @FieldValid(fieldName = "虚拟仓库", isNotBlank = true,maxLength = 50)
    private String virtualWarehouseName;
    @ExcelIgnore
    private String virtualWarehouseId;

    /**
     * 库存状态
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "*库存状态", index = 3)
    @FieldValid(fieldName = "库存状态", isNotBlank = true, maxLength = 30)
    private String inventoryStatusName;
    @ExcelIgnore
    private String inventoryStatus;

    /**
     * 调整数量
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*调整数量", index = 4)
    @FieldValid(fieldName = "调整数量", isNotBlank = true, maxLength = 30)
    private String qtyStr;
    @ExcelIgnore
    private Integer qty;

    /**
     * 备注
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "备注", index = 5)
    @FieldValid(fieldName = "备注", maxLength = 200)
    private String remark;
    /**
     * 错误信息
     */
    @ColumnWidth(50)
    @ExcelProperty(value = "错误信息", index = 6)
    private String errorMsg;
}
