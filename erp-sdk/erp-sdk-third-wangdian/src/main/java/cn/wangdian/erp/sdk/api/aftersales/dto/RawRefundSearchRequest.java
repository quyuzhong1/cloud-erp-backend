package cn.wangdian.erp.sdk.api.aftersales.dto;

import com.google.gson.annotations.SerializedName;

public class RawRefundSearchRequest
{
	@SerializedName("start_time")
	private String startTime;

	@SerializedName("end_time")
	private String endTime;

	@SerializedName("time_type")
	private Integer timeType;

	@SerializedName("refund_no")
	private String refundNo;

	@SerializedName("platform_id")
	private Short platformId;

	@SerializedName("shop_no")
	private String shopNo;

	@SerializedName("tid")
	private String tid;

	@SerializedName("oid")
	private String oid;

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

	public Integer getTimeType()
	{
		return timeType;
	}

	public void setTimeType(Integer timeType)
	{
		this.timeType = timeType;
	}

	public String getRefundNo()
	{
		return refundNo;
	}

	public void setRefundNo(String refundNo)
	{
		this.refundNo = refundNo;
	}

	public Short getPlatformId()
	{
		return platformId;
	}

	public void setPlatformId(Short platformId)
	{
		this.platformId = platformId;
	}

	public String getShopNo()
	{
		return shopNo;
	}

	public void setShopNo(String shopNo)
	{
		this.shopNo = shopNo;
	}

	public String getTid()
	{
		return tid;
	}

	public void setTid(String tid)
	{
		this.tid = tid;
	}

	public String getOid()
	{
		return oid;
	}

	public void setOid(String oid)
	{
		this.oid = oid;
	}
}
