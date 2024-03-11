package com.erp.sdk.oms.amz.spapi.dto;

import com.common.business.dto.CleanBaseDTO;
import com.common.core.anno.Panno;
import com.common.core.enums.PannoEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


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

    private String merchantOrderId;

    private String shipmentId;

    private String shipmentItemId;

    private String amazonOrderItemId;

    private String merchantOrderItemId;

    private String purchaseDate;

    private String paymentsDate;

    private String shipmentDate;

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
}
