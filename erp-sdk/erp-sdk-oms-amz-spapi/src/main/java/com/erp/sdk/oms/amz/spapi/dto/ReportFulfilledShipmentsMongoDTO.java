package com.erp.sdk.oms.amz.spapi.dto;

import com.common.core.anno.Panno;
import com.common.core.enums.PannoEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Transient;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;


/**
 * 亚马逊物流销售报告实体mongo实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ReportFulfilledShipmentsMongoDTO extends ReportSuperMongoDTO {

    private String amazonReportId;

    private String amazonOrderId;

    private String merchantOrderId;

    private String shipmentId;

    private String shipmentItemId;

    private String amazonOrderItemId;

    private String merchantOrderItemId;

    private String purchaseDate;

    private String purchaseDateLocale;

    private String paymentsDate;

    private String paymentsDateLocale;

    private String shipmentDate;

    private String shipmentDateLocale;

    private String reportingDate;

    private String buyerEmail;

    private String buyerName;

    private String buyerPhoneNumber;

    private String productName;

    private String quantityShipped;

    private String itemPrice;

    private String itemTax;

    private String shippingPrice;

    private String shippingTax;

    private String giftWrapPrice;

    private String giftWrapTax;

    private String shipServiceLevel;

    private String recipientName;

    private String shipAddress1;

    private String shipAddress2;

    private String shipAddress3;

    private String shipCity;

    private String shipState;

    private String shipPostalCode;

    private String shipCountry;

    private String shipPhoneNumber;

    private String billAddress1;

    private String billAddress2;

    private String billAddress3;

    private String billCity;

    private String billState;

    private String billPostalCode;

    private String billCountry;

    private String itemPromotionDiscount;

    private String shipPromotionDiscount;

    private String trackingNumber;

    private String estimatedArrivalDate;

    private String fulfillmentCenterId;

    private String fulfillmentChannel;

    private String salesChannel;

    private String pointsGranted;

    /**
     * 亚马逊账号代号
     */
    @Panno(findType = PannoEnum.EQ, field = "platformShopCode")
    private String platformShopCode;


    /**
     * 中转参数:不保存mongo
     */
    @Transient
    private String shopId;

    @Transient
    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public String convertShipmentDate(){
        if (StringUtils.isBlank(this.shipmentDateLocale)){
            return "";
        }
        return OffsetDateTime.parse(this.shipmentDate).toLocalDate().toString();
    }

    public void checkAndSetAllDateLocale(Integer utfDiffHour) {
        if (null == utfDiffHour){
            return;
        }
        if (StringUtils.isNotBlank(this.shipmentDate)){
            OffsetDateTime parseDate = OffsetDateTime.parse(this.shipmentDate);
            this.setShipmentDateLocale(parseDate.withOffsetSameInstant(ZoneOffset.ofHours(utfDiffHour)).toString());
        }
        if (StringUtils.isNotBlank(this.paymentsDate)){
            OffsetDateTime parseDate = OffsetDateTime.parse(this.paymentsDate);
            this.setPaymentsDateLocale(parseDate.withOffsetSameInstant(ZoneOffset.ofHours(utfDiffHour)).toString());
        }
        if (StringUtils.isNotBlank(this.purchaseDate)){
            OffsetDateTime parseDate = OffsetDateTime.parse(this.purchaseDate);
            this.setPurchaseDateLocale(parseDate.withOffsetSameInstant(ZoneOffset.ofHours(utfDiffHour)).toString());
        }
    }
}
