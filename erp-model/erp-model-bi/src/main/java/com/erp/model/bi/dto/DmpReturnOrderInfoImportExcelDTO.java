package com.erp.model.bi.dto;

import com.alibaba.excel.annotation.ExcelProperty;
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
public class DmpReturnOrderInfoImportExcelDTO implements Serializable {

    /**
     * 退货单号
     */
    @ExcelProperty(value = "退货单号", index = 0)
    private String returnOrderId;

    /**
     * 原订单号
     */
    @ExcelProperty(value = "原订单号", index = 1)
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
     * 退货数量
     */
    @ExcelProperty(value = "退货数量", index = 5)
    private Integer refundNum;

    /**
     * 退货金额
     */
    @ExcelProperty(value = "退货金额", index = 6)
    private BigDecimal orderFee;


    /**
     * 汇率
     */
    @ExcelProperty(value = "汇率", index = 7)
    private BigDecimal currencyRate;

    /**
     * cny-结算汇率
     */
    @ExcelProperty(value = "结算汇率", index = 8)
    private BigDecimal cnySettleRate;


    /**
     * 退货状态：1待处理 2已退款 3已重发 4已完成 5已作废
     */
    @ExcelProperty(value = "退货状态", index = 9)
    private String statusName;

    /**
     * 退货时间
     */
    @ExcelProperty(value = "退货时间", index = 10)
    private Date returnCreateTime;

    /**
     * 原订单时间
     */
    @ExcelProperty(value = "原订单时间", index = 11)
    private Date orderTime;

    /**
     * 错误信息
     */
    @ExcelProperty(value = "错误信息", index = 12)
    private String errorMsg;
}
