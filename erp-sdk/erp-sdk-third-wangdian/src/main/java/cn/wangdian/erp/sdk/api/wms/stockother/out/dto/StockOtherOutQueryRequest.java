package cn.wangdian.erp.sdk.api.wms.stockother.out.dto;

import com.google.gson.annotations.SerializedName;

public class StockOtherOutQueryRequest
{
	@SerializedName("start_time") private String startTime;
	@SerializedName("end_time") private String endTime;
	@SerializedName("time_type") private Integer timeType;
	@SerializedName("warehouse_no") private String warehouseNo;
	@SerializedName("other_out_no") private String otherOutNo;
	@SerializedName("status") private Integer status;
	@SerializedName("fuzzy_query") private Boolean fuzzyQuery;

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

	public Integer getTimeType()
	{
		return timeType;
	}

	public void setTimeType(Integer timeType)
	{
		this.timeType = timeType;
	}

	public String getWarehouseNo()
	{
		return warehouseNo;
	}

	public void setWarehouseNo(String warehouseNo)
	{
		this.warehouseNo = warehouseNo;
	}

	public String getOtherOutNo()
	{
		return otherOutNo;
	}

	public void setOtherOutNo(String otherOutNo)
	{
		this.otherOutNo = otherOutNo;
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
