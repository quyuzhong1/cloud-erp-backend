package com.erp.model.scm.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import com.erp.model.plm.enums.FirstMassProductTypeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 采购订单主表信息导入
 * @author will
 * @date 2025/7/30 18:08
 */
@Data
public class PurchaseOrderMainExcelDTO implements Serializable {


    /**
     * 序号
     */
    @ExcelProperty(value = "*序号", index = 0)
    @FieldValid(fieldName = "序号", isNotBlank = true, maxLength = 50)
    private String index;

    /**
     * 采购日期
     */
    @ExcelProperty(value = "*采购日期", index = 1)
    @FieldValid(fieldName = "采购日期", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String purchaseDateStr;

    /**
     * 采购组织
     */
    @ExcelProperty(value = "*采购组织", index = 2)
    @FieldValid(fieldName = "采购组织", isNotBlank = true, maxLength = 50)
    private String purchaseOrgName;

    /**
     * 交货仓库
     */
    @ExcelProperty(value = "*交货仓库", index = 3)
    @FieldValid(fieldName = "交货仓库" , isNotBlank = true, maxLength = 50)
    private String deliveryWarehouseName;

    /**
     * 供应商
     */
    @ExcelProperty(value = "*供应商", index = 4)
    @FieldValid(fieldName = "供应商", isNotBlank = true, maxLength = 50)
    private String supplierName;

    /**
     * 结算方式
     */
    @ExcelProperty(value = "结算方式", index = 5)
    @FieldValid(fieldName = "结算方式" , maxLength = 50)
    private String payMethodName;
    /**
     * 供应商联系人
     */
    @ExcelProperty(value = "供应商联系人", index = 6)
    @FieldValid(fieldName = "供应商联系人", maxLength = 50)
    private String contactName;
    /**
     * 供应商电话
     */
    @ExcelProperty(value = "供应商电话", index = 7)
    @FieldValid(fieldName = "供应商电话",  maxLength = 50)
    private String contactTelNumber;

    /**
     * 付款条件
     */
    @ExcelProperty(value = "付款条件", index = 8)
    @FieldValid(fieldName = "付款条件", maxLength = 50)
    private String paymentConditionName;

    /**
     * 账户名称
     */
    @ExcelProperty(value = "账户名称", index = 9)
    @FieldValid(fieldName = "账户名称" , maxLength = 50 )
    private String supplierAccountName;

    /**
     * SKU
     */
    @ExcelProperty(value = "*SKU", index = 10)
    @FieldValid(fieldName = "SKU", isNotBlank = true, maxLength = 50)
    private String skuNo;


    /**
     * 采购数量
     */
    @ExcelProperty(value = "*采购数量", index = 11)
    @FieldValid(fieldName = "采购数量" , isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.INTEGER)
    private String purchaseQtyStr;


    /**
     * 预计交货日期
     */
    @ExcelProperty(value = "*预计交货日期", index = 12)
    @FieldValid(fieldName = "预计交货日期", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String planDeliveryDateStr;

    /**
     * 是否是赠品（false否，true是）
     */
    @ExcelProperty(value = "是否是赠品", index = 13)
    @FieldValid(fieldName = "是否是赠品" , maxLength = 50 ,fieldValues = "是,否")
    private String isGiftStr;

    /**
     * 是否加急
     */
    @ExcelProperty(value = "是否加急", index = 14)
    @FieldValid(fieldName = "是否加急" , maxLength = 50 ,fieldValues = "是,否")
    private String isUrgentStr;


    /**
     * 新品首批
     */
    @ExcelProperty(value = "*新品首批", index = 15)
    @FieldValid(fieldName = "新品首批", isNotBlank = true , maxLength = 50 ,enumClass = FirstMassProductTypeEnum.class)
    private String firstMassProductName;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注", index = 16)
    @FieldValid(fieldName = "备注" , maxLength = 255 )
    private String remark;

    /**
     * 错误数据
     */
    private String errorMsg;

}
