package cn.wangdian.erp.sdk.api.goods.dto;

import com.google.gson.annotations.SerializedName;

public class GoodsSuiteSearchRequest
{

	@SerializedName("spec_no")
	private String specNo;
	@SerializedName("goods_no")
	private String goodsNo;
	@SerializedName("brand_name")
	private String brandName;
	@SerializedName("class_name")
	private String className;
	@SerializedName("barcode")
	private String barcode;
	@SerializedName("suite_no")
	private String suiteNo;
	@SerializedName("hide_deleted")
	private Boolean hideDeleted;
	@SerializedName("start_time")
	private String startTime;
	@SerializedName("end_time")
	private String endTime;

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

	public String getSuiteNo()
	{
		return suiteNo;
	}

	public void setSuiteNo(String suiteNo)
	{
		this.suiteNo = suiteNo;
	}

	public Boolean getHideDeleted()
	{
		return hideDeleted;
	}

	public void setHideDeleted(Boolean hideDeleted)
	{
		this.hideDeleted = hideDeleted;
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
