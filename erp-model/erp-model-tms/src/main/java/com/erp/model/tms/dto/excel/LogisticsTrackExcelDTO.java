package com.erp.model.tms.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.common.business.utils.LocalDateStringConverter;
import com.common.core.anno.FieldValid;
import com.erp.model.tms.enums.FmLogisticTrackStatusEnum;
import com.erp.model.wms.enums.LogisticsMethodEnum;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class LogisticsTrackExcelDTO implements Serializable {

    /**
     * 订单号
     */
    @ExcelProperty(value = "订单号")
    @FieldValid(fieldName = "订单号",maxLength = 32)
    private String code;
    /**
     * 出库单单号
     */
    @ExcelProperty(value = "出库单号")
    @FieldValid(fieldName = "出库单单号",maxLength = 32)
    private String outstockCode;
    /**
     * 物流跟踪号
     */
    @ExcelProperty(value = "*物流跟踪号")
    @FieldValid(fieldName = "物流跟踪号",maxLength = 32,isNotBlank = true)
    private String trackNo;

    /**
     * 运输状态
     */
    @ExcelProperty(value = "*运输状态")
    @FieldValid(fieldName = "运输状态",isNotBlank = true,maxLength = 50)
    private String trackStatusName;
    @ExcelIgnore
    private String trackStatus;

    /**
     * 状态时间
     */
    @ExcelProperty(value = "*状态时间")
    @FieldValid(fieldName = "状态时间", isNotBlank = true,maxLength = 50)
    private String statusTimeStr;
    private LocalDateTime statusTime;

    /**
     * 轨迹描述
     */
    @ExcelProperty(value = "轨迹描述")
    @FieldValid(fieldName = "轨迹描述",maxLength = 200)
    private String trackDesc;

    /**
     * 错误信息
     */
    @ExcelProperty(value = "错误信息")
    private String errorMsg;
}
