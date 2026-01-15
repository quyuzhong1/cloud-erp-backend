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
    @ExcelProperty(value = "平台订单号", index = 0)
    @FieldValid(fieldName = "平台订单号", maxLength = 100)
    private String platformCode;


    /**
     * 销售订单号
     */
    @ExcelProperty(value = "销售订单号", index = 1)
    @FieldValid(fieldName = "销售订单号", maxLength = 100)
    private String soCode;


    /**
     * 发货单号
     */
    @ExcelProperty(value = "发货单号", index = 2)
    @FieldValid(fieldName = "发货单号", maxLength = 100)
    private String soDeliveryCode;

    /**
     * 物流跟踪单号
     */
    @ExcelProperty(value = "物流跟踪单号", index = 3)
    @FieldValid(fieldName = "物流跟踪单号", maxLength = 100)
    private String trackNo;

    /**
     * 计费重[物流商]
     */
    @ExcelProperty(value = "*计费重[物流商]", index = 4)
    @FieldValid(fieldName = "计费重[物流商]", isNotBlank = true ,formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String billingWeightStr;
    
    /**
     * 类型
     */
    @ExcelProperty(value = "*对账类型", index = 5)
    @FieldValid(fieldName = "对账类型",isNotBlank = true,fieldValues = "付款,退款")
    private String  payType;

    /**
     * 币种
     */
    @ExcelProperty(value = "*币种", index = 6)
    @FieldValid(fieldName = "币种",isNotBlank = true, maxLength = 32)
    private String currency;

    /**
     * 包装尺寸(物流商)
     */
    @ExcelProperty(value = "包装尺寸长(物流商)", index = 7)
    @FieldValid(fieldName = "包装尺寸长(物流商)",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String  thirdLength;

    /**
     * 计费重[物流商]
     */
    @ExcelProperty(value = "包装尺寸宽(物流商)", index = 8)
    @FieldValid(fieldName = "包装尺寸宽(物流商)",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String  thirdWidth;

    /**
     * 包装尺寸高(物流商)
     */
    @ExcelProperty(value = "包装尺寸高(物流商)", index = 9)
    @FieldValid(fieldName = "包装尺寸高(物流商)",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String  thirdHeight;

    /**
     * 实重(物流商)
     */
    @ExcelProperty(value = "实重(物流商)", index = 10)
    @FieldValid(fieldName = "实重(物流商)",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String  thirdActualWeight;

    /**
     * 账单确认时间
     */
    @ExcelProperty(value = "账单确认时间", index = 11)
    @FieldValid(fieldName = "账单确认时间",formatPattern = FieldFormatPatternTypeEnum.DATETIME)
    private String  confirmTimeStr;

    /**
     * 错误数据
     */
    private String errorMsg;

}
