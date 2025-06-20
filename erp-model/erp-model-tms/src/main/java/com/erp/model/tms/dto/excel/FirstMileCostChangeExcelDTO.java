package com.erp.model.tms.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import com.erp.model.tms.enums.AllocationFeeTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FirstMileCostChangeExcelDTO implements Serializable {
    /**
     * *月份(yyyy-MM)
     */
    @ExcelProperty(value = "*月份(yyyy-MM)", index = 0)
    @FieldValid(fieldName = "月份",isNotBlank = true,maxLength = 32)
    private String reportMonthStr;
    @ExcelIgnore
    private LocalDate reportMonth;
    /**
     * 来源单
     */
    @ExcelProperty(value = "来源单号", index = 1)
    @FieldValid(fieldName = "来源单号",maxLength = 32)
    private String sourceCode;

    /**
     * 业务单号
     */
    @ExcelProperty(value = "业务单号", index = 2)
    @FieldValid(fieldName = "业务单号",maxLength = 32)
    private String businessCode;

    /**
     * 物流运单号
     */
    @ExcelProperty(value = "物流运单号", index = 3)
    @FieldValid(fieldName = "物流运单号",maxLength = 50)
    private String transportNo;
    /**
     * 平台SKU
     */
    @ExcelProperty(value = "*平台SKU", index = 4)
    @FieldValid(fieldName = "平台SKU）",isNotBlank = true,maxLength = 200)
    private String  platformSkuNo;
    /**
     * SKU 编号
     */
    @ExcelProperty(value = "*SKU", index = 5)
    @FieldValid(fieldName = "SKU",isNotBlank = true, maxLength =64 )
    private String skuNo;
    /**
     * *分摊重量(KG)
     */
    @ExcelProperty(value = "分摊重量(KG)", index = 6)
    @FieldValid(fieldName = "分摊重量(KG)",formatPattern = FieldFormatPatternTypeEnum.AMOUNT4)
    private String allocatedWeightStr;
    @ExcelIgnore
    private BigDecimal allocatedWeight;
    /**
     * *费用分类
     */
    @ExcelProperty(value = "*费用分类", index = 7)
    @FieldValid(fieldName = "费用分类",enumClass = AllocationFeeTypeEnum.class)
    private String feeTypeName;
    @ExcelIgnore
    private String feeType;
    /**
     * 冲期初在途费用
     */
    @ExcelProperty(value = "冲期初在途费用(¥)", index = 8)
    @FieldValid(fieldName = "冲期初在途费用")
    private String midPeriodTransitCostStr;
    @ExcelIgnore
    private BigDecimal midPeriodTransitCost;

    /**
     * 本期分摊费用
     */
    @ExcelProperty(value = "本期分摊费用(¥)", index = 9)
    @FieldValid(fieldName = "本期分摊费用")
    private String currentPeriodAllocatedCostStr;
    @ExcelIgnore
    private BigDecimal currentPeriodAllocatedCost;
    /**
     * 期末在途费用(¥)
     */
    @ExcelProperty(value = "期末在途费用(¥)", index = 10)
    @FieldValid(fieldName = "期末在途费用")
    private String endPeriodTransitCostStr;
    @ExcelIgnore
    private BigDecimal endPeriodTransitCost;
    /**
     * 期末暂估费用(¥)
     */
    @ExcelProperty(value = "期末暂估费用(¥)", index = 11)
    @FieldValid(fieldName = "期末暂估费用")
    private String endPeriodEstimatedCostStr;
    @ExcelIgnore
    private BigDecimal endPeriodEstimatedCost;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注", index = 12)
    @FieldValid(fieldName = "备注")
    private String remark;

    @ExcelIgnore
    private String mainId;
    /**
     * 错误信息
     */
    @ExcelProperty(value = "错误信息")
    private String errorMsg;
}
