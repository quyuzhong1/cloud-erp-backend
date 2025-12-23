package com.sdk.oms.shopify.api.rest.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ShopifyTrackingInfo {

	private String number;

	private List<String> numbers;
	private String url;
	private String company;

}
