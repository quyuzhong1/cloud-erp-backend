package com.sdk.wangdian.sdk.api.setting.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.Date;
import java.util.List;


@Data
public class WarehouseQueryResponse
{

	/*
	 * { "status": 0, "data": { "total_count": 1, "details": [{ "zip": "100010",
	 * "address": "高碑店新村东区D21-9", "city": "北京市", "mobile": "01065572100",
	 * "remark": "仓库备注", "type": 1, "telno": "010-1234567", "province": "北京",
	 * "warehouse_no": "1001", "sub_type": 0, "district": "朝阳区", "contact":
	 * "仓库联系人", "name": "LJ测试仓", "modified": 1569826740000 }] } }
	 */
	@SerializedName("total_count")
	private Integer total;
	@SerializedName("details")
	private List<WarehouseDto> warehouseList;

	public Integer getTotal()
	{
		return total;
	}

	public void setTotal(Integer total)
	{
		this.total = total;
	}

	public List<WarehouseDto> getWarehouseList()
	{
		return warehouseList;
	}

	public void setWarehouseList(List<WarehouseDto> warehouseList)
	{
		this.warehouseList = warehouseList;
	}

	@Data
	public static class WarehouseDto
	{

		private String zip;
		private String address;
		private String city;
		private String mobile;
		private String remark;
		private Byte type;
		private Byte sub_type;
		private String telno;
		private String province;
		private String warehouse_no;
		private String district;
		private String contact;
		private String name;
		private String modified;
		private Integer warehouse_id;
		private Boolean is_disabled;
		private String created;
	}
}
