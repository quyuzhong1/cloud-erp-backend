package com.erp.model.tms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;

import java.io.Serializable;


/**
 * 头程对账标准导入
 *
 * @author Jim
 * {@code @date:} 2024/03/22
 */
@Data
public class FirstMileReconciliationStandardExcelDTO implements Serializable {

    /**
     * 物流运单号
     */
    @ExcelProperty(value = "*物流运单号", index = 0)
    @FieldValid(fieldName = "物流运单号", isNotBlank = true, maxLength = 32)
    private String transportNo;

    /**
     * 实际实重
     */
    @ExcelProperty(value = "实际实重", index = 1)
    @FieldValid(fieldName = "实际实重", formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String actualWeight;

    /**
     * 实际计费重
     */
    @ExcelProperty(value = "实际计费重", index = 2)
    @FieldValid(fieldName = "实际计费重", formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String actualBillingWeight;

    /**
     * 费用项
     */
    @ExcelProperty(value = "费用项", index = 3)
    @FieldValid(fieldName = "费用项", maxLength = 200)
    private String costName;

    /**
     * 费用金额
     */
    @ExcelProperty(value = "费用金额", index = 4)
    @FieldValid(fieldName = "费用金额", formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String costValue;

    /**
     * 错误信息
     */
    private String errorMsg;
}
