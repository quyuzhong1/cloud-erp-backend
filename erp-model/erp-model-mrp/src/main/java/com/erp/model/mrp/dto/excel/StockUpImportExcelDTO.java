package com.erp.model.mrp.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 备货
 * @author will
 * @date 2024/9/2 19:30
 */
@Data
public class StockUpImportExcelDTO implements Serializable {


    /**
     * 平台
     */
    @ExcelProperty(value = "*平台", index = 0)
    @FieldValid(fieldName = "平台", isNotBlank = true, maxLength = 32)
    private String platform;

    /**
     * SKU
     */
    @ExcelProperty(value = "*SKU", index = 1)
    @FieldValid(fieldName = "SKU", isNotBlank = true, maxLength = 32)
    private String skuNo;


    /**
     * 店铺
     */
    @ExcelProperty(value = "*店铺", index = 2)
    @FieldValid(fieldName = "店铺", isNotBlank = true, maxLength = 32)
    private String shopName;

    /**
     * 采购审批（天）
     */
    @ExcelProperty(value = "采购审批（天）", index = 3)
    @FieldValid(fieldName = "采购审批（天）", formatPattern= FieldFormatPatternTypeEnum.YEAR_DAYS)
    private String purchaseApproveDays;

    /**
     * 生产周期（天）
     */
    @ExcelProperty(value = "生产周期（天）", index = 4)
    @FieldValid(fieldName = "生产周期（天）", formatPattern= FieldFormatPatternTypeEnum.YEAR_DAYS)
    private String productionDays;

    /**
     * 供应商发货（天）
     */
    @ExcelProperty(value = "供应商发货（天）", index = 5)
    @FieldValid(fieldName = "供应商发货（天）", formatPattern= FieldFormatPatternTypeEnum.YEAR_DAYS)
    private String supplierDeliveryDays;

    /**
     * 质检入库（天）
     */
    @ExcelProperty(value = "质检入库（天）", index = 6)
    @FieldValid(fieldName = "质检入库（天）",formatPattern= FieldFormatPatternTypeEnum.YEAR_DAYS)
    private String qcDays;

    /**
     * 采购频率（天）
     */
    @ExcelProperty(value = "采购频率（天）", index = 7)
    @FieldValid(fieldName = "采购频率（天）", formatPattern= FieldFormatPatternTypeEnum.YEAR_DAYS)
    private String purchaseCycleDays;

    /**
     * 物流时效（空运）
     */
    @ExcelProperty(value = "物流时效（空运）", index = 8)
    @FieldValid(fieldName = "物流时效（空运）",formatPattern= FieldFormatPatternTypeEnum.YEAR_DAYS)
    private String oneLogisticsDays;

    /**
     * 发货频率（空运）
     */
    @ExcelProperty(value = "发货频率（空运）", index = 9)
    @FieldValid(fieldName = "发货频率（空运）",formatPattern= FieldFormatPatternTypeEnum.YEAR_DAYS)
    private String oneLogisticsCycleDays;

    /**
     * 优先级（空运)
     */
    @ExcelProperty(value = "优先级（空运)", index = 10)
    @FieldValid(fieldName = "优先级（空运)",formatPattern= FieldFormatPatternTypeEnum.INTEGER)
    private String oneIndex;

    /**
     * 物流时效（快递）
     */
    @ExcelProperty(value = "物流时效（快递）", index = 11)
    @FieldValid(fieldName = "物流时效（快递）",formatPattern= FieldFormatPatternTypeEnum.YEAR_DAYS)
    private String twoLogisticsDays;

    /**
     * 发货频率（快递）
     */
    @ExcelProperty(value = "发货频率（快递）", index = 12)
    @FieldValid(fieldName = "发货频率（快递）",formatPattern= FieldFormatPatternTypeEnum.YEAR_DAYS)
    private String twoLogisticsCycleDays;

    /**
     * 优先级（快递)
     */
    @ExcelProperty(value = "优先级（快递)", index = 13)
    @FieldValid(fieldName = "优先级（快递)",formatPattern= FieldFormatPatternTypeEnum.INTEGER)
    private String twoIndex;

    /**
     * 物流时效（海运散装）
     */
    @ExcelProperty(value = "物流时效（海运散装）", index = 14)
    @FieldValid(fieldName = "物流时效（海运散装）",formatPattern= FieldFormatPatternTypeEnum.YEAR_DAYS)
    private String threeLogisticsDays;

    /**
     * 发货频率（海运散装）
     */
    @ExcelProperty(value = "发货频率（海运散装）", index = 15)
    @FieldValid(fieldName = "发货频率（海运散装）",formatPattern= FieldFormatPatternTypeEnum.YEAR_DAYS)
    private String threeLogisticsCycleDays;

    /**
     * 优先级（海运散装)
     */
    @ExcelProperty(value = "优先级（海运散装)", index = 16)
    @FieldValid(fieldName = "优先级（海运散装)",formatPattern= FieldFormatPatternTypeEnum.INTEGER)
    private String threeIndex;

    /**
     * 物流时效（海运整柜）
     */
    @ExcelProperty(value = "物流时效（海运整柜）", index = 17)
    @FieldValid(fieldName = "物流时效（海运整柜）",formatPattern= FieldFormatPatternTypeEnum.YEAR_DAYS)
    private String fourLogisticsDays;

    /**
     * 发货频率（海运整柜）
     */
    @ExcelProperty(value = "发货频率（海运整柜）", index = 18)
    @FieldValid(fieldName = "发货频率（海运整柜）",formatPattern= FieldFormatPatternTypeEnum.YEAR_DAYS)
    private String fourLogisticsCycleDays;

    /**
     * 优先级（海运整柜)
     */
    @ExcelProperty(value = "优先级（海运整柜)", index = 19)
    @FieldValid(fieldName = "优先级（海运整柜)",formatPattern= FieldFormatPatternTypeEnum.INTEGER)
    private String fourIndex;

    /**
     * 物流时效（铁运散装）
     */
    @ExcelProperty(value = "物流时效（铁运散装）", index = 20)
    @FieldValid(fieldName = "物流时效（铁运散装）",formatPattern= FieldFormatPatternTypeEnum.YEAR_DAYS)
    private String fiveLogisticsDays;

    /**
     * 发货频率（铁运散装）
     */
    @ExcelProperty(value = "发货频率（铁运散装）", index = 21)
    @FieldValid(fieldName = "发货频率（铁运散装）",formatPattern= FieldFormatPatternTypeEnum.YEAR_DAYS)
    private String fiveLogisticsCycleDays;

    /**
     * 优先级（铁运散装)
     */
    @ExcelProperty(value = "优先级（铁运散装)", index = 22)
    @FieldValid(fieldName = "优先级（铁运散装)",formatPattern= FieldFormatPatternTypeEnum.INTEGER)
    private String fiveIndex;

    /**
     * 物流时效（铁运整柜）
     */
    @ExcelProperty(value = "物流时效（铁运整柜）", index = 23)
    @FieldValid(fieldName = "物流时效（铁运整柜）",formatPattern= FieldFormatPatternTypeEnum.YEAR_DAYS)
    private String sixLogisticsDays;

    /**
     * 发货频率（铁运整柜）
     */
    @ExcelProperty(value = "发货频率（铁运整柜）", index = 24)
    @FieldValid(fieldName = "发货频率（铁运整柜）",formatPattern= FieldFormatPatternTypeEnum.YEAR_DAYS)
    private String sixLogisticsCycleDays;

    /**
     * 优先级（铁运整柜)
     */
    @ExcelProperty(value = "优先级（铁运整柜)", index = 25)
    @FieldValid(fieldName = "优先级（铁运整柜)",formatPattern= FieldFormatPatternTypeEnum.INTEGER)
    private String sixIndex;

    /**
     * 安全天数
     */
    @ExcelProperty(value = "安全天数", index = 26)
    @FieldValid(fieldName = "安全天数", formatPattern= FieldFormatPatternTypeEnum.YEAR_DAYS)
    private String safeDays;

    /**
     * 默认备货系数
     */
    @ExcelProperty(value = "默认备货系数", index = 27)
    @FieldValid(fieldName = "默认备货系数", formatPattern= FieldFormatPatternTypeEnum.NUMBER_99)
    private String stockingRatio;

    /**
     * 错误数据
     */
    private String errorMsg;
}
