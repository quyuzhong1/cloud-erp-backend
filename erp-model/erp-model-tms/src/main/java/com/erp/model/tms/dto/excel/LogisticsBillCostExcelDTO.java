package com.erp.model.tms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.CurrencyEnum;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: 自发货费用
 * @date 2023/11/14 15:47
 */
@Data
public class LogisticsBillCostExcelDTO  implements Serializable {

    /**
     * 销售出库单
     */
    @ExcelProperty(value = "销售出库单")
    @FieldValid(fieldName = "销售出库单",maxLength = 32)
    private String  outstockCode;

    /**
     * 运单号
     */
    @ExcelProperty(value = "*运单号")
    @FieldValid(fieldName = "运单号",isNotBlank = true,maxLength = 50)
    private String  transportNo;

    /**
     * 计费重[物流商]
     */
    @ExcelProperty(value = "*计费重[物流商]")
    @FieldValid(fieldName = "计费重[物流商]",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String  billingWeightLogistics;

    /**
     * 实际运费[物流商]
     */
    @ExcelProperty(value = "*实际运费[物流商]")
    @FieldValid(fieldName = "实际运费[物流商]",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String  actualShippingCost;

    /**
     * 币种
     */
    @ExcelProperty(value = "币种")
    @FieldValid(fieldName = "币种",enumClass = CurrencyEnum.class)
    private String  currency;

    /**
     * 错误信息
     */
    private String errorMsg;
}
