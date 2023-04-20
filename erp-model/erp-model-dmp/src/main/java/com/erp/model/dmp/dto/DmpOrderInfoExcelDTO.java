package com.erp.model.dmp.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.business.service.LocalDateStringConverter;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author Will
 * @version 1.0
 * @description: 任务视图按人员导出DTO
 * @date 2022/11/24 11:25
 */
@Data
@NoArgsConstructor
public class DmpOrderInfoExcelDTO implements Serializable {

    /**
     * 订单号
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "订单号", index = 0)
    private String platformOrderId;

    /**
     * 平台名称
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "平台名称", index = 1)
    private String sourcePlatform;

    /**
     * 店铺名称
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "店铺名称", index = 2)
    private String shopName;

    /**
     * 订单销售额[原币种]
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "订单销售额[原币种]", index = 3)
    private BigDecimal itemTotal;

    /**
     * 订单销售额[RMB-实时]
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "订单销售额[RMB-实时]", index = 4)
    private BigDecimal cnyRealTimeAmount;

    /**
     * 订单销售额[RMB-结算]
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "订单销售额[RMB-结算]", index = 5)
    private BigDecimal cnySettleAmount;

    /**
     * 买家姓名（下单人）
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "下单人", index = 6)
    private String buyerName;

    /**
     * 买家电话1（下单电话1）
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "下单电话1", index = 7)
    private String manPhone;

    /**
     * 买家电话2（下单电话2）
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "下单电话2", index = 8)
    private String secondPhone;

    /**
     * 买家地址1（下单地址1）
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "下单地址1", index = 9)
    private String manStreet;

    /**
     * 买家地址2（下单地址2）
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "下单地址2", index = 10)
    private String secondStreet;

    /**
     * 国家
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "国家", index = 11)
    private String countryNameCn;

    /**
     * 订单状态 2.配货中 3.已发货 4.已完成 5.已作废 6.退货 7.退款
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "订单状态", index = 12)
    private String orderStateName;

    /**
     * 订单修正状态 2.配货中 3.已发货 4.已完成 5.已作废 6.退货 7.退款
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "订单修正状态", index = 13)
    private String correctionStatusName;

    /**
     * 订单下单时间
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "订单下单时间", index = 14,converter= LocalDateStringConverter.class)
    private LocalDateTime platformCreateTime;

    /**
     * 订单发货时间
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "订单发货时间", index = 15)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date deliveryTime;

    /**
     * 销售员
     */
    @ExcelProperty(value = "销售员", index = 16)
    private String chargeName;

    /**
     * 销售事业部
     */
    @ExcelProperty(value = "销售事业部", index = 17)
    private String deptName;

    /**
     * SKU
     */
    @ExcelProperty(value = "SKU", index = 18)
    private String skuNo;

    /**
     * 品名
     */
    @ExcelProperty(value = "品名", index = 19)
    private String itemName;

    /**
     * 单价
     */
    @ExcelProperty(value = "单价", index = 20)
    private BigDecimal sellPriceOrigin;

    /**
     * 数量
     */
    @ExcelProperty(value = "数量", index = 21)
    private Integer quantity;

    /**
     * 销售额[原币种]
     */
    @ExcelProperty(value = "销售额[原币种]", index = 22)
    private BigDecimal sellAmountOrigin;


}
