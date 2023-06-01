package com.erp.model.dmp.mabang;

import com.alibaba.fastjson.JSONObject;
import com.erp.model.dmp.mabang.item.OrderItemEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
public class OrderEntity {

    private String _id;
    private String platformOrderId;
    private String salesRecordNumber;
    private Integer orderStatus;
    private Integer myLogisticsChannelId;
    private String trackNumber;
    private String trackNumber1;
    private String trackNumber2;
    private String orderWeight;
    private String buyerUserId;
    private String buyerName;
    private String shopId;
    private String companyId;
    private String countryCode;
    private BigDecimal orderCost;
    private String codFlag;
    private String isUrgent;
    private String transportTime;
    private String quickPickTime;
    private Integer canSend;
    private String createDate;
    private Integer isReturned;
    private Integer isRefund;
    private String paidTime;

    private BigDecimal orderFee;
    private String platformId;
    private String expressTime;
    private Integer isUnion;
    private Integer isSplit;
    private Integer isResend;
    private Integer hasGoods;
    private String hasBattery;
    private String isSyncLogisticsDescr;
    private String paypalId;
    private String isSyncLogistics;
    private String isSyncPlatform;
    private String isSyncPlatformDescr;
    private String district;
    private String paypalEmail;
    private String closeDate;
    private String street1;
    private String street2;
    private String isVirtual;
    private String city;
    private String province;
    private String postCode;
    private String phone1;
    private String phone2;
    private String email;
    private String isNewOrder;
    private String doorcode;
    private Integer fbaFlag;
    private String fbaStartDateTime;
    private String fbaEndDateTime;
    private String CarrierCode;
    private String operTime;
    private String shippingService;
    private String packageWeight;
    private String platformOrderStatus;
    private String hasMagnetic;
    private String hasPowder;
    private String hasTort;
    private String remark;
    private String sellerMessage;
    private String currencyId;
    private BigDecimal currencyRate;
    private BigDecimal itemTotal;
    private BigDecimal shippingFee;
    private BigDecimal platformFee;
    private BigDecimal shippingTotalOrigin;
    private BigDecimal itemTotalOrigin;
    private String refundFeeOrigin;
    private String refundFeeCurrencyId;
    private String originFax;
    private String beforeStatus;
    private String otherExpend;
    private String otherIncome;
    private String insuranceFee;
    private String insuranceFeeOrigin;
    private String paypalFee;
    private String paypalFeeOrigin;
    private BigDecimal itemTotalCost;
    private String shippingCost;
    private String shippingPreCost;
    private String packageFee;
    private String fbaPerOrderFulfillmentFee;
    private String fbaCommission;
    private String promotionAmount;
    private String allianceFeeOrigin;
    private String voucherPriceOrigin;
    private String subsidyAmountOrigin;
    private String CODCharge;
    private String allianceFee;
    private String ShippingChargeback;
    private String fbaPerUnitFulfillmentFee;
    private String fbaWeightBasedFee;
    private String platformFeeOrigin;
    private String voucherPrice;
    private BigDecimal subsidyAmount;
    private String isWms;
    private String payType;
    private String VendorID;
    private String abnnumber;
    private String countryNameEN;
    private String countryNameCN;
    private String shopName;
    private String myLogisticsChannelName;
    private String myLogisticsId;
    private String myLogisticsName;
    private JSONObject orderTypeNew;
    private String erpOrderId;
    private String shippingWeight;
    private List<OrderItemEntity> orderItem;
    private JSONObject extendAttr;
    /**
     * 清洗数据
     */
    private Boolean isClean;
    /**
     * 清洗到发货订单
     */
    private Boolean cleanToDelivery;

    @Override
    public String toString() {
        return "OrderEntity{" +
                "platformOrderId='" + platformOrderId + '\'' +
                ", salesRecordNumber='" + salesRecordNumber + '\'' +
                ", orderStatus=" + orderStatus +
                ", myLogisticsChannelId=" + myLogisticsChannelId +
                ", trackNumber='" + trackNumber + '\'' +
                ", trackNumber1='" + trackNumber1 + '\'' +
                ", trackNumber2='" + trackNumber2 + '\'' +
                ", orderWeight='" + orderWeight + '\'' +
                ", buyerUserId='" + buyerUserId + '\'' +
                ", buyerName='" + buyerName + '\'' +
                ", shopId='" + shopId + '\'' +
                ", companyId='" + companyId + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", orderCost=" + orderCost +
                ", transportTime='" + transportTime + '\'' +
                ", quickPickTime='" + quickPickTime + '\'' +
                ", canSend=" + canSend +
                ", createDate='" + createDate + '\'' +
                ", isReturned=" + isReturned +
                ", isRefund=" + isRefund +
                ", paidTime='" + paidTime + '\'' +
                ", orderFee=" + orderFee +
                ", platformId='" + platformId + '\'' +
                ", expressTime='" + expressTime + '\'' +
                ", isUnion=" + isUnion +
                ", isSplit=" + isSplit +
                ", isResend=" + isResend +
                ", hasGoods=" + hasGoods +
                ", hasBattery='" + hasBattery + '\'' +
                ", isSyncLogisticsDescr='" + isSyncLogisticsDescr + '\'' +
                ", paypalId='" + paypalId + '\'' +
                ", isSyncLogistics='" + isSyncLogistics + '\'' +
                ", isSyncPlatform='" + isSyncPlatform + '\'' +
                ", isSyncPlatformDescr='" + isSyncPlatformDescr + '\'' +
                ", district='" + district + '\'' +
                ", paypalEmail='" + paypalEmail + '\'' +
                ", closeDate='" + closeDate + '\'' +
                ", street1='" + street1 + '\'' +
                ", street2='" + street2 + '\'' +
                ", isVirtual='" + isVirtual + '\'' +
                ", city='" + city + '\'' +
                ", province='" + province + '\'' +
                ", postCode='" + postCode + '\'' +
                ", phone1='" + phone1 + '\'' +
                ", phone2='" + phone2 + '\'' +
                ", email='" + email + '\'' +
                ", isNewOrder='" + isNewOrder + '\'' +
                ", doorcode='" + doorcode + '\'' +
                ", fbaFlag=" + fbaFlag +
                ", fbaStartDateTime='" + fbaStartDateTime + '\'' +
                ", fbaEndDateTime='" + fbaEndDateTime + '\'' +
                ", CarrierCode='" + CarrierCode + '\'' +
                ", operTime='" + operTime + '\'' +
                ", shippingService='" + shippingService + '\'' +
                ", packageWeight='" + packageWeight + '\'' +
                ", platformOrderStatus='" + platformOrderStatus + '\'' +
                ", hasMagnetic='" + hasMagnetic + '\'' +
                ", hasPowder='" + hasPowder + '\'' +
                ", hasTort='" + hasTort + '\'' +
                ", remark='" + remark + '\'' +
                ", sellerMessage='" + sellerMessage + '\'' +
                ", currencyId='" + currencyId + '\'' +
                ", currencyRate=" + currencyRate +
                ", itemTotal=" + itemTotal +
                ", shippingFee=" + shippingFee +
                ", platformFee=" + platformFee +
                ", shippingTotalOrigin=" + shippingTotalOrigin +
                ", itemTotalOrigin=" + itemTotalOrigin +
                ", refundFeeOrigin='" + refundFeeOrigin + '\'' +
                ", refundFeeCurrencyId='" + refundFeeCurrencyId + '\'' +
                ", originFax='" + originFax + '\'' +
                ", beforeStatus='" + beforeStatus + '\'' +
                ", otherExpend='" + otherExpend + '\'' +
                ", otherIncome='" + otherIncome + '\'' +
                ", insuranceFee='" + insuranceFee + '\'' +
                ", insuranceFeeOrigin='" + insuranceFeeOrigin + '\'' +
                ", paypalFee='" + paypalFee + '\'' +
                ", paypalFeeOrigin='" + paypalFeeOrigin + '\'' +
                ", itemTotalCost=" + itemTotalCost +
                ", shippingCost='" + shippingCost + '\'' +
                ", shippingPreCost='" + shippingPreCost + '\'' +
                ", packageFee='" + packageFee + '\'' +
                ", fbaPerOrderFulfillmentFee='" + fbaPerOrderFulfillmentFee + '\'' +
                ", fbaCommission='" + fbaCommission + '\'' +
                ", promotionAmount='" + promotionAmount + '\'' +
                ", allianceFeeOrigin='" + allianceFeeOrigin + '\'' +
                ", voucherPriceOrigin='" + voucherPriceOrigin + '\'' +
                ", subsidyAmountOrigin='" + subsidyAmountOrigin + '\'' +
                ", CODCharge='" + CODCharge + '\'' +
                ", allianceFee='" + allianceFee + '\'' +
                ", ShippingChargeback='" + ShippingChargeback + '\'' +
                ", fbaPerUnitFulfillmentFee='" + fbaPerUnitFulfillmentFee + '\'' +
                ", fbaWeightBasedFee='" + fbaWeightBasedFee + '\'' +
                ", platformFeeOrigin='" + platformFeeOrigin + '\'' +
                ", voucherPrice='" + voucherPrice + '\'' +
                ", subsidyAmount=" + subsidyAmount +
                ", isWms='" + isWms + '\'' +
                ", payType='" + payType + '\'' +
                ", VendorID='" + VendorID + '\'' +
                ", abnnumber='" + abnnumber + '\'' +
                ", countryNameEN='" + countryNameEN + '\'' +
                ", countryNameCN='" + countryNameCN + '\'' +
                ", shopName='" + shopName + '\'' +
                ", myLogisticsChannelName='" + myLogisticsChannelName + '\'' +
                ", myLogisticsId='" + myLogisticsId + '\'' +
                ", myLogisticsName='" + myLogisticsName + '\'' +
                ", orderTypeNew='" + orderTypeNew + '\'' +
                ", erpOrderId='" + erpOrderId + '\'' +
                ", shippingWeight='" + shippingWeight + '\'' +
                ", orderItem=" + orderItem +
                ", extendAttr='" + extendAttr + '\'' +
                '}';
    }
}
