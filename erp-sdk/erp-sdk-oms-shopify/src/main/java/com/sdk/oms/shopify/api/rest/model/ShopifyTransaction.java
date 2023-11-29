package com.sdk.oms.shopify.api.rest.model;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.sdk.oms.shopify.api.rest.model.serializer.CurrencyDeserializer;
import com.sdk.oms.shopify.api.rest.model.serializer.CurrencySerializer;
import com.sdk.oms.shopify.api.rest.model.serializer.LocalDateTimeDeserializer;
import com.sdk.oms.shopify.api.rest.model.serializer.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

@Slf4j
@Data
@NoArgsConstructor
public class ShopifyTransaction {

	private String id;
	@JsonProperty("order_id")
	private String orderId;
	private String kind;
	private String gateway;
	private String status;
	private String message;
	@JsonProperty("created_at")
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private LocalDateTime createdAt;
	private boolean test;
	private String authorization;
	@JsonProperty("parent_id")
	private String parentId;
	private BigDecimal amount;
	@JsonSerialize(using = CurrencySerializer.class)
	@JsonDeserialize(using = CurrencyDeserializer.class)
	private Currency currency;
	@JsonProperty("maximum_refundable")
	private BigDecimal maximumRefundable;
	private ShopifyTransactionReceipt receipt;
	@JsonProperty("processed_at")
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private LocalDateTime processedAt;
	@JsonProperty("device_id")
	private Long deviceId;
	@JsonProperty("error_code")
	private String errorCode;
	@JsonProperty("source_name")
	private String sourceName;
	@JsonProperty("payment_details")
	private PaymentDetails paymentDetails;
	@JsonProperty("currency_exchange_adjustment")
	private String currencyExchangeAdjustment;
	@JsonProperty("payment_id")
	private String paymentId;
	@JsonProperty("total_unsettled_set")
	private TotalUnsettledSet totalUnsettledSet;
	@JsonProperty("admin_graphql_api_id")
	private String adminGraphqlApiId;

	@Data
	@NoArgsConstructor
	public static class PaymentDetails {
		private String credit_card_bin;
		private String avs_result_code;
		private String cvv_result_code;
		private String credit_card_number;
		private String credit_card_company;
		private String buyer_action_info;
		private String credit_card_name;
		private String credit_card_wallet;
		private String credit_card_expiration_month;
		private String credit_card_expiration_year;
		private String payment_method_name;

		// getters and setters
	}

	@Data
	@NoArgsConstructor
	public static class TotalUnsettledSet {
		private Money presentment_money;
		private Money shop_money;

		// getters and setters
	}

	@Data
	@NoArgsConstructor
	public static class Money {
		private String amount;
		private String currency;

	}

	/**
	 * @param name
	 * @param value
	 */
	@JsonAnySetter
	public void ignored(String name, Object value) {
		log.debug("ShopifyRestAPI Ignored Property: {} = {}", name, value);
	}
}
