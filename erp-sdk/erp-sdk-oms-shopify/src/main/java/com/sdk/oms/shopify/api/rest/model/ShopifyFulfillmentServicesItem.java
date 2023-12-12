package com.sdk.oms.shopify.api.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ShopifyFulfillmentServicesItem {

	private String id;
	private String name;
	private String email;
	@JsonProperty("service_name")
	private String serviceName;
	@JsonProperty("handle")
	private String handle;
	@JsonProperty("fulfillment_orders_opt_in")
	private Boolean fulfillmentOrdersOptIn;
	@JsonProperty("include_pending_stock")
	private Boolean includePendingStock;
	@JsonProperty("provider_id")
	private String providerId;
	@JsonProperty("location_id")
	private String locationId;
	@JsonProperty("callback_url")
	private String callbackUrl;
	@JsonProperty("tracking_support")
	private Boolean trackingSupport;
	@JsonProperty("inventory_management")
	private Boolean inventoryManagement;
	@JsonProperty("admin_graphql_api_id")
	private String adminGraphqlApiId;
	@JsonProperty("permits_sku_sharing")
	private Boolean permitsSkuSharing;


}
