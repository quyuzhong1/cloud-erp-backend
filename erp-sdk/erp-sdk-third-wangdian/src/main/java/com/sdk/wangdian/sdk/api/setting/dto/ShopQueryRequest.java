package com.sdk.wangdian.sdk.api.setting.dto;

import com.google.gson.annotations.SerializedName;

public class ShopQueryRequest
{
	private String shopNo ;
	@SerializedName("platform_id")
	private Short platformId ;

	public String getShopNo()
	{
		return shopNo;
	}

	public void setShopNo(String shopNo)
	{
		this.shopNo = shopNo;
	}

	public Short getPlatformId()
	{
		return platformId;
	}

	public void setPlatformId(Short platformId)
	{
		this.platformId = platformId;
	}
}
