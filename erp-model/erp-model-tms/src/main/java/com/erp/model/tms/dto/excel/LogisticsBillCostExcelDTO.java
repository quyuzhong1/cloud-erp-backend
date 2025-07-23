package com.erp.model.tms.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
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
    @ExcelProperty(value = "*销售出库单" , index = 0)
    @FieldValid(fieldName = "销售出库单",maxLength = 32)
    private String  outstockCode;

    /**
     * 物流跟踪单号
     */
    @ExcelProperty(value = "*物流跟踪单号", index = 1)
    @FieldValid(fieldName = "物流跟踪单号",isNotBlank = true,maxLength = 50)
    private String  trackNo;

    /**
     * 计费重[预估]
     */
    @ExcelProperty(value = "计费重[预估]", index = 2)
    @FieldValid(fieldName = "计费重[预估]",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String  billingWeight;

    /**
     * 计费重[物流商]
     */
    @ExcelProperty(value = "计费重[物流商]", index = 3)
    @FieldValid(fieldName = "计费重[物流商]",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String  billingWeightLogistics;

    /**
     * 类型
     */
    @ExcelProperty(value = "*对账类型", index = 4)
    @FieldValid(fieldName = "对账类型",isNotBlank = true,fieldValues = "付款,退款")
    private String  payTypeName;
    @ExcelIgnore
    private String  payType;

    /**
     * 费用名称
     */
    @ExcelProperty(value = "*费用名称", index = 5)
    @FieldValid(fieldName = "费用名称",isNotBlank = true,maxLength = 200)
    private String  costName;

    /**
     * 实际运费
     */
    @ExcelProperty(value = "*实际金额", index = 6)
    @FieldValid(fieldName = "实际金额",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String  costValue;

    /**
     * 币种
     */
    @ExcelProperty(value = "*实际币种", index = 7)
    @FieldValid(fieldName = "实际币种",isNotBlank = true,enumClass = CurrencyEnum.class)
    private String  currency;

    /**
     * 预估运费
     */
    @ExcelProperty(value = "预估金额", index = 8)
    @FieldValid(fieldName = "预估金额",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String  estimatedCostValue;
    
    /**
     * 预估币种
     */
    @ExcelProperty(value = "预估币种", index = 9)
    @FieldValid(fieldName = "预估币种",enumClass = CurrencyEnum.class)
    private String  estimatedCurrency;

    /**
     * 错误信息
     */
    private String errorMsg;
}
