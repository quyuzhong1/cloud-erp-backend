package cn.wangdian.erp.sdk.api.goods.dto;

public class GoodsBrandSearchRequest
{
	private String startTime;
	private String endTime;
	private String brandNo;
	private String brandName;

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

	public String getBrandNo()
	{
		return brandNo;
	}

	public void setBrandNo(String brandNo)
	{
		this.brandNo = brandNo;
	}

	public String getBrandName()
	{
		return brandName;
	}

	public void setBrandName(String brandName)
	{
		this.brandName = brandName;
	}
}
