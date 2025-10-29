package com.erp.model.scm.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;
import java.io.Serializable;

/**
 * @Author: wtr
 * @Date: 2025/10/24 19:04
 * @Param:
 * @Return:
 * @Description:
 **/
@Data
public class AssetPurchaseOrderImportExcelDTO implements Serializable {

    @ExcelProperty("*序号")
    @FieldValid(fieldName = "serialNumber",isNotBlank = true)
    private String serialNumber;

    /**
     * 开模通知单
     */
    @ExcelProperty("开模通知单")
    @FieldValid(fieldName = "assetNoticeCode")
    private String  assetNoticeCode;

    /**
     * 采购日期
     */
    @ExcelProperty("*采购日期")
    @FieldValid(fieldName = "applyDate", isNotBlank = true ,formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String applyDate;

    /**
     * 采购员
     */
    @ExcelProperty("采购员")
    @FieldValid(fieldName = "purchaseUserName")
    private String purchaseUserName;

    /**
     * 采购部门
     */
    @ExcelProperty("采购部门")
    @FieldValid(fieldName = "purchaseDeptName")
    private String purchaseDeptName;

    /**
     * 采购组织
     */
    @ExcelProperty("*采购组织")
    @FieldValid(fieldName = "purchaseOrgName", isNotBlank = true )
    private String  purchaseOrgName;

    /**
     * 供应商
     */
    @ExcelProperty("*供应商")
    @FieldValid(fieldName = "supplierName", isNotBlank = true )
    private String  supplierName;


    /**
     * 结算方式
     */
    @ExcelProperty("结算方式")
    @FieldValid(fieldName = "payMethodName")
    private String payMethodName;

    /**
     * 供应商联系人
     */
    @ExcelProperty("供应商联系人")
    @FieldValid(fieldName = "contactName",isNotBlank = true)
    private String  contactName;

    /**
     * 供应商电话
     */
    @ExcelProperty("供应商电话")
    @FieldValid(fieldName = "contactTelNumber",isNotBlank = true)
    private String  contactTelNumber;

    /**
     * 付款条件
     */
    @ExcelProperty("付款条件")
    @FieldValid(fieldName = "paymentConditionName")
    private String  paymentConditionName;

    /**
     * 账户名称
     */
    @ExcelProperty("账户名称")
    @FieldValid(fieldName = "payee")
    private String  payee;

    /**
     * 模具编号
     */
    @ExcelProperty("*模具编号")
    @FieldValid(fieldName = "assetCode", isNotBlank = true )
    private String  assetCode;

    /**
     * 采购数量
     */
    @ExcelProperty("*采购数量")
    @FieldValid(fieldName = "purchaseQtyStr", isNotBlank = true )
    private String  purchaseQtyStr;

    /**
     * 预计交货日期
     */
    @ExcelProperty("*预计交货日期")
    @FieldValid(fieldName = "planDeliveryDateStr", isNotBlank = true )
    private String  planDeliveryDateStr;

    /**
     * 是否加急
     */
    @ExcelProperty("*是否加急")
    @FieldValid(fieldName = "isUrgentName", isNotBlank = true ,fieldValues = "是,否")
    private String  isUrgentName;

    /**
     * 备注
     */
    @ExcelProperty("备注")
    @FieldValid(fieldName = "remark",maxLength = 200)
    private String  remark;

    /**
     * 错误数据
     */
    @ExcelProperty("错误数据")
    private String  errorMsg;
}
