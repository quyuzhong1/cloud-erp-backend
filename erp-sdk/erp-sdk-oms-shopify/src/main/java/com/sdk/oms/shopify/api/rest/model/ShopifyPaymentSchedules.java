package com.sdk.oms.shopify.api.rest.model;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sdk.oms.shopify.api.rest.model.serializer.LocalDateTimeDeserializer;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@Data
@NoArgsConstructor
public class ShopifyPaymentSchedules {

	private String amount;
	private String currency;
	@JsonProperty("issued_at")
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private LocalDateTime issuedAt;
	@JsonProperty("due_at")
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private LocalDateTime dueAt;
	@JsonProperty("completed_at")
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private LocalDateTime completedAt;
	@JsonProperty("expected_payment_method")
	private String expectedPaymentMethod;

	@JsonAnySetter
	public void ignored(String name, Object value) {
		log.debug("ShopifyRestAPI Ignored Property: {} = {}", name, value);
	}
}
