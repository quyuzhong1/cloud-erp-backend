package cn.wangdian.erp.sdk.api.purchasereturn.dto;

import com.google.gson.annotations.SerializedName;

public class PurchaseReturnQueryRequest
{
	@SerializedName("provider_no")
	private String providerNo;

	@SerializedName("return_no")
	private String returnNo;

	@SerializedName("status")
	private Byte status;

	@SerializedName("start_time")
	private String startTime;

	@SerializedName("end_time")
	private String endTime;

	public String getProviderNo()
	{
		return providerNo;
	}

	public void setProviderNo(String providerNo)
	{
		this.providerNo = providerNo;
	}

	public String getReturnNo()
	{
		return returnNo;
	}

	public void setReturnNo(String returnNo)
	{
		this.returnNo = returnNo;
	}

	public Byte getStatus()
	{
		return status;
	}

	public void setStatus(Byte status)
	{
		this.status = status;
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
