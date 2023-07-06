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


    @ExcelProperty(value = "FBillHead(PUR_PurchaseOrder)")
    @FieldValid(fieldName = "单据序号", isNotBlank = true)
    private String  id;

    @ExcelProperty(value = "FBillNo")
    @FieldValid(fieldName = "单据编码", isNotBlank = true)
    private String  code;

    @ExcelProperty(value = "FDate")
    @FieldValid(fieldName = "采购日期", isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String  billDateStr;

    @ExcelProperty(value = "FSupplierId")
    @FieldValid(fieldName = "供应商编码", isNotBlank = true)
    private String  supplierCode;

    @ExcelProperty(value = "FPurchaseOrgId")
    @FieldValid(fieldName = "采购组织", isNotBlank = true)
    private String  purchaseOrgCode;

    @ExcelProperty(value = "FPurchaseDeptId")
    @FieldValid(fieldName = "采购部门", isNotBlank = true)
    private String  purchaseDeptCode;

    @ExcelProperty(value = "FPurchaserId")
    @FieldValid(fieldName = "采购员", isNotBlank = true)
    private String  purchaseUserCode;

    @ExcelProperty(value = "FProviderContactId#Name")
    @FieldValid(fieldName = "供应商联系人")
    private String  contactName;

    @ExcelProperty(value = "FProviderPhone#Name")
    @FieldValid(fieldName = "手机")
    private String  telNumber;

    @ExcelProperty(value = "FCreatorId")
    @FieldValid(fieldName = "创建人")
    private String  createUserCode;

    @ExcelProperty(value = "FCreateDate")
    @FieldValid(fieldName = "创建日期",formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String  createTimeStr;

    @ExcelProperty(value = "FModifierId")
    @FieldValid(fieldName = "修改人")
    private String  updateUserCode;

    @ExcelProperty(value = "FModifyDate")
    @FieldValid(fieldName = "修改日期",formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String  updateTimeStr;

    @ExcelProperty(value = "FApproverId")
    @FieldValid(fieldName = "审核人")
    private String  approveUserCode;

    @ExcelProperty(value = "FApproveDate")
    @FieldValid(fieldName = "审核日期",formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String  approveTimeStr;

    @ExcelProperty(value = "F_ulz_Combo")
    @FieldValid(fieldName = "新品首批")
    private String  isFirstMassProduct;

    @ExcelProperty(value = "FSettleCurrId")
    @FieldValid(fieldName = "结算币别", isNotBlank = true)
    private String  payCurrencyCode;

    @ExcelProperty(value = "FMaterialId")
    @FieldValid(fieldName = "物料编码", isNotBlank = true)
    private String  skuNo;

    @ExcelProperty(value = "FQty")
    @FieldValid(fieldName = "采购数量", isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.INTEGER)
    private String  qty;

    @ExcelProperty(value = "FDeliveryDate")
    @FieldValid(fieldName = "交货日期",formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String  planDeliveryDateStr;

    @ExcelProperty(value = "FTaxPrice")
    @FieldValid(fieldName = "含税单价", isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.DECIMAL)
    private String  taxPrice;

    @ExcelProperty(value = "F_ulz_Base")
    @FieldValid(fieldName = "交货仓库", isNotBlank = true)
    private String  warehouseCode;

    @ExcelProperty(value = "FEntryTaxRate")
    @FieldValid(fieldName = "税率",formatPattern = FieldFormatPatternTypeEnum.DECIMAL)
    private String  taxRate;

    @ExcelProperty(value = "FReceiveOrgId")
    @FieldValid(fieldName = "收料组织", isNotBlank = true)
    private String  receiveOrgCode;

    @ExcelProperty(value = "FGiveAway")
    @FieldValid(fieldName = "是否赠品")
    private String  isGift;

    @ExcelProperty(value = "FEntryNote")
    @FieldValid(fieldName = "备注")
    private String  remark;

    /**
     * 错误信息
     */
    @ExcelProperty(value = "错误信息")
    private String errorMsg;
}
