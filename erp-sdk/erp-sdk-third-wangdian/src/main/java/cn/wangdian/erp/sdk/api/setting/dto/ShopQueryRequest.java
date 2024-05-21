package cn.wangdian.erp.sdk.api.setting.dto;

public class ShopQueryRequest
{
	private String shopNo ;
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
