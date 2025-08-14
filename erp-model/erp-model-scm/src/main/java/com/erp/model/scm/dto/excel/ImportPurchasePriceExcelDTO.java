package com.erp.model.scm.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 采购价目表导入
 * @CreateTime: 2023-08-03  17:36
 * @Author: zhangchunlin
 */
@Data
@NoArgsConstructor
public class ImportPurchasePriceExcelDTO implements Serializable {

    /**
     * 供应商名称
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "供应商名称", index = 0)
    @FieldValid(fieldName = "供应商名称",isNotBlank = true, maxLength =50 )
    private String supplierName;


    /**
     * 报价日期
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "报价日期", index = 1)
//    @FieldValid(fieldName = "报价日期",formatPattern = FieldFormatPatternTypeEnum.DATE_S)
    private String quotedDate;

    /**
     * 采购员
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "定价员", index = 2)
    private String pricingUserName;

    /**
     * 采购组织
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "采购组织", index = 3)
    @FieldValid(fieldName = "采购组织",isNotBlank = true, maxLength =50 )
    private String purchaseOrgName;

    /**
     * SKU 编号
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "SKU", index = 4)
    @FieldValid(fieldName = "SKU",isNotBlank = true, maxLength =64 )
    private String skuNo;

    /**
     * 采购交期（天）
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "采购交期（天）", index = 5)
    @FieldValid(fieldName = "采购交期（天）",formatPattern = FieldFormatPatternTypeEnum.INTEGER)
    private String deliveryDay;

    /**
     * 开始区间
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "区间从", index = 6)
    @FieldValid(fieldName = "区间从",formatPattern = FieldFormatPatternTypeEnum.INTEGER)
    private String minQty;

    /**
     * 结束区间
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "区间到", index = 7)
    @FieldValid(fieldName = "区间到",formatPattern = FieldFormatPatternTypeEnum.INTEGER)
    private String maxQty;

    /**
     * 币制
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "币制", index = 8)
    @FieldValid(fieldName = "币制", maxLength = 3)
    private String currency;

    /**
     * 含税单价
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "含税单价", index = 9)
    @FieldValid(fieldName = "含税单价", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.NUMBER)
    private String taxPrice;

    /**
     * 税率
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "税率", index = 10)
    @FieldValid(fieldName = "税率", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.NUMBER)
    private String taxRate;

    /**
     * 生效时间
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "生效时间", index = 11)
//    @FieldValid(fieldName = "生效时间",formatPattern = FieldFormatPatternTypeEnum.DATE_S)
    private String effectiveDate;

    /**
     * 失效时间
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "失效时间", index = 12)
//    @FieldValid(fieldName = "生效时间",formatPattern = FieldFormatPatternTypeEnum.DATE_S)
    private String expireDate;

    /**
     * 启用状态
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "启用状态", index = 13)
    @FieldValid(fieldName = "启用状态",isNotBlank = true,fieldValues ="启用,停用" )
    private String disabled;

    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index =14)
    @ColumnWidth(50)
    private String  errorMsg;

}