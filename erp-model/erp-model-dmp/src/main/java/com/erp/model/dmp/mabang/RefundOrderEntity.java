package com.erp.model.dmp.mabang;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@ToString
public class RefundOrderEntity {
    private String id;
    private Integer refundOrderId;
    private String refundPaypalId;
    private String refundMoneyType;
    private String refundMoney;
    private BigDecimal applyRefundMoney;
    private Integer refundType;
    private String refundReasonName;
    private String refundReasonDesc;
    private String note;
    private String content;
    private Integer type;
    private Integer flag;
    private String createTime;
    private String errorDescr;
    private Integer orderStatus;
    private String shopId;
    private String shopName;
    private String platformName;
    private String platformOrderId;
    private String refundplatformOrderId;
    private String refundTime;
    private BigDecimal currencyRate;
    private String expressType;
    private String logisticsChannelName;
    private String countryCode;
    private String countryCn;
    private String countryEn;
    private String salesRecordNumber;
    private String buyerUserId;
    private String buyerName;
    private String currencyId;
    private BigDecimal itemTotalOrigin;
    private BigDecimal shippingTotalOrigin;
    private String paypalId;
    private String orderTime;
    private String expressTime;
    private Integer source;
    private String pictureUrl;
    private String updateTime;
    private String refundText;
    private String complaintId;
    private String trackNumber;
    private List<RefundOrderItemEntity> productList;
}
