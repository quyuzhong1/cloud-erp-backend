package com.erp.model.tms.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 物流商对账单导入 Excel 中间对象
 * @author Will
 * @date: 2026/06/02
 */
@Data
public class LogisticsReconImportExcelDTO implements Serializable {

    /**
     * 序号
     */
    @ExcelProperty(value = "序号", index = 0)
    private String no;

    /**
     * 销售单号
     */
    @ExcelProperty(value = "销售单号", index = 1)
    @FieldValid(fieldName = "销售单号", maxLength = 64)
    private String soCode;

    /**
     * 平台订单号
     */
    @ExcelProperty(value = "平台订单号", index = 2)
    @FieldValid(fieldName = "平台订单号", maxLength = 64)
    private String platformOrderNo;

    /**
     * 物流跟踪号
     */
    @ExcelProperty(value = "物流跟踪号", index = 3)
    @FieldValid(fieldName = "物流跟踪号", maxLength = 64)
    private String trackNo;

    /**
     * 物流运单号
     */
    @ExcelProperty(value = "物流运单号", index = 4)
    @FieldValid(fieldName = "物流运单号", maxLength = 64)
    private String transportNo;

    /**
     * 发货单号
     */
    @ExcelProperty(value = "发货单号", index = 5)
    @FieldValid(fieldName = "发货单号", maxLength = 64)
    private String soDeliveryCode;

    /**
     * 币别
     */
    @ExcelProperty(value = "币别", index = 6)
    @FieldValid(fieldName = "币别", maxLength = 8)
    private String currency;

    /**
     * 对账类型
     */
    @ExcelProperty(value = "对账类型", index = 7)
    @FieldValid(fieldName = "对账类型", fieldValues = "pay,refund")
    private String payType;

    /**
     * 物流商实重
     */
    @ExcelProperty(value = "物流商实重", index = 8)
    @FieldValid(fieldName = "物流商实重", formatPattern = FieldFormatPatternTypeEnum.DECIMAL)
    private String weightLogistics;

    /**
     * 物流商体积重
     */
    @ExcelProperty(value = "物流商体积重", index = 9)
    @FieldValid(fieldName = "物流商体积重", formatPattern = FieldFormatPatternTypeEnum.DECIMAL)
    private String volumeWeightLogistics;

    /**
     * 物流商计费重
     */
    @ExcelProperty(value = "物流商计费重", index = 10)
    @FieldValid(fieldName = "物流商计费重", formatPattern = FieldFormatPatternTypeEnum.DECIMAL)
    private String billingWeightLogistics;

    /**
     * 物流商重量单位
     */
    @ExcelProperty(value = "物流商重量单位", index = 11)
    @FieldValid(fieldName = "物流商重量单位", maxLength = 8)
    private String weightUnit;

    /**
     * 物流商尺寸-长
     */
    @ExcelProperty(value = "物流商尺寸-长", index = 12)
    @FieldValid(fieldName = "物流商尺寸-长", formatPattern = FieldFormatPatternTypeEnum.DECIMAL)
    private String thirdLength;

    /**
     * 物流商尺寸-宽
     */
    @ExcelProperty(value = "物流商尺寸-宽", index = 13)
    @FieldValid(fieldName = "物流商尺寸-宽", formatPattern = FieldFormatPatternTypeEnum.DECIMAL)
    private String thirdWidth;

    /**
     * 物流商尺寸-高
     */
    @ExcelProperty(value = "物流商尺寸-高", index = 14)
    @FieldValid(fieldName = "物流商尺寸-高", formatPattern = FieldFormatPatternTypeEnum.DECIMAL)
    private String thirdHeight;

    /**
     * 费用名称
     */
    @ExcelProperty(value = "费用名称", index = 15)
    @FieldValid(fieldName = "费用名称", maxLength = 128)
    private String costName;

    /**
     * 实际金额
     */
    @ExcelProperty(value = "实际金额", index = 16)
    @FieldValid(fieldName = "实际金额", formatPattern = FieldFormatPatternTypeEnum.DECIMAL)
    private String actualAmount;

    /**
     * 错误信息
     */
    @ExcelProperty(value = "错误信息", index = 17)
    private String errorMsg;

    /**
     * 实际金额数值
     */
    @ExcelIgnore
    private BigDecimal actualAmountValue;
}
