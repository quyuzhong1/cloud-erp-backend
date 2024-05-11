package cn.wangdian.erp.sdk.api.wms.dto;

public class TransferOrderSearchRequest
{
	private String startTime ;
	private String endTime ;
	private String fromWarehouseNo ;
	private String toWarehouseNo ;
	private String transferNo ;
	private String status ;

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

	public String getFromWarehouseNo()
	{
		return fromWarehouseNo;
	}

	public void setFromWarehouseNo(String fromWarehouseNo)
	{
		this.fromWarehouseNo = fromWarehouseNo;
	}

	public String getToWarehouseNo()
	{
		return toWarehouseNo;
	}

	public void setToWarehouseNo(String toWarehouseNo)
	{
		this.toWarehouseNo = toWarehouseNo;
	}

	public String getTransferNo()
	{
		return transferNo;
	}

	public void setTransferNo(String transferNo)
	{
		this.transferNo = transferNo;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}
}
