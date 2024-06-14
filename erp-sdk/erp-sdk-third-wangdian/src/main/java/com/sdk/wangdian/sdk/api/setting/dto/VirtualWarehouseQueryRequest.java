package com.sdk.wangdian.sdk.api.setting.dto;

import com.google.gson.annotations.SerializedName;

public class VirtualWarehouseQueryRequest
{

	private String warehouseNo;
	private String warehouseName;
	@SerializedName("start_time")
	private String startTime;
	@SerializedName("end_time")
	private String endTime;

	public String getWarehouseNo()
	{
		return warehouseNo;
	}

	public void setWarehouseNo(String warehouseNo)
	{
		this.warehouseNo = warehouseNo;
	}

	public String getWarehouseName()
	{
		return warehouseName;
	}

	public void setWarehouseName(String warehouseName)
	{
		this.warehouseName = warehouseName;
	}

	public String getStartTime()
	{
		return startTime;
	}

	public void setStartTime(String startTime)
	{
		this.startTime = startTime;
	}

	public String getEndTime()
	{
		return endTime;
	}

	public void setEndTime(String endTime)
	{
		this.endTime = endTime;
	}
}
