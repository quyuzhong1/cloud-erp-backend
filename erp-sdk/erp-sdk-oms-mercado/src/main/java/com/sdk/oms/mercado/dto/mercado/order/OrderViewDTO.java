package com.sdk.oms.mercado.dto.mercado.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sdk.oms.mercado.dto.mercado.shipment.ShipmentViewDTO;
import lombok.Data;

import java.util.List;

@Data
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

    @JsonProperty("id")
    private long fid;
    @JsonProperty("date_created")
    private String dateCreated;
    @JsonProperty("date_closed")
    private String dateClosed;
    @JsonProperty("last_updated")
    private String lastUpdated;
    @JsonProperty("manufacturing_ending_date")
    private Object manufacturingEndingDate;
    @JsonProperty("feedback")
    private FeedbackBeanX feedback;
    @JsonProperty("comments")
    private Object comments;
    @JsonProperty("pack_id")
    private Object packId;
    @JsonProperty("pickup_id")
    private Object pickupId;
    @JsonProperty("order_request")
    private OrderRequestBean orderRequest;
    @JsonProperty("fulfilled")
    private boolean fulfilled;
    @JsonProperty("paid_amount")
    private double paidAmount;
    @JsonProperty("coupon")
    private CouponBean coupon;
    @JsonProperty("expiration_date")
    private String expirationDate;
    @JsonProperty("currency_id")
    private String currencyId;
    @JsonProperty("shipping")
    private ShippingBean shipping;
    @JsonProperty("status")
    private String status;
    @JsonProperty("buyer")
    private BuyerBeanX buyer;
    @JsonProperty("seller")
    private BuyerBean seller;
    @JsonProperty("taxes")
    private TaxesBean taxes;
    @JsonProperty("context")
    private ContextBean context;
    private List<?> mediations;
    @JsonProperty("order_items")
    private List<OrderItemsBean> orderItems;
    @JsonProperty("payments")
    private List<PaymentsBeanX> payments;
    @JsonProperty("shipmentViewDTO")
    private ShipmentViewDTO shipmentViewDTO;

}
