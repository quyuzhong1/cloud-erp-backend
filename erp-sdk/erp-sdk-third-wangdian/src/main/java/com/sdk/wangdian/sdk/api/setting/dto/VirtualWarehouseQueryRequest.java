package com.sdk.wangdian.sdk.api.setting.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class VirtualWarehouseQueryRequest
{

	private String warehouseNo;
	private String warehouseName;
	@SerializedName("start_time")
	private String startTime;
	@SerializedName("end_time")
	private String endTime;

}
