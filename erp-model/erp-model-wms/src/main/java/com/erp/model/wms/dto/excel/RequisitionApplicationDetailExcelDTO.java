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
public class RequisitionApplicationDetailExcelDTO implements Serializable {

    /**
     * 箱号
     */
    @ExcelProperty(value = "*箱号", index = 0)
    @FieldValid(fieldName = "箱号", isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER,maxLength = 16)
    private String  boxNo;
    /**
     * 装箱SKU
     */
    @ExcelProperty(value = "装箱SKU", index = 1)
    @FieldValid(fieldName = "装箱SKU")
    private String  packingSku;

    /**
     * 期初数量
     */
    @ExcelProperty(value = "装箱FNSKU", index = 2)
    @FieldValid(fieldName = "装箱FNSKU")
    private String  packingFnSku;

    /**
     * 数量
     */
    @ExcelProperty(value = "数量", index = 3)
    @FieldValid(fieldName = "数量", formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER,maxLength = 16)
    private String  qty;

    /**
     * 货件号
     */
    @ExcelProperty(value = "*货件号", index = 4)
    @FieldValid(fieldName = "货件号",isNotBlank = true)
    private String  fbaShipmentCode;
    /**
     * 货件箱号
     */
    @ExcelProperty(value = "*货件箱号", index = 5)
    @FieldValid(fieldName = "货件箱号",isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER,maxLength = 16)
    private String  fbaBoxNo;

    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index = 6)
    private String  errorMsg;

}