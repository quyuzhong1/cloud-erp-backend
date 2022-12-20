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
public class DmpReturnOrderInfoExcelDTO implements Serializable {

    /**
     * 退货单号
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "退货单号", index = 0)
    private String returnOrderId;

    /**
     * 原订单号
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "原订单号", index = 1)
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
     * 退货数量
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "退货数量", index = 5)
    private BigDecimal refundNum;

    /**
     * 退货金额
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "退货金额", index = 6)
    private BigDecimal orderFee;


    /**
     * 退货金额[RMB-实时]
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "退款金额[RMB-实时]", index = 7)
    private BigDecimal cnyRealTimeAmount;

    /**
     * 退货金额[RMB-结算]
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "退货金额[RMB-结算]", index = 8)
    private BigDecimal cnySettleAmount;


    /**
     * 退货状态：1待处理 2已退款 3已重发 4已完成 5已作废
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "退货状态", index = 8)
    private String status;

    /**
     * 退货时间
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "退货时间", index = 8)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private String returnCreateTime;

    /**
     * 原订单时间
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "原订单时间", index = 9)
    private String orderTime;
}
