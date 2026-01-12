package com.erp.model.tms.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
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
     * 平台订单号
     */
    @ExcelProperty(value = "平台订单号" , index = 0)
    @FieldValid(fieldName = "平台订单号",maxLength = 32)
    private String  platformCode;
    /**
     * 发货单号
     */
    @ExcelProperty(value = "发货单号" , index = 1)
    @FieldValid(fieldName = "发货单号",maxLength = 32)
    private String  soDeliveryCode;
    /**
     * 销售订单号
     */
    @ExcelProperty(value = "销售订单号" , index = 2)
    @FieldValid(fieldName = "销售订单号",maxLength = 32)
    private String  soCode;

    /**
     * 物流跟踪单号
     */
    @ExcelProperty(value = "物流跟踪单号", index = 3)
    @FieldValid(fieldName = "物流跟踪单号",maxLength = 50)
    private String  trackNo;

    /**
     * 计费重[预估]
     */
    @ExcelProperty(value = "计费重[预估]", index = 4)
    @FieldValid(fieldName = "计费重[预估]",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String  billingWeight;

    /**
     * 计费重[物流商]
     */
    @ExcelProperty(value = "计费重[物流商]", index = 5)
    @FieldValid(fieldName = "计费重[物流商]",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String  billingWeightLogistics;

    /**
     * 包装尺寸(物流商)
     */
    @ExcelProperty(value = "包装尺寸长(物流商)", index = 6)
    @FieldValid(fieldName = "包装尺寸长(物流商)",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String  thirdLength;

    /**
     * 计费重[物流商]
     */
    @ExcelProperty(value = "包装尺寸宽(物流商)", index = 7)
    @FieldValid(fieldName = "包装尺寸宽(物流商)",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String  thirdWidth;

    /**
     * 包装尺寸高(物流商)
     */
    @ExcelProperty(value = "包装尺寸高(物流商)", index = 8)
    @FieldValid(fieldName = "包装尺寸高(物流商)",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String  thirdHeight;

    /**
     * 实重(物流商)
     */
    @ExcelProperty(value = "实重(物流商)", index = 9)
    @FieldValid(fieldName = "实重(物流商)",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String  thirdActualWeight;

    /**
     * 类型
     */
    @ExcelProperty(value = "*对账类型", index = 10)
    @FieldValid(fieldName = "对账类型",isNotBlank = true,fieldValues = "付款,退款")
    private String  payTypeName;
    @ExcelIgnore
    private String  payType;

    /**
     * 费用名称
     */
    @ExcelProperty(value = "*费用名称", index = 11)
    @FieldValid(fieldName = "费用名称",isNotBlank = true,maxLength = 200)
    private String  costName;

    /**
     * 实际运费
     */
    @ExcelProperty(value = "*实际金额", index = 12)
    @FieldValid(fieldName = "实际金额",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String  costValue;

    /**
     * 币种
     */
    @ExcelProperty(value = "*实际币种", index = 13)
    @FieldValid(fieldName = "实际币种",isNotBlank = true)
    private String  currency;

    /**
     * 预估运费
     */
    @ExcelProperty(value = "预估金额", index = 14)
    @FieldValid(fieldName = "预估金额",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String  estimatedCostValue;
    
    /**
     * 预估币种
     */
    @ExcelProperty(value = "预估币种", index = 15)
    @FieldValid(fieldName = "预估币种")
    private String  estimatedCurrency;

    /**
     * 错误信息
     */
    private String errorMsg;
}
