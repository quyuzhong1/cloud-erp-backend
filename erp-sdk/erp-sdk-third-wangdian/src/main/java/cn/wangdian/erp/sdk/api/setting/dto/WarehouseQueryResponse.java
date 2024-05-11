package cn.wangdian.erp.sdk.api.setting.dto;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

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

	public static class WarehouseDto
	{

		private String zip;
		private String address;
		private String city;
		private String mobile;
		private String remark;
		private Byte type;
		private Byte subType;
		private String telno;
		private String province;
		private String warehouseNo;
		private String district;
		private String contact;
		private String name;
		private String modified;

		public String getZip()
		{
			return zip;
		}

		public void setZip(String zip)
		{
			this.zip = zip;
		}

		public String getAddress()
		{
			return address;
		}

		public void setAddress(String address)
		{
			this.address = address;
		}

		public String getCity()
		{
			return city;
		}

		public void setCity(String city)
		{
			this.city = city;
		}

		public String getMobile()
		{
			return mobile;
		}

		public void setMobile(String mobile)
		{
			this.mobile = mobile;
		}

		public String getRemark()
		{
			return remark;
		}

		public void setRemark(String remark)
		{
			this.remark = remark;
		}

		public Byte getType()
		{
			return type;
		}

		public void setType(Byte type)
		{
			this.type = type;
		}

		public Byte getSubType()
		{
			return subType;
		}

		public void setSubType(Byte subType)
		{
			this.subType = subType;
		}

		public String getTelno()
		{
			return telno;
		}

		public void setTelno(String telno)
		{
			this.telno = telno;
		}

		public String getProvince()
		{
			return province;
		}

		public void setProvince(String province)
		{
			this.province = province;
		}

		public String getWarehouseNo()
		{
			return warehouseNo;
		}

		public void setWarehouseNo(String warehouseNo)
		{
			this.warehouseNo = warehouseNo;
		}

		public String getDistrict()
		{
			return district;
		}

		public void setDistrict(String district)
		{
			this.district = district;
		}

		public String getContact()
		{
			return contact;
		}

		public void setContact(String contact)
		{
			this.contact = contact;
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public String getModified()
		{
			return new Date(Long.valueOf(modified)).toString();
		}

		public void setModified(String modified)
		{
			this.modified = modified;
		}
	}
}
