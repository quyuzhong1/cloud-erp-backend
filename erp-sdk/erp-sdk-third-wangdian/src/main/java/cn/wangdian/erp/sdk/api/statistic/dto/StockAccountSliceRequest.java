package cn.wangdian.erp.sdk.api.statistic.dto;

public class StockAccountSliceRequest
{
	private String date ;
	private String warehouseNos ;

	public String getDate()
	{
		return date;
	}

	public void setDate(String date)
	{
		this.date = date;
	}

	public String getWarehouseNos()
	{
		return warehouseNos;
	}

	public void setWarehouseNos(String warehouseNos)
	{
		this.warehouseNos = warehouseNos;
	}
}
