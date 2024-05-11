package cn.wangdian.erp.sdk.api.wms.stockother.in.dto;

import com.google.gson.annotations.SerializedName;

public class StockOtherInQueryRequest
{
	@SerializedName("time_type") private Integer timeType;
	@SerializedName("start_time") private String startTime;
	@SerializedName("end_time") private String endTime;
	@SerializedName("warehouse_no") private String warehouseNo;
	@SerializedName("other_in_no") private String otherInNo;
	@SerializedName("status") private Integer status;
	@SerializedName("fuzzy_query") private Boolean fuzzyQuery;

	public Integer getTimeType()
	{
		return timeType;
	}

	public void setTimeType(Integer timeType)
	{
		this.timeType = timeType;
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

	public String getWarehouseNo()
	{
		return warehouseNo;
	}

	public void setWarehouseNo(String warehouseNo)
	{
		this.warehouseNo = warehouseNo;
	}

	public String getOtherInNo()
	{
		return otherInNo;
	}

	public void setOtherInNo(String otherInNo)
	{
		this.otherInNo = otherInNo;
	}

	public Integer getStatus()
	{
		return status;
	}

	public void setStatus(Integer status)
	{
		this.status = status;
	}

	public Boolean getFuzzyQuery()
	{
		return fuzzyQuery;
	}

	public void setFuzzyQuery(Boolean fuzzyQuery)
	{
		this.fuzzyQuery = fuzzyQuery;
	}
}
