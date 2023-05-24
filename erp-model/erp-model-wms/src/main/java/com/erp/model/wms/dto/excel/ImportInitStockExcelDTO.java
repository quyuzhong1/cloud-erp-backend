package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * @CreateTime: 2023-05-12  09:45
 * @Author: zhangchunlin
 */
@Data
public class ImportInitStockExcelDTO implements Serializable {


    /**
     * sku
     */
    @ExcelProperty(value = "*SKU", index = 0)
    @FieldValid(fieldName = "SKU", isNotBlank = true)
    private String  skuNo;

    /**
     * 期初数量
     */
    @ExcelProperty(value = "*期初数量", index = 1)
    @FieldValid(fieldName = "期初数量", isNotBlank = true ,formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER,maxLength = 16)
    private String  qty;

    /**
     * 仓位
     */
    @ExcelProperty(value = "仓位", index = 2)
    @FieldValid(fieldName = "仓位",maxLength = 32)
    private String  warehouseLocation;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注", index = 3)
    @FieldValid(fieldName = "备注",maxLength = 255)
    private String  remark;

    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index = 4)
    private String  errorMsg;

}