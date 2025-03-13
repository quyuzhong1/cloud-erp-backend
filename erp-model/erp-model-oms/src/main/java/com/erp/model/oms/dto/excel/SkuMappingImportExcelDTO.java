package com.erp.model.oms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author Lambda
 * @Classname SkuMapingImportExcelDTO
 * @Date 2023-06-28 18:09
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SkuMappingImportExcelDTO {


    /**
     * 平台名称
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "平台名称", index = 0)
    @FieldValid(fieldName = "平台名称", isNotBlank = true)
    private String platformName;

    /**
     * 店铺名称
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "店铺名称", index = 1)
    @FieldValid(fieldName = "店铺名称", isNotBlank = true)
    private String shopName;


    /**
     * sku
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "平台sku", index = 2)
    @FieldValid(fieldName = "平台sku", isNotBlank = true)
    private String platformSkuNo;

    /**
     * 平台产品ID
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "平台产品ID", index = 3)
    @FieldValid(fieldName = "平台产品ID")
    private String platformProductId;
    /**
     * 平台产品名称
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "平台产品名称", index = 4)
    @FieldValid(fieldName = "平台产品名称")
    private String platformProductName;

    /**
     * 产品sku
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "产品sku", index = 5)
    @FieldValid(fieldName = "产品sku", isNotBlank = true)
    private String productSkuNo;
    /**
     * 产品sku
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "启用时间", index = 6)
    @FieldValid(fieldName = "启用时间", isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String enabledTime;
    /**
     * 错误信息
     */
    @ColumnWidth(100)
    @ExcelProperty(value = "错误数据", index = 7)
    private String errorMsg;


}
