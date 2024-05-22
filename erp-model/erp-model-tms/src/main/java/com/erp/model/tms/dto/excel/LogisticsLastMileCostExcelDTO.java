package com.erp.model.tms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * @description: 尾程物料费用导入
 * @author Will
 * @date: 2024/5/11 10:02
 */
@Data
public class LogisticsLastMileCostExcelDTO implements Serializable {

    /**
     * 平台订单号
     */
    @ExcelProperty(value = "*平台订单号", index = 0)
    @FieldValid(fieldName = "平台订单号", isNotBlank = true, maxLength = 100)
    private String platformCode;

    /**
     * 物流跟踪单号
     */
    @ExcelProperty(value = "*物流跟踪单号", index = 1)
    @FieldValid(fieldName = "物流跟踪单号", isNotBlank = true, maxLength = 100)
    private String trackNo;

    /**
     * 计费重[物流商]
     */
    @ExcelProperty(value = "*计费重[物流商]", index = 2)
    @FieldValid(fieldName = "计费重[物流商]", isNotBlank = true ,formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String billingWeightStr;

    /**
     * 币种
     */
    @ExcelProperty(value = "币种[默认￥]", index = 3)
    @FieldValid(fieldName = "币种", maxLength = 32)
    private String currency;

    /**
     * 错误数据
     */
    private String errorMsg;

}
