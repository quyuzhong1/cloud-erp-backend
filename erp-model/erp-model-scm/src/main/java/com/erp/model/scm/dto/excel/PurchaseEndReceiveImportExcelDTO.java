package com.erp.model.scm.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import lombok.Data;

import java.io.Serializable;

/**
 * 采购结束交货导入
 * @author Will
 * @date: 2024/3/5 11:20
 */
@Data
public class PurchaseEndReceiveImportExcelDTO implements Serializable {

    /**
     * 明细id
     */
    @ExcelIgnore
    private String detailId;

    /**
     * 采购订单
     */
    @ExcelProperty(value = "*采购订单", index = 0)
    @FieldValid(fieldName = "采购订单",isNotBlank = true,maxLength = 32)
    private String  code;

    /**
     * SKU
     */
    @ExcelProperty(value = "*SKU", index = 1)
    @FieldValid(fieldName = "SKU", isNotBlank = true,maxLength = 50)
    private String  skuNo;

    /**
     * 结束交货原因
     */
    @ExcelProperty(value = "*结束交货原因", index = 2)
    @FieldValid(fieldName = "结束交货原因", isNotBlank = true,maxLength = 200)
    private String remark;

    /**
     * 错误数据
     */
    private String  errorMsg;

}
