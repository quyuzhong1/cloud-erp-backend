package cn.wangdian.erp.sdk.api.wms.stockspec.dto;

public class AvailableStockQueryRequest
{
	private String startTime ;
	private String endTime ;
	private String specNo ;
	private String goodsNo ;
	private String warehouseNo ;

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
