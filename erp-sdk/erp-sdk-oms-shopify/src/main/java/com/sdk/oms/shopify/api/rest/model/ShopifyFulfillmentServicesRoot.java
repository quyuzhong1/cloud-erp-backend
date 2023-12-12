package com.sdk.oms.shopify.api.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ShopifyFulfillmentServicesRoot {

	@JsonProperty("fulfillment_services")
	private List<ShopifyFulfillmentServicesItem> fulfillmentServices;
}
