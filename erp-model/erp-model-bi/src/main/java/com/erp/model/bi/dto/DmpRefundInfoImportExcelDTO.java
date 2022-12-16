package com.erp.model.bi.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

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
    @ExcelProperty(value = "退款单号", index = 0)
    private String refundId;

    /**
     * 平台订单编号
     */
    @ExcelProperty(value = "平台订单编号", index = 1)
    private String platformOrderId;

    /**
     * 平台名称
     */
    @ExcelProperty(value = "平台名称", index = 2)
    private String platformName;

    /**
     * 店铺名称
     */
    @ExcelProperty(value = "店铺名称", index = 3)
    private String shopName;

    /**
     * SKU
     */
    @ExcelProperty(value = "SKU", index = 4)
    private String skuNo;

    /**
     * 退款金额
     */
    @ExcelProperty(value = "退款金额", index = 5)
    private String refundAmount;

    /**
     * 汇率
     */
    @ExcelProperty(value = "汇率", index = 6)
    private String currencyRate;

    /**
     * cny-结算汇率
     */
    @ExcelProperty(value = "结算汇率", index = 7)
    private String cnySettleRate;

    /**
     * 退款数量
     */
    @ExcelProperty(value = "退款数量", index = 8)
    private String refundNum;

    /**
     * 退款状态：1、新建退款 2、审核中 3、财务审核 4、成功 5、失败 6、作废
     */
    @ExcelProperty(value = "退款状态", index = 9)
    private String refundStatus;

    /**
     * 退款时间
     */
    @ExcelProperty(value = "退款时间", index = 10)
    private String refundTime;

    /**
     * 原订单时间
     */
    @ExcelProperty(value = "原订单时间", index = 11)
    private String orderTime;

    /**
     * 错误信息
     */
    @ExcelProperty(value = "错误信息", index = 12)
    private String errorMsg;
}
