package com.erp.model.tms.dto.excel;

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
public class ImportHistoryRecordExcelDTO implements Serializable {


    /**
     * 物流商
     */
    @FieldValid(fieldName = "物流商", maxLength = 100)
    private String logisticsSupplierId;

    /**
     * 平台订单号
     */
    @FieldValid(fieldName = "平台订单号", maxLength = 100)
    private String platformCode;


    /**
     * 销售订单号
     */
    @FieldValid(fieldName = "销售订单号", maxLength = 100)
    private String soCode;


    /**
     * 发货单号
     */
    @FieldValid(fieldName = "发货单号", maxLength = 100)
    private String soDeliveryCode;

    /**
     * 物流跟踪单号
     */
    @FieldValid(fieldName = "物流跟踪单号", maxLength = 100)
    private String trackNo;

    /**
     * 计费重[物流商]
     */
    @FieldValid(fieldName = "计费重[物流商]", formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String billingWeightStr;
    
    /**
     * 类型
     */
    @FieldValid(fieldName = "对账类型",fieldValues = "付款,退款")
    private String  payType;

    /**
     * 币种
     */
    @FieldValid(fieldName = "币种", maxLength = 32)
    private String currency;

    /**
     * 包装尺寸(物流商)
     */
    @FieldValid(fieldName = "包装尺寸长(物流商)",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String  thirdLength;

    /**
     * 计费重[物流商]
     */
    @FieldValid(fieldName = "包装尺寸宽(物流商)",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String  thirdWidth;

    /**
     * 包装尺寸高(物流商)
     */
    @FieldValid(fieldName = "包装尺寸高(物流商)",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String  thirdHeight;

    /**
     * 实重(物流商)
     */
    @FieldValid(fieldName = "实重(物流商)",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String  thirdActualWeight;

    /**
     * 账单确认时间
     */
    @FieldValid(fieldName = "账单确认时间",formatPattern = FieldFormatPatternTypeEnum.DATETIME)
    private String  confirmTimeStr;

    /**
     * 错误数据
     */
    private String errorMsg;

}
