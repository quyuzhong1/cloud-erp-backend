package com.sdk.oms.shopify.api.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ShopifyAssignedLocation {

	private String address1;
	private String address2;
	private String city;
	@JsonProperty("country_code")
	private String countryCode;
	@JsonProperty("location_id")
	private String locationId;
	private String name;
	private String phone;
	private String province;
	private String zip;
}
