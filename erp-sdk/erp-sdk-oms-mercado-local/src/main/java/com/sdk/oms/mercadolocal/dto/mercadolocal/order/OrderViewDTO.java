package com.sdk.oms.mercadolocal.dto.mercadolocal.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sdk.oms.mercadolocal.dto.mercadolocal.cost.CostDTO;
import com.sdk.oms.mercadolocal.dto.mercadolocal.shipment.ShipmentViewDTO;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderViewDTO {
    /**
     * payments : [{"reason":"Ulanzi J12 Lavalier Microfone Sem Fio Para iPhone iPad","status_code":null,"total_paid_amount":170,"operation_type":"regular_payment","transaction_amount":170,"transaction_amount_refunded":0,"date_approved":"2025-02-14T08:26:51.000-04:00","collector":{"id":2119968271},"coupon_id":null,"installments":1,"authorization_code":null,"taxes_amount":0,"id":102331168392,"date_last_modified":"2025-03-05T09:52:38.000-04:00","coupon_amount":0,"available_actions":["refund"],"shipping_cost":0,"installment_amount":null,"date_created":"2025-02-14T08:26:51.000-04:00","activation_uri":null,"overpaid_amount":0,"card_id":null,"status_detail":"accredited","issuer_id":"2007","payment_method_id":"account_money","payment_type":"account_money","deferred_period":null,"atm_transfer_reference":{"transaction_id":null,"company_id":null},"site_id":"MLB","payer_id":627939296,"order_id":2000010756759156,"currency_id":"BRL","status":"approved","transaction_order_id":null}]
     * fulfilled : true
     * taxes : {"amount":null,"currency_id":null,"id":null}
     * order_request : {"change":null,"return":null}
     * expiration_date : 2025-03-19T08:26:52.000-04:00
     * feedback : {"buyer":null,"seller":null}
     * shipping : {"id":44487360197}
     * date_closed : 2025-02-14T08:26:52.000-04:00
     * id : 2000010756759156
     * manufacturing_ending_date : 2025-02-19T08:26:52.000-04:00
     * order_items : [{"item":{"id":"MLB5283694678","title":"Ulanzi J12 Lavalier Microfone Sem Fio Para iPhone iPad","category_id":"MLB439409","variation_id":186902719495,"seller_custom_field":null,"global_price":null,"net_weight":null,"variation_attributes":[{"name":"Cor","id":"COLOR","value_id":"52049","value_name":"Preto"}],"warranty":"Garantia de fábrica: 90 dias","condition":"new","seller_sku":"2885"},"quantity":1,"unit_price":170,"full_unit_price":170,"currency_id":"BRL","manufacturing_days":5,"picked_quantity":null,"requested_quantity":{"measure":"unit","value":1},"sale_fee":22.1,"listing_type_id":"gold_special","base_exchange_rate":null,"base_currency_id":null,"bundle":null,"element_id":1}]
     * date_last_updated : 2025-03-05T13:52:45+00:00
     * last_updated : 2025-03-05T09:52:42.000-04:00
     * comment : null
     * pack_id : 2000007199681881
     * coupon : {"amount":0,"id":null}
     * shipping_cost : null
     * date_created : 2025-02-14T08:26:49.000-04:00
     * date_created_ttl : null
     * pickup_id : null
     * status_detail : null
     * tags : ["b2b","pack_order","paid","delivered"]
     * buyer : {"id":627939296,"nickname":"ATAKDEDETIZAOELIMPEZAATAK"}
     * seller : {"id":2119968271,"nickname":"CR20241127134854"}
     * total_amount : 170
     * paid_amount : 170
     * currency_id : BRL
     * status : paid
     * context : {"application":null,"product_id":null,"channel":"marketplace","site":"MLB","flows":["b2b"]}
     */

    @JsonProperty("fulfilled")
    private boolean fulfilled;
    @JsonProperty("taxes")
    private TaxesBean taxes;
    @JsonProperty("order_request")
    private OrderRequestBean orderRequest;
    @JsonProperty("expiration_date")
    private String expirationDate;
    @JsonProperty("feedback")
    private FeedbackBean feedback;
    @JsonProperty("shipping")
    private ShippingBean shipping;
    @JsonProperty("date_closed")
    private String dateClosed;
    @JsonProperty("id")
    private long fid;
    @JsonProperty("manufacturing_ending_date")
    private String manufacturingEndingDate;
    @JsonProperty("date_last_updated")
    private String dateLastUpdated;
    @JsonProperty("last_updated")
    private String lastUpdated;
    @JsonProperty("comment")
    private Object comment;
    @JsonProperty("pack_id")
    private long packId;
    @JsonProperty("coupon")
    private CouponBean coupon;
    @JsonProperty("shipping_cost")
    private Object shippingCost;
    @JsonProperty("date_created")
    private String dateCreated;
    @JsonProperty("date_created_ttl")
    private Object dateCreatedTtl;
    @JsonProperty("pickup_id")
    private Object pickupId;
    @JsonProperty("status_detail")
    private Object statusDetail;
    @JsonProperty("buyer")
    private BuyerBean buyer;
    @JsonProperty("seller")
    private SellerBean seller;
    @JsonProperty("total_amount")
    private int totalAmount;
    @JsonProperty("paid_amount")
    private int paidAmount;
    @JsonProperty("currency_id")
    private String currencyId;
    @JsonProperty("status")
    private String status;
    @JsonProperty("context")
    private ContextBean context;
    @JsonProperty("payments")
    private List<PaymentsBean> payments;
    @JsonProperty("order_items")
    private List<OrderItemsBean> orderItems;
    @JsonProperty("tags")
    private List<String> tags;
    @JsonProperty("shipmentViewDTO")
    private ShipmentViewDTO shipmentViewDTO;
    @JsonProperty("costDTO")
    private CostDTO costDTO;

    public boolean isFulfilled() {
        return fulfilled;
    }

    public void setFulfilled(boolean fulfilled) {
        this.fulfilled = fulfilled;
    }

    public TaxesBean getTaxes() {
        return taxes;
    }

    public void setTaxes(TaxesBean taxes) {
        this.taxes = taxes;
    }

    public OrderRequestBean getOrderRequest() {
        return orderRequest;
    }

    public void setOrderRequest(OrderRequestBean orderRequest) {
        this.orderRequest = orderRequest;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public FeedbackBean getFeedback() {
        return feedback;
    }

    public void setFeedback(FeedbackBean feedback) {
        this.feedback = feedback;
    }

    public ShippingBean getShipping() {
        return shipping;
    }

    public void setShipping(ShippingBean shipping) {
        this.shipping = shipping;
    }

    public String getDateClosed() {
        return dateClosed;
    }

    public void setDateClosed(String dateClosed) {
        this.dateClosed = dateClosed;
    }

    public long getFid() {
        return fid;
    }

    public void setFid(long fid) {
        this.fid = fid;
    }

    public String getManufacturingEndingDate() {
        return manufacturingEndingDate;
    }

    public void setManufacturingEndingDate(String manufacturingEndingDate) {
        this.manufacturingEndingDate = manufacturingEndingDate;
    }

    public String getDateLastUpdated() {
        return dateLastUpdated;
    }

    public void setDateLastUpdated(String dateLastUpdated) {
        this.dateLastUpdated = dateLastUpdated;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Object getComment() {
        return comment;
    }

    public void setComment(Object comment) {
        this.comment = comment;
    }

    public long getPackId() {
        return packId;
    }

    public void setPackId(long packId) {
        this.packId = packId;
    }

    public CouponBean getCoupon() {
        return coupon;
    }

    public void setCoupon(CouponBean coupon) {
        this.coupon = coupon;
    }

    public Object getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(Object shippingCost) {
        this.shippingCost = shippingCost;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Object getDateCreatedTtl() {
        return dateCreatedTtl;
    }

    public void setDateCreatedTtl(Object dateCreatedTtl) {
        this.dateCreatedTtl = dateCreatedTtl;
    }

    public Object getPickupId() {
        return pickupId;
    }

    public void setPickupId(Object pickupId) {
        this.pickupId = pickupId;
    }

    public Object getStatusDetail() {
        return statusDetail;
    }

    public void setStatusDetail(Object statusDetail) {
        this.statusDetail = statusDetail;
    }

    public BuyerBean getBuyer() {
        return buyer;
    }

    public void setBuyer(BuyerBean buyer) {
        this.buyer = buyer;
    }

    public SellerBean getSeller() {
        return seller;
    }

    public void setSeller(SellerBean seller) {
        this.seller = seller;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(int totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(int paidAmount) {
        this.paidAmount = paidAmount;
    }

    public String getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(String currencyId) {
        this.currencyId = currencyId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ContextBean getContext() {
        return context;
    }

    public void setContext(ContextBean context) {
        this.context = context;
    }

    public List<PaymentsBean> getPayments() {
        return payments;
    }

    public void setPayments(List<PaymentsBean> payments) {
        this.payments = payments;
    }

    public List<OrderItemsBean> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItemsBean> orderItems) {
        this.orderItems = orderItems;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public ShipmentViewDTO getShipmentViewDTO() {
        return shipmentViewDTO;
    }

    public void setShipmentViewDTO(ShipmentViewDTO shipmentViewDTO) {
        this.shipmentViewDTO = shipmentViewDTO;
    }

    public CostDTO getCostDTO() {
        return costDTO;
    }

    public void setCostDTO(CostDTO costDTO) {
        this.costDTO = costDTO;
    }
}
