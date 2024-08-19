package com.erp.model.tms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.CurrencyEnum;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @description: 期初头程分摊导入明细
 * @author zdy
 * @date: 2024/3/21 14:52
 */
@Data
public class InitFirstMileAllocationDetailExcelDTO implements Serializable {


    /**
     * 发货单号
     */
    @ExcelProperty(value = "*发货单号", index = 0)
    @FieldValid(fieldName = "发货单号",maxLength = 200)
    private String  sourceCode;

    /**
     * 业务单号
     */
    @ExcelProperty(value = "业务单号", index = 1)
    @FieldValid(fieldName = "业务单号）",maxLength = 200)
    private String  businessCode;
    /**
     * 店铺
     */
    @ExcelProperty(value = "店铺", index = 2)
    @FieldValid(fieldName = "店铺）",maxLength = 200)
    private String  shopName;
    /**
     * 仓库
     */
    @ExcelProperty(value = "仓库", index = 3)
    @FieldValid(fieldName = "仓库）",maxLength = 200)
    private String  warehouseName;
    /**
     * SKU
     */
    @ExcelProperty(value = "*SKU", index = 4)
    @FieldValid(fieldName = "SKU）",maxLength = 200)
    private String  skuNo;
    /**
     * 产品名称
     */
    @ExcelProperty(value = "产品名称", index = 5)
    @FieldValid(fieldName = "产品名称）",maxLength = 200)
    private String  productName;
    /**
     * 初始签收数量
     */
    @ExcelProperty(value = "*初始签收数量", index = 6)
    @FieldValid(fieldName = "初始签收数量",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.INTEGER)
    private Integer  initReceiveQty;
    /**
     * 期初在途头程费用
     */
    @ExcelProperty(value = "*期初在途头程费用", index = 7)
    @FieldValid(fieldName = "期初在途头程费用",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.AMOUNT2)
    private BigDecimal initTransitCost;
    /**
     * 期初在途头程关税
     */
    @ExcelProperty(value = "*期初在途头程关税", index = 8)
    @FieldValid(fieldName = "期初在途头程关税",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.AMOUNT2)
    private BigDecimal initTransitTariff;
    /**
     * *期初暂估头程费用
     */
    @ExcelProperty(value = "*期初暂估头程费用", index = 9)
    @FieldValid(fieldName = "期初暂估头程费用",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.AMOUNT2)
    private BigDecimal initEstimatedCost;
    /**
     * *期初暂估头程关税
     */
    @ExcelProperty(value = "*期初暂估头程关税", index = 10)
    @FieldValid(fieldName = "期初暂估头程关税",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.AMOUNT2)
    private BigDecimal initEstimatedTariff;
    /**
     * *分摊重量(KG)
     */
    @ExcelProperty(value = "*分摊重量(KG)", index = 11)
    @FieldValid(fieldName = "分摊重量(KG)",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.AMOUNT2)
    private BigDecimal weightAllocation;
    /**
     * *产品成本
     */
    @ExcelProperty(value = "*产品成本", index = 12)
    @FieldValid(fieldName = "产品成本",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.AMOUNT2)
    private BigDecimal productCost;
    /**
     * 币种（默认CNY）
     */
    @ExcelProperty(value = "币种（默认CNY）", index = 13)
    @FieldValid(fieldName = "币种",enumClass = CurrencyEnum.class)
    private BigDecimal currency;
    /**
     * 错误信息
     */
    @ExcelProperty(value = "错误数据", index = 14)
    private String errorMsg;
}
