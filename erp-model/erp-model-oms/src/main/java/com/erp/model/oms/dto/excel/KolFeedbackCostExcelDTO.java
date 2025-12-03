package com.erp.model.oms.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * KOL回片费用Excel导入DTO
 * @author wuhaotian
 * @since 2025-12-03
 */
@Data
@NoArgsConstructor
public class KolFeedbackCostExcelDTO implements Serializable {

    /**
     * 回片链接
     */
    @ColumnWidth(50)
    @ExcelProperty(value = "*回片链接", index = 0)
    @FieldValid(fieldName = "*回片链接", isNotBlank = true, maxLength = 500)
    private String url;
    @ExcelIgnore
    private String urlHash;

    /**
     * 付费币别
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*付费币别", index = 1)
    @FieldValid(fieldName = "*付费币别", isNotBlank = true, maxLength = 30)
    private String currencyName;
    @ExcelIgnore
    private String currency;

    /**
     * 费用名称
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*费用名称", index = 2)
    @FieldValid(fieldName = "*费用名称", isNotBlank = true, maxLength = 100)
    private String costTypeName;
    @ExcelIgnore
    private String costTypeId;

    /**
     * 金额（原币）
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "金额(原币)", index = 3)
    @FieldValid(fieldName = "金额(原币)", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.DECIMAL)
    private String originalAmountStr;
    @ExcelIgnore
    private BigDecimal originalAmount;

    /**
     * 备注
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "备注", index = 4)
    @FieldValid(fieldName = "备注", maxLength = 255)
    private String remark;

    /**
     * 汇率 - 通过接口查询，不需要导入
     */
    @ExcelIgnore
    private BigDecimal exchangeRate;

    /**
     * 金额（本位币）- 自动计算，不需要输入
     */
    @ExcelIgnore
    private BigDecimal baseAmount;

    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误信息", index = 6)
    @ColumnWidth(50)
    private String errorMsg = "";

    /**
     * 行号
     */
    @ExcelIgnore
    private Integer rowNum;

    /**
     * 创建人ID
     */
    @ExcelIgnore
    private String createUserId;

    /**
     * 创建人姓名
     */
    @ExcelIgnore
    private String createUserName;
}

