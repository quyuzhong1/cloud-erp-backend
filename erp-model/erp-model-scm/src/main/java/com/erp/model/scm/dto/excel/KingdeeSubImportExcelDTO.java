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


    @ExcelProperty(value = "*单据头(序号)")
    @FieldValid(fieldName = "单据序号", isNotBlank = true)
    private String  id;

    @ExcelProperty(value = "(单据头)单据编号")
    @FieldValid(fieldName = "单据编码", isNotBlank = true)
    private String  code;

    @ExcelProperty(value = "*(单据头)委外组织#编码")
    @FieldValid(fieldName = "委外组织", isNotBlank = true)
    private String  subOrgCode;

    @ExcelProperty(value = "*(单据头)单据日期")
    @FieldValid(fieldName = "单据日期", isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.DATE_)
    private String  billDateStr;

    @ExcelProperty(value = "(单据头)采购部门编码")
    @FieldValid(fieldName = "采购部门")
    private String  purchaseDeptCode;

    @ExcelProperty(value = "(单据头)采购员编码")
    @FieldValid(fieldName = "采购员")
    private String  purchaseUserCode;


    @ExcelProperty(value = "(单据头)审核人#编码")
    @FieldValid(fieldName = "审核人")
    private String  approveUserCode;

    @ExcelProperty(value = "(单据头)审核日期")
    @FieldValid(fieldName = "审核日期",formatPattern = FieldFormatPatternTypeEnum.DATETIME_)
    private String  approveTimeStr;

    @ExcelProperty(value = "(明细)采购组织#编码")
    @FieldValid(fieldName = "采购组织", isNotBlank = true)
    private String  purchaseOrgCode;

    @ExcelProperty(value = "收料组织")
    @FieldValid(fieldName = "收料组织", isNotBlank = true)
    private String  receiveOrgCode;

    @ExcelProperty(value = "*明细(序号)")
    @FieldValid(fieldName = "明细id", isNotBlank = true)
    private String  detailId;

    @ExcelProperty(value = "*(明细)物料编码#编码")
    @FieldValid(fieldName = "物料编码", isNotBlank = true)
    private String  skuNo;

    @ExcelProperty(value = "(明细)数量")
    @FieldValid(fieldName = "采购数量", isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.INTEGER)
    private String  qty;

    @ExcelProperty(value = "(明细)领料数量")
    @FieldValid(fieldName = "领料数量", isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.INTEGER)
    private String  deliveryQty;

    @ExcelProperty(value = "(明细)含税单价")
    @FieldValid(fieldName = "含税单价", isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.DECIMAL)
    private String  taxPrice;

    @ExcelProperty(value = "(明细)交货日期")
    @FieldValid(fieldName = "交货日期Str",formatPattern = FieldFormatPatternTypeEnum.DATE_)
    private String  planDeliveryDateStr;

    @ExcelProperty(value = "(明细)供应商#编码")
    @FieldValid(fieldName = "供应商编码", isNotBlank = true)
    private String  supplierCode;

    @ExcelProperty(value = "(明细)仓库#编码")
    @FieldValid(fieldName = "仓库", isNotBlank = true)
    private String  warehouseCode;

    @ExcelProperty(value = "(明细)备注")
    @FieldValid(fieldName = "备注")
    private String  remark;

    /**
     * 错误信息
     */
    @ExcelProperty(value = "错误信息")
    private String errorMsg;
}
