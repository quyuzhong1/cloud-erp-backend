package com.erp.model.oms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
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

    
    @ColumnWidth(10)
    @ExcelProperty(value = "*序号")
    @FieldValid(fieldName = "序号", isNotBlank = true ,maxLength = 10,formatPattern= FieldFormatPatternTypeEnum.NUMBER)
    private String no;


    /**
     * 平台订单号
     */
    
    @ColumnWidth(30)
    @ExcelProperty(value = "*平台订单号")
    @FieldValid(fieldName = "平台订单号", isNotBlank = true,maxLength = 30)
    private String platformCode;


    /**
     * 平台
     */
    
    @ColumnWidth(30)
    @ExcelProperty(value = "*平台")
    @FieldValid(fieldName = "平台", isNotBlank = true,maxLength = 30)
    private String dictPlatformName;



    /**
     * 店铺
     */
    
    @ColumnWidth(30)
    @ExcelProperty(value = "*店铺")
    @FieldValid(fieldName = "店铺", isNotBlank = true,maxLength = 30)
    private String shopName;

    /**
     * 订单金额
     */
    
    @ColumnWidth(30)
    @ExcelProperty(value = "*订单金额")
    @FieldValid(fieldName = "订单金额",isNotBlank = true,maxLength = 30,formatPattern= FieldFormatPatternTypeEnum.AMOUNT)
    private String amount;

    /**
     * 币别
     */
    
    @ColumnWidth(10)
    @ExcelProperty(value = "*币别")
    @FieldValid(fieldName = "币别",isNotBlank = true,maxLength = 10)
    private String currency;

    /**
     * 付款时间
     */
    
    @ColumnWidth(30)
    @ExcelProperty(value = "*付款时间")
    @FieldValid(fieldName = "付款时间",isNotBlank = true)
    private String payTime;

    /**
     * 卖家订单编号
     */
    
    @ColumnWidth(30)
    @ExcelProperty(value = "卖家订单编号")
    @FieldValid(fieldName = "卖家订单编号",maxLength = 30)
    private String sellerOrderCode;


    /**
     * 订单分类
     */
    
    @ColumnWidth(30)
    @ExcelProperty(value = "订单分类")
    @FieldValid(fieldName = "订单分类",maxLength = 30)
    private String category;


    /**
     * 订单类型
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "*订单类型")
    @FieldValid(fieldName = "订单类型",isNotBlank = true,maxLength = 30)
    private String transactionSubType;


    /**
     * 订单备注
     */
    
    @ColumnWidth(30)
    @ExcelProperty(value = "订单备注")
    @FieldValid(fieldName = "订单备注",maxLength = 255)
    private String remark;

    /**
     * 物流渠道
     */
    
    @ColumnWidth(30)
    @ExcelProperty(value = "物流渠道")
    @FieldValid(fieldName = "物流渠道",maxLength = 30)
    private String logisticsChannelName;

    /**
     * 运单号
     */
    
    @ColumnWidth(30)
    @ExcelProperty(value = "运单号")
    @FieldValid(fieldName = "运单号",maxLength = 30)
    private String trackNo;


    /**
     * 包装尺寸(长*宽*高)
     */
    
    @ColumnWidth(30)
    @ExcelProperty(value = "包装尺寸(长*宽*高)")
    @FieldValid(fieldName = "包装尺寸(长*宽*高)",maxLength = 30)
    private String packSize;


    /**
     * 预估运费
     */
    
    @ColumnWidth(30)
    @ExcelProperty(value = "预估运费")
    @FieldValid(fieldName = "预估运费",maxLength = 20,formatPattern= FieldFormatPatternTypeEnum.AMOUNT)
    private String estimatedShippingCost;


    /**
     * 实际运费
     */
    
    @ColumnWidth(30)
    @ExcelProperty(value = "实际运费")
    @FieldValid(fieldName = "实际运费",maxLength = 20,formatPattern= FieldFormatPatternTypeEnum.AMOUNT)
    private String actualShippingCost;


    /**
     * 买家全名
     */
    
    @ColumnWidth(30)
    @ExcelProperty(value = "*买家全名")
    @FieldValid(fieldName = "买家全名",isNotBlank = true,maxLength = 50)
    private String customerName;

    /**
     * 买家邮箱
     */
    
    @ColumnWidth(30)
    @ExcelProperty(value = "买家邮箱")
    @FieldValid(fieldName = "买家邮箱",maxLength = 50)
    private String email;


    /**
     * 买家电话
     */
    
    @ColumnWidth(30)
    @ExcelProperty(value = "买家电话")
    @FieldValid(fieldName = "买家电话",maxLength = 20)
    private String telNumber;


    /**
     * countryName
     */
    
    @ColumnWidth(30)
    @ExcelProperty(value = "*国家")
    @FieldValid(fieldName = "国家",isNotBlank = true,maxLength = 30)
    private String countryName;


    /**
     * provinceName
     */
    
    @ColumnWidth(30)
    @ExcelProperty(value = "省/州")
    @FieldValid(fieldName = "省/州",maxLength = 30)
    private String provinceName;
    /**
     * 城市
     */
    
    @ColumnWidth(30)
    @ExcelProperty(value = "*城市")
    @FieldValid(fieldName = "城市",isNotBlank = true,maxLength = 30)
    private String cityName;

    /**
     * 区域
     */
    
    @ColumnWidth(30)
    @ExcelProperty(value = "区域")
    @FieldValid(fieldName = "区域",maxLength = 30)
    private String districtName;

    /**
     * 收货人
     */
    
    @ColumnWidth(30)
    @ExcelProperty(value = "*收货人")
    @FieldValid(fieldName = "收货人",isNotBlank = true,maxLength = 30)
    private String receiverName;
    /**
     * 邮编
     */
    
    @ColumnWidth(30)
    @ExcelProperty(value = "邮编")
    @FieldValid(fieldName = "邮编",maxLength = 30)
    private String postCode;
    /**
     * 收货人电话
     */
    
    @ColumnWidth(30)
    @ExcelProperty(value = "收货人电话")
    @FieldValid(fieldName = "收货人电话",maxLength = 30)
    private String receiverTelNumber;

    /**
     * 收件人税号
     */
    
    @ColumnWidth(30)
    @ExcelProperty(value = "收件人税号")
    @FieldValid(fieldName = "收件人税号",maxLength = 30)
    private String receiverTaxNo;

    /**
     * IE号
     */

    @ColumnWidth(30)
    @ExcelProperty(value = "IE号")
    @FieldValid(fieldName = "IE号",maxLength = 30)
    private String ieNo;

    /**
     * 收件人地址1
     */
    
    @ColumnWidth(30)
    @ExcelProperty(value = "收件人地址1")
    @FieldValid(fieldName = "收件人地址1",maxLength = 255)
    private String firstAddress;

    /**
     * 收件人地址2
     */
    
    @ColumnWidth(30)
    @ExcelProperty(value = "收件人地址2")
    @FieldValid(fieldName = "收件人地址2",maxLength = 255)
    private String secondAddress;

    /**
     * 街道详细地址
     */
    
    @ColumnWidth(30)
    @ExcelProperty(value = "街道详细地址")
    @FieldValid(fieldName = "街道详细地址",maxLength = 255)
    private String fullAddress;

    /**
     * SKU
     */
    
    @ColumnWidth(30)
    @ExcelProperty(value = "*SKU")
    @FieldValid(fieldName = "SKU",isNotBlank = true,maxLength = 64)
    private String skuNo;


    /**
     * 数量
     */
    
    @ColumnWidth(20)
    @ExcelProperty(value = "*数量")
    @FieldValid(fieldName = "数量",isNotBlank = true,maxLength = 10,formatPattern= FieldFormatPatternTypeEnum.NUMBER)
    private String qty;

    /**
     * 仓库
     */
    
    @ColumnWidth(30)
    @ExcelProperty(value = "仓库")
    @FieldValid(fieldName = "仓库",maxLength = 30)
    private String warehouseName;
    /**
     * 真实售价
     */
    
    @ColumnWidth(20)
    @ExcelProperty(value = "*真实售价")
    @FieldValid(fieldName = "真实售价",isNotBlank = true,maxLength = 10,formatPattern= FieldFormatPatternTypeEnum.AMOUNT)
    private String detailAmount;

    /**
     * 错误信息
     */
    @ExcelProperty(value = "错误数据")
    private String errorMsg;

}
