package com.sdk.oms.tiktok.dto.tiktok.order.view;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrdersBean {
    /**
     * buyer_email : v2b2V5@chat.seller.tiktok.com
     * buyer_message : Please ship asap!
     * cancel_order_sla_time : 1619621355
     * cancel_reason : Pricing error
     * cancel_time : 1678389618
     * cancellation_initiator : SELLER
     * collection_due_time : 1678389618
     * collection_time : 1678389618
     * cpf : 3213-31231412
     * create_time : 1619611561
     * delivery_due_time : 1678389618
     * delivery_option_id : 7091146663229654785
     * delivery_option_name : Express shipping
     * delivery_option_required_delivery_time : 1678389618
     * delivery_sla_time : 1678389618
     * delivery_time : 1678389618
     * fulfillment_type : FULFILLMENT_BY_SELLER
     * has_updated_recipient_address : false
     * id : 576461413038785752
     * is_buyer_request_cancel : false
     * is_cod : false
     * is_on_hold_order : false
     * is_replacement_order : false
     * is_sample_order : false
     * line_items : [{"cancel_reason":"Discount not as expected","cancel_user":"BUYER","combined_listing_skus":[{"product_id":"1729582718312380456","sku_count":1,"sku_id":"2729382476852921123"}],"currency":"IDR","display_status":"TO_SHIP","id":"577086512123755123","is_gift":false,"item_tax":[{"tax_amount":"21.2","tax_rate":"0.35","tax_type":"SALES_TAX"}],"original_price":"0.01","package_id":"1153132168123859123","package_status":"TO_FULFILL","platform_discount":"0","product_id":"1729582718312380123","product_name":"Women's Winter Crochet Clothes","retail_delivery_fee":"1.28","rts_time":1678389618,"sale_price":"0.01","seller_discount":"0","seller_sku":"red_iphone_256","shipping_provider_id":"6617675021119438849","shipping_provider_name":"TT Virtual express","sku_id":"2729382476852921560","sku_image":"https://p16-oec-va.itexeitg.com/tos-maliva-d-o5syd03w52-us/46123e87d14f40b69b839","sku_name":"Iphone","sku_type":"PRE_ORDER","small_order_fee":"5000","tracking_number":"JX12345"}]
     * need_upload_invoice : NEED_INVOICE
     * packages : [{"id":"1152321127278713123"}]
     * paid_time : 1619611563
     * payment : {"currency":"IDR","original_shipping_fee":"5000","original_total_product_price":"5000","platform_discount":"5000","product_tax":"21.3","retail_delivery_fee":"1.28","seller_discount":"5000","shipping_fee":"5000","shipping_fee_platform_discount":"5000","shipping_fee_seller_discount":"5000","shipping_fee_tax":"11","small_order_fee":"3000","sub_total":"5000","tax":"5000","total_amount":"5000"}
     * payment_method_name : CCDC
     * recipient_address : {"address_detail":"Unit one building 8","address_line1":"TikTok 5800 bristol Pkwy","address_line2":"Suite 100","address_line3":"\"\"","address_line4":"\"\"","delivery_preferences":{"drop_off_location":"Front Door"},"district_info":[{"address_level":"L0","address_level_name":"Country","address_name":"United Kingdom"}],"full_address":"1199 Coleman Ave San Jose, CA 95110","name":"Zay","phone_number":"(+1)213-***-1234","postal_code":"95110","region_code":"US"}
     * replaced_order_id : 576461416728782174
     * request_cancel_time : 1678389618
     * rts_sla_time : 1619611688
     * rts_time : 1619611563
     * seller_note : seller note
     * shipping_due_time : 1678389618
     * shipping_provider : TT Virtual express
     * shipping_provider_id : 6617675021119438849
     * shipping_type : TIKTOK
     * split_or_combine_tag : COMBINED
     * status : UNPAID
     * tracking_number : JX12345
     * tts_sla_time : 1619611761
     * update_time : 1619621355
     * user_id : 7021436810468230477
     * warehouse_id : 6955005333819123123
     */

    @JsonProperty("buyer_email")
    private String buyerEmail;
    @JsonProperty("buyer_message")
    private String buyerMessage;
    @JsonProperty("cancel_order_sla_time")
    private int cancelOrderSlaTime;
    @JsonProperty("cancel_reason")
    private String cancelReason;
    @JsonProperty("cancel_time")
    private int cancelTime;
    @JsonProperty("cancellation_initiator")
    private String cancellationInitiator;
    @JsonProperty("collection_due_time")
    private int collectionDueTime;
    @JsonProperty("collection_time")
    private int collectionTime;
    @JsonProperty("cpf")
    private String cpf;
    @JsonProperty("create_time")
    private int createTime;
    @JsonProperty("delivery_due_time")
    private int deliveryDueTime;
    @JsonProperty("delivery_option_id")
    private String deliveryOptionId;
    @JsonProperty("delivery_option_name")
    private String deliveryOptionName;
    @JsonProperty("delivery_option_required_delivery_time")
    private int deliveryOptionRequiredDeliveryTime;
    @JsonProperty("delivery_sla_time")
    private int deliverySlaTime;
    @JsonProperty("delivery_time")
    private int deliveryTime;
    @JsonProperty("fulfillment_type")
    private String fulfillmentType;
    @JsonProperty("has_updated_recipient_address")
    private boolean hasUpdatedRecipientAddress;
    @JsonProperty("id")
    private String fid;
    @JsonProperty("is_buyer_request_cancel")
    private boolean isBuyerRequestCancel;
    @JsonProperty("is_cod")
    private boolean isCod;
    @JsonProperty("is_on_hold_order")
    private boolean isOnHoldOrder;
/*    @JsonProperty("is_replacement_order")
    private boolean isReplacementOrder;*/
    @JsonProperty("is_sample_order")
    private boolean isSampleOrder;
    @JsonProperty("need_upload_invoice")
    private String needUploadInvoice;
    @JsonProperty("paid_time")
    private int paidTime;
    @JsonProperty("payment")
    private PaymentBean payment;
    @JsonProperty("payment_method_name")
    private String paymentMethodName;
    @JsonProperty("recipient_address")
    private RecipientAddressBean recipientAddress;
    @JsonProperty("replaced_order_id")
    private String replacedOrderId;
    @JsonProperty("request_cancel_time")
    private int requestCancelTime;
    @JsonProperty("rts_sla_time")
    private int rtsSlaTime;
    @JsonProperty("rts_time")
    private int rtsTime;
    @JsonProperty("seller_note")
    private String sellerNote;
    @JsonProperty("shipping_due_time")
    private int shippingDueTime;
    @JsonProperty("shipping_provider")
    private String shippingProvider;
    @JsonProperty("shipping_provider_id")
    private String shippingProviderId;
    @JsonProperty("shipping_type")
    private String shippingType;
    @JsonProperty("split_or_combine_tag")
    private String splitOrCombineTag;
    @JsonProperty("status")
    private String status;
    @JsonProperty("tracking_number")
    private String trackingNumber;
    @JsonProperty("tts_sla_time")
    private int ttsSlaTime;
    @JsonProperty("update_time")
    private int updateTime;
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("warehouse_id")
    private String warehouseId;
    @JsonProperty("line_items")
    private List<LineItemsBean> lineItems;
    @JsonProperty("packages")
    private List<PackagesBean> packages;
}
