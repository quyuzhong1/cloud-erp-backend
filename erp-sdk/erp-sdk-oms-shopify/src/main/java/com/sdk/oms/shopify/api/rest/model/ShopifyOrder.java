package com.sdk.oms.shopify.api.rest.model;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.sdk.oms.shopify.api.rest.model.serializer.CurrencyDeserializer;
import com.sdk.oms.shopify.api.rest.model.serializer.CurrencySerializer;
import com.sdk.oms.shopify.api.rest.model.serializer.LocalDateTimeDeserializer;
import com.sdk.oms.shopify.api.rest.model.serializer.LocalDateTimeSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Data
@NoArgsConstructor
public class ShopifyOrder {

    private String id;
    private String email;
    @JsonProperty("closed_at")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime closedAt;
    @JsonProperty("created_at")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createdAt;
    @JsonProperty("updated_at")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime updatedAt;
    private int number;
    private String note;
    private String token;
    @JsonProperty("total_price")
    private BigDecimal totalPrice;
    @JsonProperty("subtotal_price")
    private BigDecimal subtotalPrice;
    @JsonProperty("total_weight")
    private long totalWeight;
    @JsonProperty("total_tax")
    private BigDecimal totalTax;
    @JsonProperty("taxes_included")
    private boolean taxesIncluded;
    @JsonSerialize(using = CurrencySerializer.class)
    @JsonDeserialize(using = CurrencyDeserializer.class)
    private Currency currency;
    @JsonProperty("financial_status")
    private String financialStatus;
    @JsonProperty("total_discounts")
    private BigDecimal totalDiscounts;
    @JsonProperty("total_line_items_price")
    private BigDecimal totaLineItemsPrice;
    @JsonProperty("cart_token")
    private String cartToken;
    @JsonProperty("buyer_accepts_marketing")
    private boolean buyerAcceptsMarketing;
    private String name;
    @JsonProperty("referring_site")
    private String referringSite;
    @JsonProperty("landing_site")
    private String landingSite;
    @JsonProperty("cancelled_at")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime cancelledAt;
    @JsonProperty("cancel_reason")
    private String cancelReason;
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("location_id")
    private String locationId;
    @JsonProperty("processed_at")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime processedAt;
    @JsonProperty("browser_ip")
    private String browserIp;
    @JsonProperty("order_number")
    private String orderNumber;
    @JsonProperty("processing_method")
    private String processingMethod;
    @JsonProperty("source_name")
    private String sourceName;
    @JsonProperty("fulfillment_status")
    private String fulfillmentStatus;
    @JsonProperty("tags")
    private String tags;
    @JsonProperty("order_status_url")
    private String orderStatusUrl;
    @JsonProperty("line_items")
    private List<ShopifyLineItem> lineItems = new LinkedList<>();
    private List<ShopifyFulfillment> fulfillments = new LinkedList<>();
    @JsonProperty("billing_address")
    private ShopifyAddress billingAddress = new ShopifyAddress();
    @JsonProperty("shipping_address")
    private ShopifyAddress shippingAddress = new ShopifyAddress();
    private ShopifyCustomer customer = new ShopifyCustomer();
    @JsonProperty("shipping_lines")
    private List<ShopifyShippingLine> shippingLines = new LinkedList<>();
    @JsonProperty("tax_lines")
    private List<ShopifyTaxLine> taxLines = new LinkedList<>();
    @JsonProperty("note_attributes")
    private List<ShopifyAttribute> noteAttributes = new LinkedList<>();
    private List<ShopifyRefund> refunds = new LinkedList<>();
    private List<Metafield> metafields = new LinkedList<>();


    /**
     * @param name
     * @param value
     */
    @JsonAnySetter
    public void ignored(String name, Object value) {
        log.debug("ShopifyRestAPI Ignored Property: {} = {}", name, value);
    }
}
