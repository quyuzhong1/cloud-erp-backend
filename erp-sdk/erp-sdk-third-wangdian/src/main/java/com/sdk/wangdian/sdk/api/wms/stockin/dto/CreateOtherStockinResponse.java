package com.sdk.wangdian.sdk.api.wms.stockin.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class CreateOtherStockinResponse
{
	/*
	 * { "status":0 "data": { "message":"CK2020072018" "status":20 } }
	 */
	private String message;
	private Integer status;

	@SerializedName("data")
	private DataDto data;

	@Data
	public static class DataDto
	{
		private String message;
		private Integer status;
	}

	@Override
	public String toString()
	{
		return "CreateOtherStockinResponse [message=" + message + ", status=" + status + ", data=" + data + "]";
	}

}
