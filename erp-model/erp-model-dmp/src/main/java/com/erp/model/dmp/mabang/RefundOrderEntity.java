package com.erp.model.dmp.mabang;

import com.erp.model.dmp.mabang.item.RefundOrderItemEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
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
    /**
     * 清洗数据
     */
    private Boolean isClean;

    @Override
    public String toString() {
        return "RefundOrderEntity{" +
                "id='" + id + '\'' +
                ", refundOrderId=" + refundOrderId +
                ", refundPaypalId='" + refundPaypalId + '\'' +
                ", refundMoneyType='" + refundMoneyType + '\'' +
                ", refundMoney='" + refundMoney + '\'' +
                ", applyRefundMoney=" + applyRefundMoney +
                ", refundType=" + refundType +
                ", refundReasonName='" + refundReasonName + '\'' +
                ", refundReasonDesc='" + refundReasonDesc + '\'' +
                ", note='" + note + '\'' +
                ", content='" + content + '\'' +
                ", type=" + type +
                ", flag=" + flag +
                ", createTime='" + createTime + '\'' +
                ", errorDescr='" + errorDescr + '\'' +
                ", orderStatus=" + orderStatus +
                ", shopId='" + shopId + '\'' +
                ", shopName='" + shopName + '\'' +
                ", platformName='" + platformName + '\'' +
                ", platformOrderId='" + platformOrderId + '\'' +
                ", refundplatformOrderId='" + refundplatformOrderId + '\'' +
                ", refundTime='" + refundTime + '\'' +
                ", currencyRate=" + currencyRate +
                ", expressType='" + expressType + '\'' +
                ", logisticsChannelName='" + logisticsChannelName + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", countryCn='" + countryCn + '\'' +
                ", countryEn='" + countryEn + '\'' +
                ", salesRecordNumber='" + salesRecordNumber + '\'' +
                ", buyerUserId='" + buyerUserId + '\'' +
                ", buyerName='" + buyerName + '\'' +
                ", currencyId='" + currencyId + '\'' +
                ", itemTotalOrigin=" + itemTotalOrigin +
                ", shippingTotalOrigin=" + shippingTotalOrigin +
                ", paypalId='" + paypalId + '\'' +
                ", orderTime='" + orderTime + '\'' +
                ", expressTime='" + expressTime + '\'' +
                ", source=" + source +
                ", pictureUrl='" + pictureUrl + '\'' +
                ", updateTime='" + updateTime + '\'' +
                ", refundText='" + refundText + '\'' +
                ", complaintId='" + complaintId + '\'' +
                ", trackNumber='" + trackNumber + '\'' +
                ", productList=" + productList +
                '}';
    }
}
