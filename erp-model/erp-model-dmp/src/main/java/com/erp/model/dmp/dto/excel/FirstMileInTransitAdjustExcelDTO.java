package com.erp.model.dmp.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import com.erp.model.dmp.entity.doris.AdsErpFirstMileInTransitDiffEntity;
import lombok.Data;

import java.io.Serializable;

/**
 * 期初头程分摊导入明细
 */
@Data
public class FirstMileInTransitAdjustExcelDTO implements Serializable {

    /**
     * 导入月份
     */
    @ExcelProperty(value = "导入月份", index = 0)
    @FieldValid(fieldName = "*导入月份",isNotBlank = true)
    private String reportMonth;
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
    @FieldValid(fieldName = "*ASIN）",maxLength = 200)
    private String asin;
    /**
     * MSKU
     */
    @ExcelProperty(value = "MSKU/第三方SKU", index = 3)
    @FieldValid(fieldName = "*MSKU/第三方SKU）",isNotBlank = true,maxLength = 200)
    private String platformSkuNo;
    /**
     * 调整数量
     */
    @ExcelProperty(value = "调整数量", index = 4)
    @FieldValid(fieldName = "*调整数量）",isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.INTEGER)
    private String adjustQty;

    /**
     * 备注
     */
    @ExcelProperty(value = "调整原因", index = 5)
    @FieldValid(fieldName = "*调整原因）",isNotBlank = true,maxLength = 255)
    private String remark;

    /**
     * 错误信息
     */
    private String errorMsg;

    @ExcelIgnore()
    private AdsErpFirstMileInTransitDiffEntity entity;
}
