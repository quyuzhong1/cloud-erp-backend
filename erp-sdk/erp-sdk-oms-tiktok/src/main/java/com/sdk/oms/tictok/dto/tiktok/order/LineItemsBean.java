package com.sdk.oms.tictok.dto.tiktok.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LineItemsBean {
    /**
     * cancel_reason : Discount not as expected
     * cancel_user : BUYER
     * currency : IDR
     * display_status : TO_SHIP
     * id : 577086512123755123
     * is_gift : false
     * item_tax : [{"tax_amount":"21.2","tax_rate":"0.35","tax_type":"SALES_TAX"}]
     * original_price : 0.01
     * package_id : 1153132168123859123
     * package_status : TO_FULFILL
     * platform_discount : 0
     * product_id : 1729582718312380123
     * product_name : Women's Winter Crochet Clothes
     * retail_delivery_fee : 1.28
     * rts_time : 1678389618
     * sale_price : 0.01
     * seller_discount : 0
     * seller_sku : red_iphone_256
     * shipping_provider_id : 6617675021119438849
     * shipping_provider_name : TT Virtual express
     * sku_id : 2729382476852921560
     * sku_image : https://p16-oec-va.itexeitg.com/tos-maliva-d-o5syd03w52-us/46123e87d14f40b69b839
     * sku_name : Iphone
     * sku_type : PRE_ORDER
     * small_order_fee : 5000
     * tracking_number : JX12345
     */

    @JsonProperty("cancel_reason")
    private String cancelReason;
    @JsonProperty("cancel_user")
    private String cancelUser;
    @JsonProperty("currency")
    private String currency;
    @JsonProperty("display_status")
    private String displayStatus;
    @JsonProperty("id")
    private String fid;
    @JsonProperty("is_gift")
    private boolean isGift;
    @JsonProperty("original_price")
    private String originalPrice;
    @JsonProperty("package_id")
    private String packageId;
    @JsonProperty("package_status")
    private String packageStatus;
    @JsonProperty("platform_discount")
    private String platformDiscount;
    @JsonProperty("product_id")
    private String productId;
    @JsonProperty("product_name")
    private String productName;
    @JsonProperty("retail_delivery_fee")
    private String retailDeliveryFee;
    @JsonProperty("rts_time")
    private int rtsTime;
    @JsonProperty("sale_price")
    private String salePrice;
    @JsonProperty("seller_discount")
    private String sellerDiscount;
    @JsonProperty("seller_sku")
    private String sellerSku;
    @JsonProperty("shipping_provider_id")
    private String shippingProviderId;
    @JsonProperty("shipping_provider_name")
    private String shippingProviderName;
    @JsonProperty("sku_id")
    private String skuId;
    @JsonProperty("sku_image")
    private String skuImage;
    @JsonProperty("sku_name")
    private String skuName;
    @JsonProperty("sku_type")
    private String skuType;
    @JsonProperty("small_order_fee")
    private String smallOrderFee;
    @JsonProperty("tracking_number")
    private String trackingNumber;
    @JsonProperty("item_tax")
    private List<ItemTaxBean> itemTax;

}
