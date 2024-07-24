package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import lombok.Data;

import java.io.Serializable;

/**
 * 虚拟仓分货单分配导入
 *
 * @author hyj
 * @date 2024/6/11
 */
@Data
public class VwAllocationAllocationExcelDTO extends VwAllocationExcelDTO implements Serializable {

    /**
     * SKU
     */
    @ExcelProperty(value = "SKU", index = 0)
    @FieldValid(fieldName = "sku", isNotBlank = true)
    private String skuNo;

    /**
     * 仓库名称
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "实体仓", index = 1)
    @FieldValid(fieldName = "实体仓", isNotBlank = true)
    private String warehouseName;


    /**
     * 分配数量
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "分配数量", index = 2)
    @FieldValid(fieldName = "分配数量", isNotBlank = true)
    private String qty;

    /**
     * 调入虚拟仓
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "调入虚拟仓", index = 3)
    @FieldValid(fieldName = "调入虚拟仓", isNotBlank = true)
    private String toVirtualWarehouseName;

    /**
     * 备注
     */
    @ColumnWidth(50)
    @ExcelProperty(value = "备注", index = 4)
    @FieldValid(fieldName = "备注")
    private String detailRemark;

    /**
     * 错误信息
     */
    @ColumnWidth(50)
    @ExcelProperty(value = "导入错误说明", index = 5)
    private String errorMsg;
}
