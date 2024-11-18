package com.erp.model.tms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.business.utils.LocalDateStringConverter;
import com.common.core.anno.FieldValid;
import com.erp.model.tms.enums.FmLogisticTrackStatusEnum;
import com.erp.model.wms.enums.LogisticsMethodEnum;
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
    @FieldValid(fieldName = "运输方式",enumClass = LogisticsMethodEnum.class)
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
    @FieldValid(fieldName = "物流状态",enumClass = FmLogisticTrackStatusEnum.class)
    private String logisticStatusName;

    /**
     * 状态时间
     */
    @ExcelProperty(value = "状态时间", converter= LocalDateStringConverter.class)
    private LocalDateTime statusTime;

    /**
     * 最新轨迹
     */
    @ExcelProperty(value = "最新轨迹")
    private String currencyTrack;

    /**
     * 错误信息
     */
    @ExcelProperty(value = "错误信息")
    private String errorMsg;
}
