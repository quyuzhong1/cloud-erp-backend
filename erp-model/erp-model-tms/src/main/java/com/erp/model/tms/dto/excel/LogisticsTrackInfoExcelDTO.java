package com.erp.model.tms.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Will
 * @version 1.0
 * @description: 自发货费用
 * @date 2023/11/14 15:47
 */
@Data
public class LogisticsTrackInfoExcelDTO implements Serializable {

    /**
     * 店铺名称
     */
    @ExcelProperty(value = "店铺名称" , index = 0)
    @FieldValid(fieldName = "店铺名称",maxLength = 32)
    private String  shopName;

    /**
     * 渠道名称
     */
    @ExcelProperty(value = "渠道名称", index = 1)
    @FieldValid(fieldName = "渠道名称",isNotBlank = true,maxLength = 50)
    private String  channelName;

    /**
     * 发货时间
     */
    @ExcelProperty(value = "发货时间", index = 2)
    @FieldValid(fieldName = "发货时间")
    private String  deliveryTimeStr;
    @ExcelIgnore
    private LocalDateTime deliveryTime;

    /**
     * 运输单号
     */
    @ExcelProperty(value = "运输单号", index = 3)
    @FieldValid(fieldName = "运输单号")
    private String  transportNo;

    /**
     * 跟踪号
     */
    @ExcelProperty(value = "跟踪号", index = 4)
    @FieldValid(fieldName = "跟踪号")
    private String  trackNo;

    /**
     * 注册状态（0未注册1注册成功-1注册失败）
     */
    @ExcelProperty(value = "注册状态（0未注册1注册成功-1注册失败）", index = 5)
    @FieldValid(fieldName = "注册状态（0未注册1注册成功-1注册失败）")
    private String  registerStatusStr;
    @ExcelIgnore
    private Integer registerStatus;

    /**
     * 尾号后四位
     */
    @ExcelProperty(value = "尾号后四位", index = 6)
    @FieldValid(fieldName = "尾号后四位",isNotBlank = true)
    private String  mobileLastFour;

    /**
     * 销售平台
     */
    @ExcelProperty(value = "销售平台", index = 7)
    @FieldValid(fieldName = "销售平台")
    private String  salePlatform;

    /**
     * 手机号
     */
    @ExcelProperty(value = "手机号", index = 8)
    @FieldValid(fieldName = "手机号",formatPattern = FieldFormatPatternTypeEnum.MOBILE)
    private String  mobile;


    /**
     * 错误信息
     */
    private String errorMsg;
}
