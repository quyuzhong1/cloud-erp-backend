package com.erp.model.mrp.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
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
    @ExcelProperty(value = "平台", index = 0)
    @FieldValid(fieldName = "平台", isNotBlank = true, maxLength = 50)
    private String platform;

    /**
     * SKU
     */
    @ExcelProperty(value = "SKU", index = 0)
    @FieldValid(fieldName = "SKU", isNotBlank = true, maxLength = 50)
    private String skuNo;


    /**
     * 店铺
     */
    @ExcelProperty(value = "店铺", index = 0)
    @FieldValid(fieldName = "店铺", isNotBlank = true, maxLength = 50)
    private String shopName;

    /**
     * 采购审批（天）
     */
    @ExcelProperty(value = "*采购审批（天）", index = 0)
    @FieldValid(fieldName = "采购审批（天）", isNotBlank = true, maxLength = 50)
    private String purchaseApproveDays;

    /**
     * 生产周期（天）
     */
    @ExcelProperty(value = "*生产周期（天）", index = 0)
    @FieldValid(fieldName = "生产周期（天）", isNotBlank = true, maxLength = 50)
    private String productionDays;

    /**
     * 供应商发货（天）
     */
    @ExcelProperty(value = "*供应商发货（天）", index = 0)
    @FieldValid(fieldName = "供应商发货（天）", isNotBlank = true, maxLength = 50)
    private String supplierDeliveryDays;

    /**
     * 质检入库（天）
     */
    @ExcelProperty(value = "*质检入库（天）", index = 0)
    @FieldValid(fieldName = "质检入库（天）", isNotBlank = true, maxLength = 50)
    private String qcDays;

    /**
     * 采购频率（天）
     */
    @ExcelProperty(value = "*采购频率（天）", index = 0)
    @FieldValid(fieldName = "采购频率（天）", isNotBlank = true, maxLength = 50)
    private String purchaseCycleDays;

    /**
     * 物流时效（空运）
     */
    @ExcelProperty(value = "物流时效（空运）", index = 0)
    @FieldValid(fieldName = "物流时效（空运）", maxLength = 50)
    private String oneLogisticsDays;

    /**
     * 发货频率（空运）
     */
    @ExcelProperty(value = "发货频率（空运）", index = 0)
    @FieldValid(fieldName = "发货频率（空运）", maxLength = 50)
    private String oneLogisticsCycleDays;

    /**
     * 优先级（空运)
     */
    @ExcelProperty(value = "优先级（空运)", index = 0)
    @FieldValid(fieldName = "优先级（空运)", maxLength = 50)
    private String oneIndex;

    /**
     * 物流时效（快递）
     */
    @ExcelProperty(value = "物流时效（快递）", index = 0)
    @FieldValid(fieldName = "物流时效（快递）", maxLength = 50)
    private String twoLogisticsDays;

    /**
     * 发货频率（快递）
     */
    @ExcelProperty(value = "发货频率（快递）", index = 0)
    @FieldValid(fieldName = "发货频率（快递）", maxLength = 50)
    private String twoLogisticsCycleDays;

    /**
     * 优先级（快递)
     */
    @ExcelProperty(value = "优先级（快递)", index = 0)
    @FieldValid(fieldName = "优先级（快递)", maxLength = 50)
    private String twoIndex;

    /**
     * 物流时效（海运散装）
     */
    @ExcelProperty(value = "物流时效（海运散装）", index = 0)
    @FieldValid(fieldName = "物流时效（海运散装）", maxLength = 50)
    private String threeLogisticsDays;

    /**
     * 发货频率（海运散装）
     */
    @ExcelProperty(value = "发货频率（海运散装）", index = 0)
    @FieldValid(fieldName = "发货频率（海运散装）", maxLength = 50)
    private String threeLogisticsCycleDays;

    /**
     * 优先级（海运散装)
     */
    @ExcelProperty(value = "优先级（海运散装)", index = 0)
    @FieldValid(fieldName = "优先级（海运散装)", maxLength = 50)
    private String threeIndex;

    /**
     * 物流时效（海运整柜）
     */
    @ExcelProperty(value = "物流时效（海运整柜）", index = 0)
    @FieldValid(fieldName = "物流时效（海运整柜）", maxLength = 50)
    private String fourLogisticsDays;

    /**
     * 发货频率（海运整柜）
     */
    @ExcelProperty(value = "发货频率（海运整柜）", index = 0)
    @FieldValid(fieldName = "发货频率（海运整柜）", maxLength = 50)
    private String fourLogisticsCycleDays;

    /**
     * 优先级（海运整柜)
     */
    @ExcelProperty(value = "优先级（海运整柜)", index = 0)
    @FieldValid(fieldName = "优先级（海运整柜)", maxLength = 50)
    private String fourIndex;

    /**
     * 物流时效（铁运散装）
     */
    @ExcelProperty(value = "物流时效（铁运散装）", index = 0)
    @FieldValid(fieldName = "物流时效（铁运散装）", maxLength = 50)
    private String fiveLogisticsDays;

    /**
     * 发货频率（铁运散装）
     */
    @ExcelProperty(value = "发货频率（铁运散装）", index = 0)
    @FieldValid(fieldName = "发货频率（铁运散装）", maxLength = 50)
    private String fiveLogisticsCycleDays;

    /**
     * 优先级（铁运散装)
     */
    @ExcelProperty(value = "优先级（铁运散装)", index = 0)
    @FieldValid(fieldName = "优先级（铁运散装)", maxLength = 50)
    private String fiveIndex;

    /**
     * 物流时效（铁运整柜）
     */
    @ExcelProperty(value = "物流时效（铁运整柜）", index = 0)
    @FieldValid(fieldName = "物流时效（铁运整柜）", maxLength = 50)
    private String sixLogisticsDays;

    /**
     * 发货频率（铁运整柜）
     */
    @ExcelProperty(value = "发货频率（铁运整柜）", index = 0)
    @FieldValid(fieldName = "发货频率（铁运整柜）", maxLength = 50)
    private String sixLogisticsCycleDays;

    /**
     * 优先级（铁运整柜)
     */
    @ExcelProperty(value = "优先级（铁运整柜)", index = 0)
    @FieldValid(fieldName = "优先级（铁运整柜)", maxLength = 50)
    private String sixIndex;

    /**
     * 安全天数
     */
    @ExcelProperty(value = "*安全天数", index = 0)
    @FieldValid(fieldName = "安全天数", isNotBlank = true, maxLength = 50)
    private String safeDays;

    /**
     * 默认备货系数
     */
    @ExcelProperty(value = "*默认备货系数", index = 0)
    @FieldValid(fieldName = "默认备货系数", isNotBlank = true, maxLength = 50)
    private String stockingRatio;

    /**
     * 错误数据
     */
    private String errorMsg;
}
