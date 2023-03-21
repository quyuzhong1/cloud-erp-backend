package com.erp.model.scm.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: 备货申请单导入DTO
 * @date 2023/3/21 11:29
 */
@Data
public class SalesDemandImportExcelDTO implements Serializable {

    /**
     * sku
     */
    @ExcelProperty(value = "*SKU", index = 0)
    @FieldValid(fieldName = "SKU", isNotBlank = true)
    private String  skuNo;

    /**
     * 是否加急
     */
    @ExcelProperty(value = "是否加急", index = 1)
    @FieldValid(fieldName = "是否加急",fieldValues = "是,否")
    private String  isUrgentStr;

    /**
     * 计划交期
     */
    @ExcelProperty(value = "计划交期", index = 2)
    @FieldValid(fieldName = "计划交期",formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String  planDeliveryDateStr;

    /**
     * 计划备货量
     */
    @ExcelProperty(value = "*计划备货量", index = 3)
    @FieldValid(fieldName = "计划备货量",formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER,maxLength = 16)
    private String  planStockQtyStr;

    /**
     * 目的仓库
     */
    @ExcelProperty(value = "*目的仓库", index = 4)
    @FieldValid(fieldName = "目的仓库", isNotBlank = true)
    private String  destWarehouseName;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注", index = 5)
    @FieldValid(fieldName = "备注",maxLength = 255)
    private String  remark;

    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index = 6)
    private String  errorMsg;
}
