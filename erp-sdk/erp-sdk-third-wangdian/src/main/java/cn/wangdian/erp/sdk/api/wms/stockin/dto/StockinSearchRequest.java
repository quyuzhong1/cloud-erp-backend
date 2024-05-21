package cn.wangdian.erp.sdk.api.wms.stockin.dto;

public class StockinSearchRequest
{
	private String startTime ;
	private String endTime ;
	private Byte orderType ;
	private Byte status ;
	private String warehouseNo ;
	private String srcOrderNo ;
	private String stockinNo ;

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

	public Byte getOrderType()
	{
		return orderType;
	}

	public void setOrderType(Byte orderType)
	{
		this.orderType = orderType;
	}

	public Byte getStatus()
	{
		return status;
	}

	public void setStatus(Byte status)
	{
		this.status = status;
	}

	public String getWarehouseNo()
	{
		return warehouseNo;
	}

	public void setWarehouseNo(String warehouseNo)
	{
		this.warehouseNo = warehouseNo;
	}

	public String getSrcOrderNo()
	{
		return srcOrderNo;
	}

	public void setSrcOrderNo(String srcOrderNo)
	{
		this.srcOrderNo = srcOrderNo;
	}

	public String getStockinNo()
	{
		return stockinNo;
	}

	public void setStockinNo(String stockinNo)
	{
		this.stockinNo = stockinNo;
	}
}
