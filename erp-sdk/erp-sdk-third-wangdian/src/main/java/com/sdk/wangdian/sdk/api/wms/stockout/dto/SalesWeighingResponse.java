package com.sdk.wangdian.sdk.api.wms.stockout.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class SalesWeighingResponse {


	@SerializedName( "logistics_name")
	private String logisticsName;
	@SerializedName( "sys_logistics_name")
	private String sysLogisticsName;
	@SerializedName( "province")
	private Integer province;
	@SerializedName( "city")
	private Integer city;
	@SerializedName( "logistics_no")
	private String logisticsNo;
	@SerializedName( "district")
	private Integer district;
	@SerializedName( "calc_weight")
	private Double calcWeight;
}
