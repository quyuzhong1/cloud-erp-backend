package cn.wangdian.erp.sdk.api.sales.dto;

import com.google.gson.annotations.SerializedName;

public class LogisticsSyncGetRequest
{

	@SerializedName("shop_no")
	private String shopNo;
	@SerializedName("is_own_platform")
	private Boolean ownPlatform;
	@SerializedName("tid")
	private String tid;
	@SerializedName("logistics_no")
	private String logisticsNo;
	@SerializedName("is_part_sync")
	private Boolean partSync;

	public String getShopNo()
	{
		return shopNo;
	}

	public void setShopNo(String shopNo)
	{
		this.shopNo = shopNo;
	}

	public Boolean getOwnPlatform()
	{
		return ownPlatform;
	}

	public void setOwnPlatform(Boolean ownPlatform)
	{
		this.ownPlatform = ownPlatform;
	}

	public String getTid()
	{
		return tid;
	}

	public void setTid(String tid)
	{
		this.tid = tid;
	}

	public String getLogisticsNo()
	{
		return logisticsNo;
	}

	public void setLogisticsNo(String logisticsNo)
	{
		this.logisticsNo = logisticsNo;
	}

	public Boolean getPartSync()
	{
		return partSync;
	}

	public void setPartSync(Boolean partSync)
	{
		this.partSync = partSync;
	}
}
