package com.sdk.oms.shopify.api.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ShopifyUpdateFulfillmentPayload {

	private String message;
	@JsonProperty("notify_customer")
	private boolean notifyCustomer;
	@JsonProperty("tracking_info")
	private ShopifyTrackingInfo trackingInfo;
}
