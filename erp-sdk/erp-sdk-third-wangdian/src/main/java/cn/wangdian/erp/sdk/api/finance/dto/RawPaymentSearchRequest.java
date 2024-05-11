package cn.wangdian.erp.sdk.api.finance.dto;

public class RawPaymentSearchRequest
{
	private String shopNo;
	private String orderNo;
	private String startTime;
	private String endTime;

	public String getShopNo()
	{
		return shopNo;
	}

	public void setShopNo(String shopNo)
	{
		this.shopNo = shopNo;
	}

	public String getOrderNo()
	{
		return orderNo;
	}

	public void setOrderNo(String orderNo)
	{
		this.orderNo = orderNo;
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

	@Override public String toString()
	{
		return "RawPaymentSearchRequest{" + "shopNo='" + shopNo + '\'' + ", orderNo='" + orderNo + '\'' + ", startTime='"
				+ startTime + '\'' + ", endTime='" + endTime + '\'' + '}';
	}
}
