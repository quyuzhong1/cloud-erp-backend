package com.erp.model.tms.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 物流下单导出实体
 */
@Data
@NoArgsConstructor
public class TmsLogisticsOrderExcelDTO implements Serializable {

    /**
     * 单据编号
     */
    private String code;

    /**
     * 物流渠道id
     */
    @ExcelIgnore
    private String logisticsChannelId;

    /**
     * 物流渠道名称
     */
    private String logisticsChannelName;

    /**
     * 物流跟踪号
     */
    private String trackNo;

    /**
     * 运单号
     */
    private String transportNo;

    /**
     * 来源单号
     */
    private String sourceCode;

    /**
     * 来源单据类型
     */
    @ExcelIgnore
    private String sourceType;

    /**
     * 来源单据类型名称
     */
    private String sourceTypeName;

    /**
     * 备注
     */
    private String remark;

    /**
     * 单据状态
     */
    @ExcelIgnore
    private String status;

    /**
     * 单据状态名称
     */
    private String statusName;

    /**
     * 面单状态
     */
    @ExcelIgnore
    private String labelStatus;

    /**
     * 面单状态名称
     */
    private String labelStatusName;

    /**
     * 异常原因
     */
    private String exceptionReason;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 创建人名称
     */
    private String createUserName;

    /**
     * 售后申请id,after_sale.id
     */
    @ExcelIgnore
    private String afterSaleId;

    /**
     * 异常类型
     */
    @ExcelIgnore
    private String exceptionType;

    /**
     * 物流平台
     */
    @ExcelIgnore
    private String logisticsPlatform;

    /**
     * 物流平台名称
     */
    @ExcelIgnore
    private String logisticsPlatformName;

    /**
     * 收件人
     */
    @ExcelIgnore
    private String receiver;

    /**
     * 电话
     */
    @ExcelIgnore
    private String contactNumber;

    /**
     * 国家,dict_country.id
     */
    @ExcelIgnore
    private String country;

    /**
     * 省/州
     */
    @ExcelIgnore
    private String province;

    /**
     * 城市
     */
    @ExcelIgnore
    private String city;

    /**
     * 详细地址
     */
    @ExcelIgnore
    private String detailedAddress;

}
