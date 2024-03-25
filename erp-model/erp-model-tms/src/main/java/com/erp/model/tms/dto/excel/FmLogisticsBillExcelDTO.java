package com.erp.model.tms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.CurrencyEnum;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class FmLogisticsBillExcelDTO implements Serializable {

    /**
     * 来源单
     */
    @ExcelProperty(value = "*来源单")
    @FieldValid(fieldName = "来源单",isNotBlank = true,maxLength = 32)
    private String outstockCode;

    /**
     * 运输方式
     */
    @ExcelProperty(value = "运输方式")
    private String shippingMethodName;

    /**
     * 物流商
     */
    @ExcelProperty(value = "物流商")
    private String supplierName;

    /**
     * 物流渠道
     */
    @ExcelProperty(value = "物流渠道")
    private String channelName;

    /**
     * 运单号
     */
    @ExcelProperty(value = "运单号")
    private String transportNo;

    /**
     * 柜号
     */
    @ExcelProperty(value = "柜号")
    private String counterNo;


    /**
     * 物流状态
     */
    @ExcelProperty(value = "物流状态")
    private String logisticStatusName;

    /**
     * 状态时间
     */
    @ExcelProperty(value = "状态时间")
    private LocalDateTime statusTime;

    /**
     * 最新轨迹
     */
    @ExcelProperty(value = "最新轨迹")
    private String currencyTrack;

    /**
     * 错误信息
     */
    private String errorMsg;
}
