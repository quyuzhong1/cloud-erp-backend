package com.sdk.oms.shopee.dto.order.response;

import cn.hutool.core.annotation.Alias;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class OrderDetail implements Serializable {

    public static final long serialVersionUID = 1L;

    /**
     * Return by default. Shopee's unique identifier for an order.
     */
    @Alias("order_sn")
    private String ordersn;

    /**
     * The two-digit code representing the country where the order was made.
     */
    @Alias("region")
    private String region;

    /**
     * The three-digit code representing the currency unit for which the order was paid.
     */
    @Alias("currency")
    private String currency;

    /**
     * This value indicates whether the order was a COD (cash on delivery) order.
     */
    @Alias("cod")
    private boolean cod;

    /**
     * The total amount paid by the buyer for the order. This amount includes the total sale price of items, shipping cost beared by buyer;
     * and offset by Shopee promotions if applicable.
     * This value will only return after the buyer has completed payment for the order.
     */
    @Alias("total_amount")
    private float totalAmount;

    /**
     * Enumerated type that defines the current status of the order.
     */
    @Alias("order_status")
    private String orderStatus;
    /**
     *
     * The list of pending terms, possible values: SYSTEM_PENDING for order under Shopee internal processing, KYC_PENDING for order under KYC checking(TW CB orders only)
     */
    @Alias("pending_terms")
    private List<String> pendingTerms;

    /**
     * The logistics META-INF provider that the buyer selected for the order to deliver items.
     */
    @Alias("shippingCarrier")
    private String shipping_carrier;

    /**
     * The payment method that the buyer selected to pay for the order.
     */
    @Alias("payment_method")
    private String paymentMethod;

    /**
     * The estimated shipping fee paid by buyer when placing order with selected logistics META-INF provider.
     */
    @Alias("estimated_shipping_fee")
    private float estimatedShippingFee;

    /**
     * Message to seller.
     */
    @Alias("message_to_seller")
    private String messageToSeller;

    /**
     * The time when discount activity start.
     */
    @Alias("create_time")
    private Long createTime;

    /**
     * The time when discount activity end. The end time must be 1 hour later than start time.
     */
    @Alias("update_time")
    private Long updateTime;

    /**
     * Shipping preparation time set by the seller when listing item on Shopee.
     */
    @Alias("days_to_ship")
    private Long daysToShip;

    @Alias("ship_by_date")
    private Long shipByDate;

    @Alias("buyer_user_id")
    private Long buyerUserId;

    @Alias("buyer_username")
    private String buyerUsername;

    @Alias("actual_shipping_fee")
    private float actualShippingFee;

    /**
     * Only work for cross-border order.This value indicates whether
     * the order contains goods that are required to declare at customs.
     */
    @Alias("goods_to_declare")
    private boolean goodsToDeclare;
    /**
     * The note seller made for own reference.
     */
    private String note;

    /**
     * Update time for the note.
     */
    @Alias("note_update_time")
    private Long noteUpdateTime;

    @Alias("pay_time")
    private Long payTime;

    /**
     * For Indonesia orders only. The name of the dropshipper.
     */
    @Alias("dropshipper")
    private String dropshipper;

    /**
     * The phone number of dropshipper, could be empty.
     */
    @Alias("dropshipper_phone")
    private String dropshipperPhone;

    @Alias("split_up")
    private boolean splitUp;
    /**
     * Cancel reason from buyer, could be empty.
     */
    @Alias("buyer_cancel_reason")
    private String buyerCancelReason;
    /**
     * Could be one of buyer, seller, system or Ops.
     */
    @Alias("cancel_by")
    private String cancelBy;
    /**
     * BACKEND_LOGISTICS_NOT_STARTED
     */
    @Alias("cancel_reason")
    private String cancelReason;

    @Alias("actual_shipping_fee_confirmed")
    private boolean actualShippingFeeConfirmed;
    @Alias("buyer_cpf_id")
    private String buyerCpfId;
    @Alias("fulfillment_flag")
    private String fulfillmentFlag;

    @Alias("pickup_done_time")
    private Long pickupDoneTime;
    @Alias("checkout_shipping_carrier")
    private String checkoutShippingCarrier;

    @Alias("reverse_shipping_fee")
    private float reverseShippingFee;
    @Alias("order_chargeable_weight_gram")
    private Integer orderChargeableWeightGram;

    @Alias("edt_from")
    private Long edtFrom;

    @Alias("edt_to")
    private Long edtTo;

    @Alias("prescription_images")
    private List<String> prescriptionImages;
    /**
     * Return prescription check status of this order enum OrderPrescriptionCheckStatus: NONE = 0; PASSED = 1; FAILED = 2; only for ID and PH whitelist user.
     */
    @Alias("prescription_check_status")
    private Integer prescriptionCheckStatus;

    /**
     * This object contains detailed breakdown for the recipient address. 收货人信息
     */
    @Alias("recipient_address")
    private RecipientAddress recipientAddress;

    /**
     * This object contains the detailed breakdown for all the items in this order,
     * including regular items(non-activity) and activity items.
     */
    @Alias("item_list")
    private List<OrderItemDetail> itemList;

    @Alias("package_list")
    private List<Package> packageList;

    @Alias("invoice_data")
    private JSONObject invoiceData;

}
