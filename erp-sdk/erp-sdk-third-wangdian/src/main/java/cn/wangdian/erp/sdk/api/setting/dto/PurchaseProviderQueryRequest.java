package cn.wangdian.erp.sdk.api.setting.dto;

import com.google.gson.annotations.SerializedName;

public class PurchaseProviderQueryRequest
{
	@SerializedName("provider_no")
	private String providerNo;

	@SerializedName("provider_name")
	private String providerName;

	@SerializedName("created_begin")
	private String createdBegin;

	@SerializedName("created_end")
	private String createdEnd;

	public String getProviderNo()
	{
		return providerNo;
	}

	public void setProviderNo(String providerNo)
	{
		this.providerNo = providerNo;
	}

	public String getProviderName()
	{
		return providerName;
	}

	public void setProviderName(String providerName)
	{
		this.providerName = providerName;
	}

	public String getCreatedBegin()
	{
		return createdBegin;
	}

	public void setCreatedBegin(String createdBegin)
	{
		this.createdBegin = createdBegin;
	}

	public String getCreatedEnd()
	{
		return createdEnd;
	}

	public void setCreatedEnd(String createdEnd)
	{
		this.createdEnd = createdEnd;
	}
}
