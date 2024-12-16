package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * @description: 期初头程分摊导入明细
 * @author zdy
 * @date: 2024/3/21 14:52
 */
@Data
public class FbaTransitExcelDTO implements Serializable {

    /**
     * 导入月份
     */
    @ExcelProperty(value = "导入月份", index = 0)
    @FieldValid(fieldName = "*导入月份",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.YEARMONTH)
    private LocalDate reportMonth;
    /**
     * 货件单号
     */
    @ExcelProperty(value = "货件单号", index = 1)
    @FieldValid(fieldName = "*货件单号）",isNotBlank = true,maxLength = 200)
    private String  shipmentCode;
    /**
     * ASIN
     */
    @ExcelProperty(value = "ASIN", index = 2)
    @FieldValid(fieldName = "*ASIN）",isNotBlank = true,maxLength = 200)
    private String  asin;
    /**
     * MSKU
     */
    @ExcelProperty(value = "MSKU", index = 3)
    @FieldValid(fieldName = "*MSKU）",isNotBlank = true,maxLength = 200)
    private String  msku;
    /**
     * 期初在途
     */
    @ExcelProperty(value = "期初在途", index = 4)
    @FieldValid(fieldName = "*期初在途）",isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.INTEGER)
    private Integer  initTransitQty;
    /**
     * 错误信息
     */
    @ExcelProperty(value = "错误数据", index = 5)
    private String errorMsg;
}
