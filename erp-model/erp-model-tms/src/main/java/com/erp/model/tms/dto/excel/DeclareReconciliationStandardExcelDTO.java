package com.erp.model.tms.dto.excel;

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
public class DeclareReconciliationStandardExcelDTO implements Serializable {

    /**
     * 销售订单号
     */
    @ExcelProperty(value = "*销售订单号")
    @FieldValid(fieldName = "销售订单号",isNotBlank = true,maxLength = 32)
    private String  soCode;

    /**
     * 实际实重
     */
    @ExcelProperty(value = "实际实重")
    @FieldValid(fieldName = "实际实重",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String  actualWeight;

    /**
     * 实际计费重
     */
    @ExcelProperty(value = "实际计费重")
    @FieldValid(fieldName = "实际计费重",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String  actualBillingWeight;

    /**
     * 费用项
     */
    @ExcelProperty(value = "费用项")
    @FieldValid(fieldName = "费用项",maxLength = 200)
    private String  costName;

    /**
     * 费用金额
     */
    @ExcelProperty(value = "费用金额")
    @FieldValid(fieldName = "费用金额",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String  costValue;

    /**
     * 重量单位
     */
    @ExcelProperty(value = "重量单位")
    @FieldValid(fieldName = "重量单位")
    private String  actualWeightUnit;

    /**
     * 错误信息
     */
    private String errorMsg;
}
