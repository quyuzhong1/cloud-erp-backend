package com.erp.model.oms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 导出的错误数据
 * @author Lambda
 * @Classname SoDetailImportExcelDTO

 * @Date 2023-05-17 19:48
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SoDetailImportExcelDTO {


    /**
     * sku
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "SKU", index = 0)
    private String skuNo;

    /**
     * 客户sku
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "SKU", index = 1)
    private String platformSkuNo;

    /**
     * 销售数量
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "销售数量", index = 2)
    @FieldValid(fieldName = "销售数量", isNotBlank = true,formatPattern= FieldFormatPatternTypeEnum.NUMBER)
    private String qty;


    /**
     * 销售单价
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "销售单价", index = 3)
    @FieldValid(fieldName = "销售单价", formatPattern= FieldFormatPatternTypeEnum.AMOUNT)
    private String  price;


    /**
     * 税率
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "税率", index = 4)
    @FieldValid(fieldName = "税率",formatPattern= FieldFormatPatternTypeEnum.AMOUNT)
    private String taxRate;

    /**
     * 含税单价
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "含税单价", index = 5)
    @FieldValid(fieldName = "含税单价", formatPattern= FieldFormatPatternTypeEnum.AMOUNT)
    private String  taxPrice;

    /**
     * 是否赠品
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "是否赠品", index = 6)
    @FieldValid(fieldName = "是否赠品",isNotBlank = true,fieldValues = "是,否")
    private String isGift;

    /**
     * 是否补发
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "是否补发", index = 7)
    @FieldValid(fieldName = "是否补发",isNotBlank = true,fieldValues = "是,否")
    private String isReissue;
    /**
     * 客户PO号
     */
    @ColumnWidth(40)
    @ExcelProperty(value = "客户PO号", index = 8)
    @FieldValid(fieldName = "客户PO号",maxLength=30)
    private String customerPO;
    /**
     * 目的地
     */
    @ColumnWidth(40)
    @ExcelProperty(value = "目的地", index = 9)
    @FieldValid(fieldName = "目的地",maxLength=100)
    private String toCountry;

    /**
     * 备注
     */
    @ColumnWidth(40)
    @ExcelProperty(value = "备注", index = 10)
    @FieldValid(fieldName = "备注",maxLength=200)
    private String remark;



    /**
     * 错误信息
     */
    @ColumnWidth(100)
    @ExcelProperty(value = "错误数据", index = 11)
    private String errorMsg;




}
