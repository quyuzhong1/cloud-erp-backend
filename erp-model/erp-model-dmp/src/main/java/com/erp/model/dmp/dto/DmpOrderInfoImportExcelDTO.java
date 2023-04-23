package com.erp.model.dmp.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/16 11:08
 */
@Data
@NoArgsConstructor
public class DmpOrderInfoImportExcelDTO implements Serializable {

    /**
     * 订单号
     */
    @ExcelProperty(value = "*订单号", index = 0)
    @FieldValid(fieldName = "订单号",isNotBlank = true)
    private String platformOrderId;

    /**
     * 平台名称
     */
    @ExcelProperty(value = "*平台名称", index = 1)
    @FieldValid(fieldName = "平台名称",isNotBlank = true)
    private String sourcePlatform;

    /**
     * 站点
     */
    @ExcelProperty(value = "*站点", index = 2)
    @FieldValid(fieldName = "站点",isNotBlank = true)
    private String site;

    /**
     * 店铺名称
     */
    @ExcelProperty(value = "*店铺名称", index = 3)
    @FieldValid(fieldName = "店铺名称",isNotBlank = true)
    private String shopName;

    /**
     * 订单销售额[原币种]
     */
    @ExcelProperty(value = "*订单销售额[原币种]", index = 4)
    private BigDecimal orderFee;

    /**
     * 汇率
     */
    @ExcelProperty(value = "汇率", index = 5)
    private BigDecimal currencyRate;

    /**
     * 结算汇率
     */
    @ExcelProperty(value = "结算汇率", index = 6)
    private BigDecimal cnySettleRate;

    /**
     * 买家姓名（下单人）
     */
    @ExcelProperty(value = "*下单人", index = 7)
    @FieldValid(fieldName = "下单人",isNotBlank = true)
    private String buyerName;

    /**
     * 买家电话1（下单电话1）
     */
    @ExcelProperty(value = "下单电话1", index = 8)
    private String manPhone;

    /**
     * 买家电话2（下单电话2）
     */
    @ExcelProperty(value = "下单电话2", index = 9)
    private String secondPhone;

    /**
     * 买家地址1（下单地址1）
     */
    @ExcelProperty(value = "下单地址1", index = 10)
    private String manStreet;

    /**
     * 买家地址2（下单地址2）
     */
    @ExcelProperty(value = "下单地址2", index = 11)
    private String secondStreet;

    /**
     * 国家名称
     */
    @ExcelProperty(value = "*国家名称", index = 12)
    @FieldValid(fieldName = "国家名称",isNotBlank = true)
    private String countryNameCn;

    /**
     * 订单状态 2.配货中 3.已发货 4.已完成 5.已作废
     */
    @ExcelProperty(value = "*订单状态", index = 13)
    @FieldValid(fieldName = "订单状态",isNotBlank = true)
    private String orderStateName;

    /**
     * 订单下单时间
     */
    @ExcelProperty(value = "*订单下单时间", index = 14)
    @FieldValid(fieldName = "订单下单时间",isNotBlank = true ,formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String platformCreateTimeStr;

    /**
     * 订单发货时间
     */
    @ExcelProperty(value = "订单发货时间", index = 15)
    @FieldValid(fieldName = "订单发货时间",isNotBlank = true ,formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String deliveryTimeStr;

    /**
     * 销售员
     */
    @ExcelProperty(value = "*销售员", index = 16)
    @FieldValid(fieldName = "销售员",isNotBlank = true)
    private String chargeName;

    /**
     * 销售事业部
     */
    @ExcelProperty(value = "*销售事业部", index = 17)
    @FieldValid(fieldName = "销售事业部",isNotBlank = true)
    private String deptName;

    /**
     * 币种
     */
    @ExcelProperty(value = "币种", index = 18)
    private String currencyCode;

    /**
     * SKU
     */
    @ExcelProperty(value = "*SKU", index = 19)
    @FieldValid(fieldName = "SKU",isNotBlank = true)
    private String skuNo;

    /**
     * 品名
     */
    @ExcelProperty(value = "*品名", index = 20)
    @FieldValid(fieldName = "品名",isNotBlank = true)
    private String itemName;

    /**
     * 单价
     */
    @ExcelProperty(value = "*单价", index = 21)
    private BigDecimal sellPriceOrigin;

    /**
     * 数量
     */
    @ExcelProperty(value = "*数量", index = 22)
    private Integer quantity;

    /**
     * 错误信息
     */
    @ExcelProperty(value = "错误信息", index = 23)
    private String errorMsg;
}
