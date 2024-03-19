package com.sdk.oms.mercado.dto.mercado.order;

import com.google.gson.annotations.SerializedName;
import com.sdk.oms.mercado.dto.mercado.shipment.ShipmentViewDTO;
import lombok.Data;

import java.util.List;

@Data
public class OrderViewDTO {

    @SerializedName("id")
    private long fid;
    @SerializedName("date_created")
    private String dateCreated;
    @SerializedName("date_closed")
    private String dateClosed;
    @SerializedName("last_updated")
    private String lastUpdated;
    @SerializedName("manufacturing_ending_date")
    private Object manufacturingEndingDate;
    @SerializedName("feedback")
    private FeedbackBeanX feedback;
    @SerializedName("comments")
    private Object comments;
    @SerializedName("pack_id")
    private Object packId;
    @SerializedName("pickup_id")
    private Object pickupId;
    @SerializedName("order_request")
    private OrderRequestBean orderRequest;
    @SerializedName("fulfilled")
    private boolean fulfilled;
    @SerializedName("paid_amount")
    private double paidAmount;
    @SerializedName("coupon")
    private CouponBean coupon;
    @SerializedName("expiration_date")
    private String expirationDate;
    @SerializedName("currency_id")
    private String currencyId;
    @SerializedName("shipping")
    private ShippingBean shipping;
    @SerializedName("status")
    private String status;
    @SerializedName("buyer")
    private BuyerBeanX buyer;
    @SerializedName("seller")
    private BuyerBean seller;
    @SerializedName("taxes")
    private TaxesBean taxes;
    @SerializedName("context")
    private ContextBean context;
    @SerializedName("mediations")
    private List<?> mediations;
    @SerializedName("order_items")
    private List<OrderItemsBean> orderItems;
    @SerializedName("payments")
    private List<PaymentsBeanX> payments;
    @SerializedName("shipmentViewDTO")
    private ShipmentViewDTO shipmentViewDTO;

}
