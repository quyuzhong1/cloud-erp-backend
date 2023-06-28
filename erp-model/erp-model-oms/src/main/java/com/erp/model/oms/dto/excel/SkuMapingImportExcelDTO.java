package com.erp.model.oms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Lambda
 * @Classname SkuMapingImportExcelDTO
 * @Description TODO
 * @Date 2023-06-28 18:09
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SkuMapingImportExcelDTO {


    /**
     * 平台名称
     */

    @ExcelProperty(value = "平台名称", index = 0)
    @FieldValid(fieldName = "平台名称", isNotBlank = true)
    private String platformName;

    /**
     * 店铺名称
     */
    @ExcelProperty(value = "店铺名称", index = 1)
    @FieldValid(fieldName = "店铺名称", isNotBlank = true)
    private String shopName;


    /**
     * 平台sku
     */
    @ExcelProperty(value = "平台sku", index = 2)
    @FieldValid(fieldName = "平台sku", isNotBlank = true)
    private String platformSkuNo;


    /**
     * 平台产品名称
     */
    @ExcelProperty(value = "平台产品名称", index = 3)
    @FieldValid(fieldName = "平台产品名称")
    private String platformSkuName;

    /**
     * 产品sku
     */
    @ExcelProperty(value = "产品sku", index = 4)
    @FieldValid(fieldName = "产品sku", isNotBlank = true)
    private String productSkuNo;


}
