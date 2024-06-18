package com.erp.sdk.oms.amz.spapi.dto;

import com.common.business.dto.CleanBaseDTO;
import com.common.core.anno.Panno;
import com.common.core.enums.PannoEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.time.OffsetDateTime;
import java.time.ZoneId;


/**
 * 亚马逊物流销售报告实体mongo实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PlatformAmazonFulfilledShipmentsDTO extends CleanBaseDTO {

    /**
     * 处理状态：
     * -1=无需处理(已有订单直接处理)
     * 0=待下载订单(检查订单下载处理)
     * 1=待处理销售出库单(订单已下载处理)
     * 2=已处理
     */
    @Panno(findType = PannoEnum.EQ,field = "handleStatus")
    private String handleStatus;

    @Panno(findType = PannoEnum.EQ,field = "amazonOrderId")
    private String amazonOrderId;

    @Panno(findType = PannoEnum.EQ,field = "shopId")
    private String shopId;

    /**
     * 分组ID:{订单ID}_{单据日期}_{店铺ID}
     */
    @Panno(findType = PannoEnum.EQ,field = "groupId")
    private String groupId;

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
    /**
     * 亚马逊账号代号
     */
    @Panno(findType = PannoEnum.EQ, field = "platformShopCode")
    private String platformShopCode;


    public void checkAndSetAllDateLocale(String timeZone) {
        if (StringUtils.isBlank(timeZone)){
            return;
        }
        if (StringUtils.isNotBlank(this.shipmentDate)){
            OffsetDateTime parseDate = OffsetDateTime.parse(this.shipmentDate);
            this.setShipmentDateLocale(parseDate.atZoneSameInstant(ZoneId.of(timeZone)).toString());
        }
        if (StringUtils.isNotBlank(this.paymentsDate)){
            OffsetDateTime parseDate = OffsetDateTime.parse(this.paymentsDate);
            this.setPaymentsDateLocale(parseDate.atZoneSameInstant(ZoneId.of(timeZone)).toString());
        }
        if (StringUtils.isNotBlank(this.purchaseDate)){
            OffsetDateTime parseDate = OffsetDateTime.parse(this.purchaseDate);
            this.setPurchaseDateLocale(parseDate.atZoneSameInstant(ZoneId.of(timeZone)).toString());
        }
    }
}
