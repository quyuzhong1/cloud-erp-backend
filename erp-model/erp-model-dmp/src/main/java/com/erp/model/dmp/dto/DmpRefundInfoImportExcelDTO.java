package com.erp.model.dmp.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Will
 * @version 1.0
 * @description: 任务视图按人员导出DTO
 * @date 2022/11/24 11:25
 */
@Data
@NoArgsConstructor
public class DmpRefundInfoImportExcelDTO implements Serializable {

    /**
     * 退款单号
     */
    @ExcelProperty(value = "*退款单号", index = 0)
    @FieldValid(fieldName = "退款单号",isNotBlank = true,maxLength = 50)
    private String refundCode;

    /**
     * 平台订单编号
     */
    @ExcelProperty(value = "*平台订单编号", index = 1)
    @FieldValid(fieldName = "平台订单编号",isNotBlank = true,maxLength = 50)
    private String platformOrderId;

    /**
     * 平台名称
     */
    @ExcelProperty(value = "*平台名称", index = 2)
    @FieldValid(fieldName = "平台名称",isNotBlank = true)
    private String platformName;

    /**
     * 店铺名称
     */
    @ExcelProperty(value = "*店铺名称", index = 3)
    @FieldValid(fieldName = "店铺名称",isNotBlank = true)
    private String shopName;

    /**
     * SKU
     */
    @ExcelProperty(value = "*SKU", index = 4)
    @FieldValid(fieldName = "SKU",isNotBlank = true)
    private String skuNo;

    /**
     * 退款金额
     */
    @ExcelProperty(value = "*退款金额", index = 5)
    private BigDecimal refundAmount;

    /**
     * 汇率
     */
    @ExcelProperty(value = "汇率", index = 6)
    private BigDecimal currencyRate;

    /**
     * cny-结算汇率
     */
    @ExcelProperty(value = "结算汇率", index = 7)
    private BigDecimal cnySettleRate;

    /**
     * 退款数量
     */
    @ExcelProperty(value = "*退款数量", index = 8)
    private Integer refundNum;

    /**
     * 退款状态：1、新建退款 2、审核中 3、财务审核 4、成功 5、失败 6、作废
     */
    @ExcelProperty(value = "*退款状态", index = 9)
    @FieldValid(fieldName = "退款状态",isNotBlank = true)
    private String refundStatusName;

    /**
     * 退款时间
     */
    @ExcelProperty(value = "*退款时间", index = 10)
    private Date refundTime;

    /**
     * 币种
     */
    @ExcelProperty(value = "*币种", index = 11)
    @FieldValid(fieldName = "币种",isNotBlank = true)
    private String currencyCode;

    /**
     * 错误信息
     */
    @ExcelProperty(value = "错误信息", index = 12)
    private String errorMsg;
}
