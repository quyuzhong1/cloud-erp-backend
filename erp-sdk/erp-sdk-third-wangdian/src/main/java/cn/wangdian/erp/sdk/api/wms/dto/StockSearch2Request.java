package cn.wangdian.erp.sdk.api.wms.dto;

import java.util.List;

public class StockSearch2Request
{
	private String startTime;
	private String endTime;
	private List<String> specNos;
	private String warehouseNo;
	private Integer mask;

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

	public List<String> getSpecNos()
	{
		return specNos;
	}

	public void setSpecNos(List<String> specNos)
	{
		this.specNos = specNos;
	}

	public String getWarehouseNo()
	{
		return warehouseNo;
	}

	public void setWarehouseNo(String warehouseNo)
	{
		this.warehouseNo = warehouseNo;
	}

	public Integer getMask()
	{
		return mask;
	}

	public void setMask(Integer mask)
	{
		this.mask = mask;
	}
}
