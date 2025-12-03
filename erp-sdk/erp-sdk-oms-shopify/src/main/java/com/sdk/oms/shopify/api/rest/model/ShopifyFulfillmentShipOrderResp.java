package com.sdk.oms.shopify.api.rest.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class ShopifyFulfillmentShipOrderResp {

	@JSONField(name = "data")
	private DataDTO data;

	@NoArgsConstructor
	@Data
	public static class DataDTO {
		@JSONField(name = "fulfillmentCreate")
		private FulfillmentCreateDTO fulfillmentCreate;

		@NoArgsConstructor
		@Data
		public static class FulfillmentCreateDTO {
			@JSONField(name = "fulfillment")
			private FulfillmentDTO fulfillment;
			@JSONField(name = "userErrors")
			private List<UserErrorsDTO> userErrors;

			@NoArgsConstructor
			@Data
			public static class FulfillmentDTO {
				@JSONField(name = "id")
				private String id;
				@JSONField(name = "status")
				private String status;
			}

			@NoArgsConstructor
			@Data
			public static class UserErrorsDTO {
				@JSONField(name = "field")
				private List<String> field;
				@JSONField(name = "message")
				private String message;
			}
		}
	}
}
