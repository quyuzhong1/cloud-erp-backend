package com.sdk.oms.mercado.dto.mercado.order;

import com.google.gson.annotations.SerializedName;
import com.sdk.oms.mercado.dto.mercado.shipment.ShipmentViewDTO;

import java.util.List;

public class OrderViewDTO {

    /**
     * id : 2000007633674134
     * date_created : 2024-02-18T22:28:18.000-04:00
     * date_closed : 2024-02-18T22:28:19.000-04:00
     * last_updated : 2024-03-01T12:35:04.000-04:00
     * manufacturing_ending_date : null
     * feedback : {"sale":null,"purchase":null}
     * mediations : []
     * comments : null
     * pack_id : null
     * pickup_id : null
     * order_request : {"return":null,"change":null}
     * fulfilled : true
     * paid_amount : 21.2
     * coupon : {"id":null,"amount":36.27}
     * expiration_date : 2024-05-28T22:28:19.000-04:00
     * order_items : [{"item":{"id":"MLM2802963514","title":"Ulanzi R099 Kit De Montaje Con Clip Para Cámara Gopro","category_id":"MLM127873","variation_id":null,"seller_custom_field":null,"variation_attributes":[],"warranty":"Garantía del vendedor: 1 meses","condition":"new","seller_sku":"2993+1764A+0605","parent_item_id":"CBT1908713864"},"quantity":1,"unit_price":21.2,"full_unit_price":21.2,"currency_id":"USD","manufacturing_days":null,"sale_fee":3.18,"base_exchange_rate":17.11}]
     * currency_id : USD
     * payments : [{"id":72480387497,"order_id":2000007633674134,"payer_id":139133205,"collector":{"id":1511265855},"card_id":0,"site_id":"MLM","reason":"Ulanzi R099 Kit De Montaje Con Clip Para Cámara Gopro","payment_method_id":"consumer_credits","currency_id":"USD","installments":1,"issuer_id":"","atm_transfer_reference":{"company_id":null,"transaction_id":"611672965"},"coupon_id":null,"activation_uri":null,"operation_type":"regular_payment","payment_type":"digital_currency","available_actions":["refund"],"status":"approved","status_code":null,"status_detail":"accredited","transaction_amount":21.2,"taxes_amount":0,"shipping_cost":0,"coupon_amount":2.12,"overpaid_amount":0,"total_paid_amount":19.08,"installment_amount":0,"deferred_period":null,"date_approved":"2024-02-18T22:28:19.000-04:00","authorization_code":"","transaction_order_id":null,"date_created":"2024-02-18T22:28:19.000-04:00","date_last_modified":"2024-03-01T12:34:02.000-04:00"}]
     * shipping : {"id":43116658829}
     * status : paid
     * buyer : {"id":139133205,"nickname":"WILBERTALONZO","last_name":"ALONZO","first_name":"WILBERT"}
     * seller : {"id":1511265855}
     * taxes : {"amount":0,"currency_id":"USD"}
     * context : {"channel":"marketplace","site":"MLM","flows":["cbt"],"application":"buyingflow-api"}
     */

    @SerializedName("id")
    private long id;
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getDateClosed() {
        return dateClosed;
    }

    public void setDateClosed(String dateClosed) {
        this.dateClosed = dateClosed;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Object getManufacturingEndingDate() {
        return manufacturingEndingDate;
    }

    public void setManufacturingEndingDate(Object manufacturingEndingDate) {
        this.manufacturingEndingDate = manufacturingEndingDate;
    }

    public FeedbackBeanX getFeedback() {
        return feedback;
    }

    public void setFeedback(FeedbackBeanX feedback) {
        this.feedback = feedback;
    }

    public Object getComments() {
        return comments;
    }

    public void setComments(Object comments) {
        this.comments = comments;
    }

    public Object getPackId() {
        return packId;
    }

    public void setPackId(Object packId) {
        this.packId = packId;
    }

    public Object getPickupId() {
        return pickupId;
    }

    public void setPickupId(Object pickupId) {
        this.pickupId = pickupId;
    }

    public OrderRequestBean getOrderRequest() {
        return orderRequest;
    }

    public void setOrderRequest(OrderRequestBean orderRequest) {
        this.orderRequest = orderRequest;
    }

    public boolean isFulfilled() {
        return fulfilled;
    }

    public void setFulfilled(boolean fulfilled) {
        this.fulfilled = fulfilled;
    }

    public double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public CouponBean getCoupon() {
        return coupon;
    }

    public void setCoupon(CouponBean coupon) {
        this.coupon = coupon;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(String currencyId) {
        this.currencyId = currencyId;
    }

    public ShippingBean getShipping() {
        return shipping;
    }

    public void setShipping(ShippingBean shipping) {
        this.shipping = shipping;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BuyerBeanX getBuyer() {
        return buyer;
    }

    public void setBuyer(BuyerBeanX buyer) {
        this.buyer = buyer;
    }

    public BuyerBean getSeller() {
        return seller;
    }

    public void setSeller(BuyerBean seller) {
        this.seller = seller;
    }

    public TaxesBean getTaxes() {
        return taxes;
    }

    public void setTaxes(TaxesBean taxes) {
        this.taxes = taxes;
    }

    public ContextBean getContext() {
        return context;
    }

    public void setContext(ContextBean context) {
        this.context = context;
    }

    public List<?> getMediations() {
        return mediations;
    }

    public void setMediations(List<?> mediations) {
        this.mediations = mediations;
    }

    public List<OrderItemsBean> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItemsBean> orderItems) {
        this.orderItems = orderItems;
    }

    public List<PaymentsBeanX> getPayments() {
        return payments;
    }

    public void setPayments(List<PaymentsBeanX> payments) {
        this.payments = payments;
    }

    public ShipmentViewDTO getShipmentViewDTO() {
        return shipmentViewDTO;
    }

    public void setShipmentViewDTO(ShipmentViewDTO shipmentViewDTO) {
        this.shipmentViewDTO = shipmentViewDTO;
    }
}
