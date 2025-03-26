package com.erp.model.oms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 *
 * @author jack
 */
@Data
@NoArgsConstructor
public class B2CSoImportExcelDTO {

    
    @ExcelProperty(value = "*序号")
    @FieldValid(fieldName = "序号", isNotBlank = true )
    private String no;


    /**
     * 平台订单号
     */
    
    @ExcelProperty(value = "*平台订单号")
    @FieldValid(fieldName = "平台订单号", isNotBlank = true)
    private String platformCode;


    /**
     * 平台
     */
    
    @ExcelProperty(value = "*平台")
    @FieldValid(fieldName = "平台", isNotBlank = true)
    private String dictPlatformName;



    /**
     * 店铺
     */
    
    @ExcelProperty(value = "*店铺")
    @FieldValid(fieldName = "店铺", isNotBlank = true)
    private String shopName;

    /**
     * 订单金额
     */
    
    @ExcelProperty(value = "*订单金额")
    @FieldValid(fieldName = "订单金额",isNotBlank = true,formatPattern= FieldFormatPatternTypeEnum.AMOUNT)
    private String amount;

    /**
     * 币别
     */
    
    @ExcelProperty(value = "*币别")
    @FieldValid(fieldName = "币别",isNotBlank = true)
    private String currency;

    /**
     * 付款时间
     */
    
    @ExcelProperty(value = "*付款时间")
    @FieldValid(fieldName = "付款时间",isNotBlank = true,formatPattern= FieldFormatPatternTypeEnum.DATE)
    private String payTime;

    /**
     * 卖家订单编号
     */
    
    @ExcelProperty(value = "卖家订单编号")
    @FieldValid(fieldName = "卖家订单编号")
    private String sellerOrderCode;


    /**
     * 订单分类
     */
    
    @ExcelProperty(value = "订单分类")
    @FieldValid(fieldName = "订单分类")
    private String category;


    /**
     * 订单类型
     */
    @ExcelProperty(value = "*订单类型")
    @FieldValid(fieldName = "订单类型",isNotBlank = true)
    private String transactionSubType;


    /**
     * 订单备注
     */
    
    @ExcelProperty(value = "订单备注")
    @FieldValid(fieldName = "订单备注")
    private String remark;

    /**
     * 物流渠道
     */
    
    @ExcelProperty(value = "物流渠道")
    @FieldValid(fieldName = "物流渠道")
    private String logisticsChannelName;

    /**
     * 跟踪号
     */
    
    @ExcelProperty(value = "跟踪号")
    @FieldValid(fieldName = "跟踪号")
    private String trackNo;


    /**
     * 包装尺寸(长*宽*高)
     */
    
    @ExcelProperty(value = "包装尺寸(长*宽*高)")
    @FieldValid(fieldName = "包装尺寸(长*宽*高)")
    private String packSize;


    /**
     * 预估运费
     */
    
    @ExcelProperty(value = "预估运费")
    @FieldValid(fieldName = "预估运费",formatPattern= FieldFormatPatternTypeEnum.AMOUNT)
    private String estimatedShippingCost;


    /**
     * 实际运费
     */
    
    @ExcelProperty(value = "实际运费")
    @FieldValid(fieldName = "实际运费",formatPattern= FieldFormatPatternTypeEnum.AMOUNT)
    private String actualShippingCost;


    /**
     * 买家全名
     */
    
    @ExcelProperty(value = "*买家全名")
    @FieldValid(fieldName = "买家全名",isNotBlank = true)
    private String customerName;

    /**
     * 买家邮箱
     */
    
    @ExcelProperty(value = "买家邮箱")
    @FieldValid(fieldName = "买家邮箱")
    private String email;


    /**
     * 买家电话
     */
    
    @ExcelProperty(value = "买家电话")
    @FieldValid(fieldName = "买家电话")
    private String telNumber;


    /**
     * countryName
     */
    
    @ExcelProperty(value = "国家")
    @FieldValid(fieldName = "国家",isNotBlank = true)
    private String countryName;


    /**
     * provinceName
     */
    
    @ExcelProperty(value = "省/州")
    @FieldValid(fieldName = "省/州")
    private String provinceName;
    /**
     * 城市
     */
    
    @ExcelProperty(value = "*城市")
    @FieldValid(fieldName = "城市",isNotBlank = true)
    private String cityName;

    /**
     * 区域
     */
    
    @ExcelProperty(value = "区域")
    @FieldValid(fieldName = "区域")
    private String districtName;

    /**
     * 收货人
     */
    
    @ExcelProperty(value = "*收货人")
    @FieldValid(fieldName = "收货人",isNotBlank = true)
    private String receiverName;
    /**
     * 邮编
     */
    
    @ExcelProperty(value = "邮编")
    @FieldValid(fieldName = "邮编")
    private String postCode;
    /**
     * 收货人电话
     */
    
    @ExcelProperty(value = "收货人电话")
    @FieldValid(fieldName = "收货人电话")
    private String receiverTelNumber;

    /**
     * 收件人税号
     */
    
    @ExcelProperty(value = "收件人税号")
    @FieldValid(fieldName = "收件人税号")
    private String receiverTaxNo;

    /**
     * 收件人地址1
     */
    
    @ExcelProperty(value = "收件人地址1")
    @FieldValid(fieldName = "收件人地址1")
    private String firstAddress;

    /**
     * 收件人地址2
     */
    
    @ExcelProperty(value = "收件人地址2")
    @FieldValid(fieldName = "收件人地址2")
    private String secondAddress;

    /**
     * 街道详细地址
     */
    
    @ExcelProperty(value = "街道详细地址")
    @FieldValid(fieldName = "街道详细地址")
    private String fullAddress;

    /**
     * SKU
     */
    
    @ExcelProperty(value = "*SKU")
    @FieldValid(fieldName = "SKU",isNotBlank = true)
    private String skuNo;


    /**
     * 数量
     */
    
    @ExcelProperty(value = "数量")
    @FieldValid(fieldName = "数量",isNotBlank = true)
    private String qty;

    /**
     * 仓库
     */
    
    @ExcelProperty(value = "仓库")
    @FieldValid(fieldName = "仓库")
    private String warehouseName;
    /**
     * 真实售价
     */
    
    @ExcelProperty(value = "*真实售价")
    @FieldValid(fieldName = "真实售价",isNotBlank = true,formatPattern= FieldFormatPatternTypeEnum.AMOUNT)
    private String detailAmount;

    /**
     * 错误信息
     */
    @ExcelProperty(value = "错误数据")
    private String errorMsg;

}
