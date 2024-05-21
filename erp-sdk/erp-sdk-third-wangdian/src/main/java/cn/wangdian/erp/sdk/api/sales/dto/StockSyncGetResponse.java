package cn.wangdian.erp.sdk.api.sales.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class StockSyncGetResponse
{
	@SerializedName("id_list")
	private List<Integer> idList;

	@SerializedName("position")
	private Integer position;

	public List<Integer> getIdList()
	{
		return idList;
	}

	public void setIdList(List<Integer> idList)
	{
		this.idList = idList;
	}

	public Integer getPosition()
	{
		return position;
	}

	public void setPosition(Integer position)
	{
		this.position = position;
	}
}
