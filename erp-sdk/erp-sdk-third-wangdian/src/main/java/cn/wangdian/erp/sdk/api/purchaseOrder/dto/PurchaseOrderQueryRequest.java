package cn.wangdian.erp.sdk.api.purchaseOrder.dto;

import com.google.gson.annotations.SerializedName;

public class PurchaseOrderQueryRequest
{

	@SerializedName("purchase_no")
	private String purchaseNo;
	@SerializedName("provider_no")
	private String providerNo;
	@SerializedName("expect_warehouse_no")
	private String exceptWarehouseNo;
	@SerializedName("receive_warehouse_no")
	private String receiverWarehouseNo;
	@SerializedName("start_time")
	private String startTime;
	@SerializedName("end_time")
	private String endTime;
	@SerializedName("stockin_status")
	private String stockinStatus;
	@SerializedName("status")
	private String status;

	public String getPurchaseNo()
	{
		return purchaseNo;
	}

	public void setPurchaseNo(String purchaseNo)
	{
		this.purchaseNo = purchaseNo;
	}

	public String getProviderNo()
	{
		return providerNo;
	}

	public void setProviderNo(String providerNo)
	{
		this.providerNo = providerNo;
	}

	public String getExceptWarehouseNo()
	{
		return exceptWarehouseNo;
	}

	public void setExceptWarehouseNo(String exceptWarehouseNo)
	{
		this.exceptWarehouseNo = exceptWarehouseNo;
	}

	public String getReceiverWarehouseNo()
	{
		return receiverWarehouseNo;
	}

	public void setReceiverWarehouseNo(String receiverWarehouseNo)
	{
		this.receiverWarehouseNo = receiverWarehouseNo;
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

	public String getStockinStatus()
	{
		return stockinStatus;
	}

	public void setStockinStatus(String stockinStatus)
	{
		this.stockinStatus = stockinStatus;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}
}
