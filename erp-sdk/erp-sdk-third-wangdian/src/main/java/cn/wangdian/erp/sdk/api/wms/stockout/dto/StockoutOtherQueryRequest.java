package cn.wangdian.erp.sdk.api.wms.stockout.dto;

import com.google.gson.annotations.SerializedName;

public class StockoutOtherQueryRequest
{
	public static final Byte TIME_TYPE_CONSIGN = 1;
	public static final Byte TIME_TYPE_CREATED = 2;
	public static final Byte TIME_TYPE_MODIFIED = 3;

	@SerializedName("time_type")
	private Byte timeType;

	@SerializedName("start_time")
	private String startTime;

	@SerializedName("end_time")
	private String endTime;

	@SerializedName("warehouse_no")
	private String warehouseNo;

	@SerializedName("src_order_no")
	private String srcOrderNo;

	@SerializedName("stockout_no")
	private String stockoutNo;

	@SerializedName("status")
	private Short status;

	@SerializedName("position")
	private Boolean position;

	public Byte getTimeType()
	{
		return timeType;
	}

	public void setTimeType(Byte timeType)
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

	public String getSrcOrderNo()
	{
		return srcOrderNo;
	}

	public void setSrcOrderNo(String srcOrderNo)
	{
		this.srcOrderNo = srcOrderNo;
	}

	public String getStockoutNo()
	{
		return stockoutNo;
	}

	public void setStockoutNo(String stockoutNo)
	{
		this.stockoutNo = stockoutNo;
	}

	public Short getStatus()
	{
		return status;
	}

	public void setStatus(Short status)
	{
		this.status = status;
	}

	public Boolean getPosition()
	{
		return position;
	}

	public void setPosition(Boolean position)
	{
		this.position = position;
	}
}
