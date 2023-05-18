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
 * @Description TODO
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
    @FieldValid(fieldName = "SKU", isNotBlank = true)
    private String skuNo;



    /**
     * 销售数量
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "销售数量", index = 1)
    @FieldValid(fieldName = "销售数量", isNotBlank = true,formatPattern= FieldFormatPatternTypeEnum.NUMBER)
    private Integer qty;


    /**
     * 销售单价
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "销售单价", index = 2)
    @FieldValid(fieldName = "销售单价", isNotBlank = true,formatPattern= FieldFormatPatternTypeEnum.DECIMAL)
    private String  price;


    /**
     * 税率
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "税率", index = 3)
    @FieldValid(fieldName = "税率",formatPattern= FieldFormatPatternTypeEnum.DECIMAL)
    private String taxRate;


    /**
     * 是否赠品
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "是否赠品", index = 4)
    @FieldValid(fieldName = "是否赠品",fieldValues = "是,否")
    private String isGift;

    /**
     * 是否补发
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "是否补发", index = 5)
    @FieldValid(fieldName = "是否补发",fieldValues = "是,否")
    private String isReissue;



}
