package com.sdk.wangdian.sdk.api.sales.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class TradeUploadRequest {

	@JSONField(name = "logistics_name")
	private String logisticsName;
	@JSONField(name = "logistics_no")
	private String logisticsNo;
	@JSONField(name = "merchant_no")
	private String merchantNo;
	@JSONField(name = "num")
	private Integer num;
	@JSONField(name = "receivable")
	private Double receivable;
	@JSONField(name = "receiver_address")
	private String receiverAddress;
	@JSONField(name = "receiver_mobile")
	private String receiverMobile;
	@JSONField(name = "shop_name")
	private String shopName;
	@JSONField(name = "trade_no")
	private String tradeNo;
	@JSONField(name = "trade_time")
	private String tradeTime;
	@JSONField(name = "warehouse_name")
	private String warehouseName;
}
