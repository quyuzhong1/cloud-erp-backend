package com.erp.model.plm.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;

import java.io.Serializable;

@Data
public class ProductDetailImageExcelDTO implements Serializable {

    /**
     * skuNo
     */
    @ExcelProperty(value = "SKU", index = 0)
    private String skuNo;

    /**
     * 错误信息
     */
    @ExcelProperty(value = "错误信息", index = 1)
    private String errorMsg;

}
