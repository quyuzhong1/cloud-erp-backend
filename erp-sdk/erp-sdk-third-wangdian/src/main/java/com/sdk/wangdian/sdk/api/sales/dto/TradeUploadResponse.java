package com.sdk.wangdian.sdk.api.sales.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class TradeUploadResponse {

	@JSONField(name = "status")
	private Integer status;
	@JSONField(name = "message")
	private String message;
}
