package com.erp.model.tms.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.erp.model.tms.enums.LogisticsThirdChannelRefPushTypeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * @description: 公司信息导入DTO
 * @author zdy
 * @date: 2024/5/8 14:19
 */
@Data
public class ImportLogisticsThirdChannelRefExcelDTO implements Serializable {


    /**
     * 序号
     */
    @ExcelProperty(value = "*序号", index = 0)
    @FieldValid(fieldName = "序号", isNotBlank = true ,maxLength = 64)
    private String serialNumber;

    /**
     * *我司物流商
     */
    @ExcelProperty(value = "*我司物流商", index = 1)
    @FieldValid(fieldName = "我司物流商", isNotBlank = true ,maxLength = 64)
    private String logisticsSupplierName;
    @ExcelIgnore
    private String logisticsSupplierId;

    /**
     * *我司渠道
     */
    @ExcelProperty(value = "*我司渠道", index = 2)
    @FieldValid(fieldName = "我司渠道",isNotBlank = true ,maxLength = 64)
    private String logisticsChannelName;
    @ExcelIgnore
    private String logisticsChannelId;

    /**
     * *查询服务商
     */
    @ExcelProperty(value = "*查询服务商", index = 3)
    @FieldValid(fieldName = "查询服务商",isNotBlank = true ,maxLength = 64)
    private String platformTypeName;
    @ExcelIgnore
    private String platformType;

    /**
     * 查询物流商
     */
    @ExcelProperty(value = "查询物流商", index = 4)
    @FieldValid(fieldName = "查询物流商",maxLength = 64)
    private String thirdSupplierName;
    /**
     * 物流商编码
     */
    @ExcelProperty(value = "物流商编码", index = 5)
    @FieldValid(fieldName = "物流商编码",maxLength = 64)
    private String thirdSupplierCode;
    /**
     * *查询渠道名称
     */
    @ExcelProperty(value = "查询渠道名称", index = 6)
    @FieldValid(fieldName = "查询渠道名称",maxLength = 64)
    private String thirdChannelName;
    /**
     * *是否推送电话
     */
    @ExcelProperty(value = "*是否推送电话", index = 7)
    @FieldValid(fieldName = "是否推送电话",isNotBlank = true ,maxLength = 64,fieldValues = "是,否")
    private String pushMobileName;
    @ExcelIgnore
    private Boolean isPushMobile;
    /**
     * 推送类型
     * LogisticsThirdChannelRefPushTypeEnum
     */
    @ExcelProperty(value = "*推送类型", index = 8)
    @FieldValid(fieldName = "推送类型",isNotBlank = true ,maxLength = 64, enumClass = LogisticsThirdChannelRefPushTypeEnum.class)
    private String pushTypeName;
    @ExcelIgnore
    private String pushType;
    /**
     * 默认手机号
     */
    @ExcelProperty(value = "默认手机号", index = 9)
    @FieldValid(fieldName = "默认手机号",maxLength = 64)
    private String mobile;
    /**
     * 店铺/平台名称
     */
    @ExcelProperty(value = "店铺/平台名称", index = 10)
    @FieldValid(fieldName = "店铺/平台名称",maxLength = 64)
    private String shopName;
    @ExcelIgnore
    private String shopId;
    @ExcelIgnore
    private String dictPlatform;
    /**
     * 店铺/平台手机号码
     */
    @ExcelProperty(value = "店铺/平台手机号码", index = 11)
    @FieldValid(fieldName = "店铺/平台手机号码",maxLength = 64)
    private String shopPhone;

    /**
     * 错误数据
     */
    private String  errorMsg;

}
