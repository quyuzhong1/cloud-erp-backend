package cn.wangdian.erp.sdk.api.sales.dto;

import com.google.gson.annotations.SerializedName;

public class StockOutQueryRequest
{
	@SerializedName("start_time")
	private String startTime;
	@SerializedName("end_time")
	private String endTime;
	@SerializedName("status_type")
	private String statusType;
	@SerializedName("status")
	private Integer status;
	@SerializedName("warehouse_no")
	private String warehouseNo;
	@SerializedName("stockout_no")
	private String stockoutNo;
	@SerializedName("shop_nos")
	private String shopNos;
	@SerializedName("src_order_no")
	private String srcOrderNo;
	@SerializedName("need_sn")
	private Integer needSn;
	@SerializedName("position")
	private Integer position;

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

	public String getStatusType()
	{
		return statusType;
	}

	public void setStatusType(String statusType)
	{
		this.statusType = statusType;
	}

	public Integer getStatus()
	{
		return status;
	}

	public void setStatus(Integer status)
	{
		this.status = status;
	}

	public String getWarehouseNo()
	{
		return warehouseNo;
	}

	public void setWarehouseNo(String warehouseNo)
	{
		this.warehouseNo = warehouseNo;
	}

	public String getStockoutNo()
	{
		return stockoutNo;
	}

	public void setStockoutNo(String stockoutNo)
	{
		this.stockoutNo = stockoutNo;
	}

	public String getShopNos()
	{
		return shopNos;
	}

	public void setShopNos(String shopNos)
	{
		this.shopNos = shopNos;
	}

	public String getSrcOrderNo()
	{
		return srcOrderNo;
	}

	public void setSrcOrderNo(String srcOrderNo)
	{
		this.srcOrderNo = srcOrderNo;
	}

	public Integer getNeedSn()
	{
		return needSn;
	}

	public void setNeedSn(Integer needSn)
	{
		this.needSn = needSn;
	}

	public Integer getPosition()
	{
		return position;
	}

	public void setPosition(Integer position)
	{
		this.position = position;
	}
}
