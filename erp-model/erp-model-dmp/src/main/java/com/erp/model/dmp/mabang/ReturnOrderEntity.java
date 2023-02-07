package com.erp.model.dmp.mabang;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@ToString
public class ReturnOrderEntity {

    private String _id;
    private String platformOrderId;
    private String shopId;
    private LocalDateTime paidTime;
    private LocalDateTime expressTime;
    private Integer status;
    private String salesRecordNumber;
    private BigDecimal orderFee;
    private BigDecimal orderWeight;
    private Integer myLogisticsChannelId;
    private String trackNumber;
    private String platformId;
    private Integer type;
    private String countryCode;
    private String buyerUserId;
    private String buyerName;
    private String employeeId;
    private String remark;
    private String currencyId;
    private BigDecimal currencyRate;
    private LocalDateTime createDate;
    private LocalDateTime refundTime;
    private String inTime;
    @SerializedName("update_time")
    private String updateTime;
    private String myLogisticsChannelName;
    private Integer myLogisticsId;
    private String myLogisticsName;
    private String countryNameEN;
    private String countryNameCN;
    private String shopName;
    private String forecastChannel;
    private Integer forecastStatus;
    private String employeeName;
    @SerializedName("item")
    private List<ReturnOrderItemEntity> item;
}
