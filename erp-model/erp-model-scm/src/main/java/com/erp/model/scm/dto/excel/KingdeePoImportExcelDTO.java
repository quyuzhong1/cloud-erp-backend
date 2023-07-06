package com.erp.model.scm.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;

/**
 * @author Will
 * @version 1.0
 * @description: 金蝶采购订单导入DTO
 * @date 2023/7/5 15:12
 */
@Data
public class KingdeePoImportExcelDTO {


    @ExcelProperty(value = "*基本信息(序号)" )
    @FieldValid(fieldName = "单据序号", isNotBlank = true)
    private String  id;

    @ExcelProperty(value = "(基本信息)单据编号" )
    @FieldValid(fieldName = "单据编码", isNotBlank = true)
    private String  code;

    @ExcelProperty(value = "*(基本信息)采购日期" )
    @FieldValid(fieldName = "采购日期", isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.DATE_)
    private String  billDateStr;

    @ExcelProperty(value = "*(基本信息)供应商#编码" )
    @FieldValid(fieldName = "供应商编码", isNotBlank = true)
    private String  supplierCode;

    @ExcelProperty(value = "*(基本信息)采购组织#编码" )
    @FieldValid(fieldName = "采购组织", isNotBlank = true)
    private String  purchaseOrgCode;

    @ExcelProperty(value = "(基本信息)采购部门#编码" )
    @FieldValid(fieldName = "采购部门", isNotBlank = true)
    private String  purchaseDeptCode;

    @ExcelProperty(value = "(基本信息)采购员#编码" )
    @FieldValid(fieldName = "采购员", isNotBlank = true)
    private String  purchaseUserCode;

    @ExcelProperty(value = "(基本信息)供货方联系人#名称" )
    @FieldValid(fieldName = "供应商联系人")
    private String  contactName;

    @ExcelProperty(value = "(基本信息)手机" )
    @FieldValid(fieldName = "手机")
    private String  telNumber;

    @ExcelProperty(value = "(基本信息)审核人#编码" )
    @FieldValid(fieldName = "审核人")
    private String  approveUserCode;

    @ExcelProperty(value = "(基本信息)审核日期" )
    @FieldValid(fieldName = "审核日期",formatPattern = FieldFormatPatternTypeEnum.DATE_)
    private String  approveTimeStr;

    @ExcelProperty(value = "(基本信息)新品首批" )
    @FieldValid(fieldName = "新品首批")
    private String  isFirstMassProduct;

    @ExcelProperty(value = "*(财务信息)结算币别#编码" )
    @FieldValid(fieldName = "结算币别", isNotBlank = true)
    private String  payCurrencyCode;

    @ExcelProperty(value = "*(明细信息)物料编码#编码" )
    @FieldValid(fieldName = "物料编码", isNotBlank = true)
    private String  skuNo;

    @ExcelProperty(value = "(明细信息)采购数量" )
    @FieldValid(fieldName = "采购数量", isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.INTEGER)
    private String  qty;

    @ExcelProperty(value = "(明细信息)交货日期" )
    @FieldValid(fieldName = "交货日期",formatPattern = FieldFormatPatternTypeEnum.DATETIME_)
    private String  planDeliveryDateStr;

    @ExcelProperty(value = "(明细信息)含税单价" )
    @FieldValid(fieldName = "含税单价", isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.DECIMAL)
    private String  taxPrice;

    @ExcelProperty(value = "(明细信息)交货仓库#编码" )
    @FieldValid(fieldName = "交货仓库", isNotBlank = true)
    private String  warehouseCode;

    @ExcelProperty(value = "(明细信息)税率%" )
    @FieldValid(fieldName = "税率",formatPattern = FieldFormatPatternTypeEnum.DECIMAL)
    private String  taxRate;

    @ExcelProperty(value = "(明细信息)收料组织#编码" )
    @FieldValid(fieldName = "收料组织", isNotBlank = true)
    private String  receiveOrgCode;

    @ExcelProperty(value = "(明细信息)是否赠品" )
    @FieldValid(fieldName = "是否赠品")
    private String  isGift;

    @ExcelProperty(value = "(明细信息)备注" )
    @FieldValid(fieldName = "备注")
    private String  remark;

    /**
     * 错误信息
     */
    @ExcelProperty(value = "错误信息" )
    private String errorMsg;
}
