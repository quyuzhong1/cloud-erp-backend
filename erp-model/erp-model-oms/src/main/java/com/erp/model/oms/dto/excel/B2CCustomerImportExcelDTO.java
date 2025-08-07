package com.erp.model.oms.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 *
 * @author jack
 */
@Data
@NoArgsConstructor
public class B2CCustomerImportExcelDTO {

    
    @ColumnWidth(10)
    @ExcelProperty(value = "销售单号")
    @FieldValid(fieldName = "销售单号" ,maxLength = 32)
    private String code;


    /**
     * 平台订单号
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "平台订单号")
    @FieldValid(fieldName = "平台订单号",maxLength = 32)
    private String platformCode;

    /**
     * 买家全名
     */
    
    @ColumnWidth(30)
    @ExcelProperty(value = "买家全名")
    @FieldValid(fieldName = "买家全名",maxLength = 100)
    private String customerName;

    /**
     * 买家邮箱
     */
    
    @ColumnWidth(30)
    @ExcelProperty(value = "买家邮箱")
    @FieldValid(fieldName = "买家邮箱",maxLength = 100)
    private String email;


    /**
     * 买家电话
     */
    
    @ColumnWidth(30)
    @ExcelProperty(value = "买家电话")
    @FieldValid(fieldName = "买家电话",maxLength = 32)
    private String telNumber;


    /**
     * countryName
     */
    
    @ColumnWidth(30)
    @ExcelProperty(value = "国家")
    @FieldValid(fieldName = "国家",maxLength = 50)
    private String country;


    /**
     * provinceName
     */
    
    @ColumnWidth(30)
    @ExcelProperty(value = "省/州")
    @FieldValid(fieldName = "省/州",maxLength = 32)
    private String provinceName;
    /**
     * 城市
     */
    
    @ColumnWidth(30)
    @ExcelProperty(value = "城市")
    @FieldValid(fieldName = "城市",maxLength = 128)
    private String cityName;

    /**
     * 区域
     */
    
    @ColumnWidth(30)
    @ExcelProperty(value = "区域")
    @FieldValid(fieldName = "区域",maxLength = 255)
    private String districtName;

    /**
     * 收货人
     */
    
    @ColumnWidth(30)
    @ExcelProperty(value = "*收货人")
    @FieldValid(fieldName = "收货人",isNotBlank = true,maxLength = 255)
    private String receiverName;
    /**
     * 邮编
     */
    
    @ColumnWidth(30)
    @ExcelProperty(value = "邮编")
    @FieldValid(fieldName = "邮编",maxLength = 32)
    private String postCode;
    /**
     * 收货人电话
     */
    
    @ColumnWidth(30)
    @ExcelProperty(value = "收货人电话")
    @FieldValid(fieldName = "收货人电话",maxLength = 32)
    private String receiverTelNumber;

    /**
     * 收件人税号
     */
    
    @ColumnWidth(30)
    @ExcelProperty(value = "收件人税号")
    @FieldValid(fieldName = "收件人税号",maxLength = 64)
    private String receiverTaxNo;

    /**
     * IE号
     */

    @ColumnWidth(30)
    @ExcelProperty(value = "IE号")
    @FieldValid(fieldName = "IE号",maxLength = 64)
    private String ieNo;

    /**
     * 收件人地址1
     */
    
    @ColumnWidth(30)
    @ExcelProperty(value = "收货地址1")
    @FieldValid(fieldName = "收件人地址1",maxLength = 255)
    private String firstAddress;

    /**
     * 收件人地址2
     */
    
    @ColumnWidth(30)
    @ExcelProperty(value = "收货地址2")
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
     * 错误信息
     */
    @ExcelProperty(value = "错误数据")
    private String errorMsg;

    @ExcelIgnore
    private String countryName;

}
