package com.erp.model.bi.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Will
 * @version 1.0
 * @description: 任务视图按人员导出DTO
 * @date 2022/11/24 11:25
 */
@Data
@NoArgsConstructor
public class DmpRefundInfoExcelDTO implements Serializable {

    /**
     * 退款单号
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "退款单号", index = 0)
    private String refundId;

    /**
     * 平台订单编号
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "平台订单编号", index = 1)
    private String platformOrderId;

    /**
     * 平台名称
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "平台名称", index = 2)
    private String platformName;

    /**
     * 店铺名称
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "店铺名称", index = 3)
    private String shopName;

    /**
     * SKU
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "SKU", index = 4)
    private String skuNo;

    /**
     * 退货金额
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "退货金额", index = 5)
    private BigDecimal refundAmount;


    /**
     * 退款金额[RMB-实时]
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "退款金额[RMB-实时]", index = 6)
    private BigDecimal cnyRealTimeAmount;

    /**
     * 退款金额[RMB-实时]
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "退款金额[RMB-实时]", index = 7)
    private BigDecimal cnySettleAmount;

    /**
     * 退款数量
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "退款数量", index = 8)
    private BigDecimal refundNum;

    /**
     * 退款状态：1、新建退款 2、审核中 3、财务审核 4、成功 5、失败 6、作废
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "退款状态", index = 9)
    private String refundStatus;

    /**
     * 退款时间
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "退款时间", index = 10)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private String refundTime;

    /**
     * 原订单时间
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "原订单时间", index = 11)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private String orderTime;
}
