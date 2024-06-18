package com.sdk.wangdian.sdk.api.setting.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;
import java.util.Objects;


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
		private String virtual_warehouse_id;
		private Boolean is_disabled;
		private String created;
		private List<WarehouseListDto> warehouse_list;
	}
	@Data
	public static class WarehouseListDto
	{

		private String virtual_warehouse_id;
		private String warehouse_no;
		private String sys_warehouse_id;
		private Integer is_start_up;
	}
}
