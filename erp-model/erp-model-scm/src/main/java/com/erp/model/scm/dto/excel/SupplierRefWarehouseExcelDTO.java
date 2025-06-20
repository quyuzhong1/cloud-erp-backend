package com.erp.model.scm.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import lombok.Data;

import java.io.Serializable;

/**
 * 仓库绑定导出
 * @author will
 * @date 2025/6/18 18:32
 */
@Data
public class SupplierRefWarehouseExcelDTO implements Serializable {

    /**
     * 供应商名称
     */
    @ExcelProperty(value = "*供应商名称", index = 0)
    @FieldValid(fieldName = "供应商名称", isNotBlank = true)
    private String  supplierName;

    /**
     * 仓库名称
     */
    @ExcelProperty(value = "*仓库名称", index = 1)
    @FieldValid(fieldName = "仓库名称", isNotBlank = true)
    private String  warehouseName;

    /**
     * 仓位名称
     */
    @ExcelProperty(value = "仓位名称", index = 2)
    @FieldValid(fieldName = "仓位名称")
    private String  warehouseLocationName;

    /**
     * 启用状态
     */
    @ExcelProperty(value = "启用状态", index = 3)
    @FieldValid(fieldName = "启用状态" ,fieldValues = "启用,禁用")
    private String  disabledName;

    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index = 4)
    private String  errorMsg;
}
