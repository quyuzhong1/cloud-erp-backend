package cn.wangdian.erp.sdk.api.goods.dto;

public class GoodsSearchRequest
{
	private String specNo;
	private String goodsNo;
	private String brandName;
	private String className;
	private String barcode;
	private String hideDeleted;
	private String startTime;
	private String endTime;
	private String specIds;

	public String getSpecNo()
	{
		return specNo;
	}

	public void setSpecNo(String specNo)
	{
		this.specNo = specNo;
	}

	public String getGoodsNo()
	{
		return goodsNo;
	}

	public void setGoodsNo(String goodsNo)
	{
		this.goodsNo = goodsNo;
	}

	public String getBrandName()
	{
		return brandName;
	}

	public void setBrandName(String brandName)
	{
		this.brandName = brandName;
	}

	public String getClassName()
	{
		return className;
	}

	public void setClassName(String className)
	{
		this.className = className;
	}

	public String getBarcode()
	{
		return barcode;
	}

	public void setBarcode(String barcode)
	{
		this.barcode = barcode;
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

	public String getHideDeleted()
	{
		return hideDeleted;
	}

	public void setHideDeleted(String hideDeleted)
	{
		this.hideDeleted = hideDeleted;
	}

	public String getSpecIds()
	{
		return specIds;
	}

	public void setSpecIds(String specIds)
	{
		this.specIds = specIds;
	}
}
