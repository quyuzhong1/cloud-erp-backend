package com.erp.model.wms.dto.excel;

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
public class MoveInfoExcelDTO implements Serializable {

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
    @ExcelProperty(value = "仓库", index = 1)
    @FieldValid(fieldName = "仓库",isNotBlank = true,maxLength=50)
    private String warehouseName;

    /**
     * 取货仓位
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "取货仓位", index = 2)
    @FieldValid(fieldName = "取货仓位")
    private String outWarehouseLocationName;

    /**
     * 上架仓位
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "上架仓位", index = 3)
    @FieldValid(fieldName = "上架仓位",isNotBlank = true)
    private String inWarehouseLocationName;

    /**
     * 移动数量
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "移动数量", index = 4)
    @FieldValid(fieldName = "移动数量",isNotBlank = true,fieldValues = "是,否")
    private String isVirtual;

    /**
     * 备注
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "备注", index = 5)
    @FieldValid(fieldName = "备注")
    private String remark;
    /**
     * 错误信息
     */
    @ColumnWidth(200)
    @ExcelProperty(value = "错误信息", index = 6)
    private String errorMsg;
}
