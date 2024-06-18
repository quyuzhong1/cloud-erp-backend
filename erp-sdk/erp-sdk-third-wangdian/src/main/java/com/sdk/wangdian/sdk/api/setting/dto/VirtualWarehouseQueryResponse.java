package com.sdk.wangdian.sdk.api.setting.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;


@Data
public class VirtualWarehouseQueryResponse
{

	@SerializedName("total_count")
	private Integer total;
	@SerializedName("detail_list")
	private List<VirtualWarehouseDto> virtualWarehouseList;

	public Integer getTotal()
	{
		return total;
	}

	public void setTotal(Integer total)
	{
		this.total = total;
	}

	public List<VirtualWarehouseDto> getVirtualWarehouseList()
	{
		return virtualWarehouseList;
	}

	public void setVirtualWarehouseList(List<VirtualWarehouseDto> warehouseList)
	{
		this.virtualWarehouseList = warehouseList;
	}

	@Data
	public static class VirtualWarehouseDto
	{

		private String remark;
		private Byte warehouse_type;
		private String virtual_warehouse_no;
		private String virtual_warehouse_name;
		private String modified;
		private Integer warehouse_id;
		private Boolean is_disabled;
		private String created;
	}
}
