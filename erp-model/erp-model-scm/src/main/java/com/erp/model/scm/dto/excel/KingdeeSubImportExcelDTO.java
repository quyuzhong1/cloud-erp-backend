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
public class KingdeeSubImportExcelDTO {


    @ExcelProperty(value = "FBillHead(SUB_SUBREQORDER)")
    @FieldValid(fieldName = "单据序号", isNotBlank = true)
    private String  id;

    @ExcelProperty(value = "FBillNo")
    @FieldValid(fieldName = "单据编码", isNotBlank = true)
    private String  code;

    @ExcelProperty(value = "FSubOrgId")
    @FieldValid(fieldName = "委外组织", isNotBlank = true)
    private String  subOrgCode;

    @ExcelProperty(value = "FPurorgId")
    @FieldValid(fieldName = "采购组织", isNotBlank = true)
    private String  purchaseOrgCode;

    @ExcelProperty(value = "收料组织")
    @FieldValid(fieldName = "收料组织", isNotBlank = true)
    private String  receiveOrgCode;

    @ExcelProperty(value = "FApproverId")
    @FieldValid(fieldName = "审核人")
    private String  approveUserCode;

    @ExcelProperty(value = "FApproveDate")
    @FieldValid(fieldName = "审核日期",formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String  approveTimeStr;

    @ExcelProperty(value = "FTreeEntity")
    @FieldValid(fieldName = "明细id", isNotBlank = true)
    private String  detailId;

    @ExcelProperty(value = "FMaterialId")
    @FieldValid(fieldName = "物料编码", isNotBlank = true)
    private String  skuNo;

    @ExcelProperty(value = "FQty")
    @FieldValid(fieldName = "采购数量", isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.INTEGER)
    private String  qty;

    @ExcelProperty(value = "领料数量")
    @FieldValid(fieldName = "采购数量", isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.INTEGER)
    private String  deliveryQty;

    @ExcelProperty(value = "含税单价")
    @FieldValid(fieldName = "含税单价", isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.DECIMAL)
    private String  taxPrice;

    @ExcelProperty(value = "交货日期")
    @FieldValid(fieldName = "交货日期Str",formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String  planDeliveryDateStr;

    @ExcelProperty(value = "FSettleCurrId")
    @FieldValid(fieldName = "结算币别", isNotBlank = true)
    private String  payCurrencyCode;

    @ExcelProperty(value = "FPlanStartDate")
    @FieldValid(fieldName = "计划开工时间",formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String  billDateStr;

    @ExcelProperty(value = "采购部门编码")
    @FieldValid(fieldName = "采购部门")
    private String  purchaseDeptCode;

    @ExcelProperty(value = "采购员编码")
    @FieldValid(fieldName = "采购员")
    private String  purchaseUserCode;

    @ExcelProperty(value = "FSupplierId")
    @FieldValid(fieldName = "供应商编码", isNotBlank = true)
    private String  supplierCode;

    @ExcelProperty(value = "FStockID")
    @FieldValid(fieldName = "仓库", isNotBlank = true)
    private String  warehouseCode;

    @ExcelProperty(value = "FDescription1")
    @FieldValid(fieldName = "备注")
    private String  remark;

    /**
     * 错误信息
     */
    @ExcelProperty(value = "错误信息")
    private String errorMsg;
}
