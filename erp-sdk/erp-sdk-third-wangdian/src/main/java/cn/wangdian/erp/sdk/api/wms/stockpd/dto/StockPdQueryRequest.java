package cn.wangdian.erp.sdk.api.wms.stockpd.dto;

public class StockPdQueryRequest
{
	private String startTime;
	private String endTime;
	private String specNo;
	private String goodsNo;
	private String pdNo;
	private String employeeNo;
	private String warehouseNo;
	private String status;

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public String getPdNo()
	{
		return pdNo;
	}

	public void setPdNo(String pdNo)
	{
		this.pdNo = pdNo;
	}

	public String getEmployeeNo()
	{
		return employeeNo;
	}

	public void setEmployeeNo(String employeeNo)
	{
		this.employeeNo = employeeNo;
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

	public String getWarehouseNo()
	{
		return warehouseNo;
	}

	public void setWarehouseNo(String warehouseNo)
	{
		this.warehouseNo = warehouseNo;
	}
}
