package com.erp.model.scm.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/27 15:58
 */
@Data
public class PurchaseOrderImportExcelDTO implements Serializable {


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
     * 采购数量
     */
    @ExcelProperty(value = "*采购数量", index = 2)
    @FieldValid(fieldName = "采购数量", isNotBlank = true ,formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER,maxLength = 16)
    private String  purchaseQtyStr;

    /**
     * 计划交期
     */
    @ExcelProperty(value = "计划交期", index = 3)
    @FieldValid(fieldName = "计划交期",formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String  planDeliveryDateStr;

    /**
     * 收料组织
     */
    @ExcelProperty(value = "*收料组织", index = 4)
    @FieldValid(fieldName = "收料组织",isNotBlank = true)
    private String  receiveOrgName;


    /**
     * 交货仓库
     */
    @ExcelProperty(value = "*交货仓库", index = 5)
    @FieldValid(fieldName = "交货仓库", isNotBlank = true)
    private String  deliveryWarehouseName;

    /**
     * 是否赠品
     */
    @ExcelProperty(value = "*是否赠品", index = 6)
    @FieldValid(fieldName = "是否赠品", isNotBlank = true,fieldValues = "是,否")
    private String  isGiftStr;


    /**
     * 备注
     */
    @ExcelProperty(value = "备注", index = 7)
    @FieldValid(fieldName = "备注",maxLength = 255)
    private String  remark;

    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index = 8)
    private String  errorMsg;

}
