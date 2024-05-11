package cn.wangdian.erp.sdk.api.setting.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ShopQueryResponse
{
	@SerializedName("total_count")
	private Integer total;
	@SerializedName("details")
	private List<ShopDto> shopDtoList ;

	private static class ShopDto
	{
		private Short shopId ;
		private String shopName ;
		private String shopNo ;
		private Short platformId ;
		private Byte subPlatformId ;
		private String contact ;
		private String province ;
		private String city ;
		private String district ;
		private String address ;
		private String telno ;
		private String mobile ;
		private String zip ;
		private String email ;
		private String remark ;
		private String prop1 ;
		private String prop2 ;
		private String website ;
		@SerializedName("is_disabled")
		private Boolean disabled ;
		private String groupId ;
		private Byte authState ;
		private String authTime ;
		private String reExpireTime ;
		private String modified ;
		private String expireTime ;
		private String created ;

		public Short getShopId()
		{
			return shopId;
		}

		public void setShopId(Short shopId)
		{
			this.shopId = shopId;
		}

		public String getShopName()
		{
			return shopName;
		}

		public void setShopName(String shopName)
		{
			this.shopName = shopName;
		}

		public String getShopNo()
		{
			return shopNo;
		}

		public void setShopNo(String shopNo)
		{
			this.shopNo = shopNo;
		}

		public Short getPlatformId()
		{
			return platformId;
		}

		public void setPlatformId(Short platformId)
		{
			this.platformId = platformId;
		}

		public Byte getSubPlatformId()
		{
			return subPlatformId;
		}

		public void setSubPlatformId(Byte subPlatformId)
		{
			this.subPlatformId = subPlatformId;
		}

		public String getContact()
		{
			return contact;
		}

		public void setContact(String contact)
		{
			this.contact = contact;
		}

		public String getProvince()
		{
			return province;
		}

		public void setProvince(String province)
		{
			this.province = province;
		}

		public String getCity()
		{
			return city;
		}

		public void setCity(String city)
		{
			this.city = city;
		}

		public String getDistrict()
		{
			return district;
		}

		public void setDistrict(String district)
		{
			this.district = district;
		}

		public String getAddress()
		{
			return address;
		}

		public void setAddress(String address)
		{
			this.address = address;
		}

		public String getTelno()
		{
			return telno;
		}

		public void setTelno(String telno)
		{
			this.telno = telno;
		}

		public String getMobile()
		{
			return mobile;
		}

		public void setMobile(String mobile)
		{
			this.mobile = mobile;
		}

		public String getZip()
		{
			return zip;
		}

		public void setZip(String zip)
		{
			this.zip = zip;
		}

		public String getEmail()
		{
			return email;
		}

		public void setEmail(String email)
		{
			this.email = email;
		}

		public String getRemark()
		{
			return remark;
		}

		public void setRemark(String remark)
		{
			this.remark = remark;
		}

		public String getProp1()
		{
			return prop1;
		}

		public void setProp1(String prop1)
		{
			this.prop1 = prop1;
		}

		public String getProp2()
		{
			return prop2;
		}

		public void setProp2(String prop2)
		{
			this.prop2 = prop2;
		}

		public String getWebsite()
		{
			return website;
		}

		public void setWebsite(String website)
		{
			this.website = website;
		}

		public Boolean getDisabled()
		{
			return disabled;
		}

		public void setDisabled(Boolean disabled)
		{
			this.disabled = disabled;
		}

		public String getGroupId()
		{
			return groupId;
		}

		public void setGroupId(String groupId)
		{
			this.groupId = groupId;
		}

		public Byte getAuthState()
		{
			return authState;
		}

		public void setAuthState(Byte authState)
		{
			this.authState = authState;
		}

		public String getAuthTime()
		{
			return authTime;
		}

		public void setAuthTime(String authTime)
		{
			this.authTime = authTime;
		}

		public String getReExpireTime()
		{
			return reExpireTime;
		}

		public void setReExpireTime(String reExpireTime)
		{
			this.reExpireTime = reExpireTime;
		}

		public String getModified()
		{
			return modified;
		}

		public void setModified(String modified)
		{
			this.modified = modified;
		}

		public String getExpireTime()
		{
			return expireTime;
		}

		public void setExpireTime(String expireTime)
		{
			this.expireTime = expireTime;
		}

		public String getCreated()
		{
			return created;
		}

		public void setCreated(String created)
		{
			this.created = created;
		}
	}

	public Integer getTotal()
	{
		return total;
	}

	public void setTotal(Integer total)
	{
		this.total = total;
	}

	public List<ShopDto> getShopDtoList()
	{
		return shopDtoList;
	}

	public void setShopDtoList(List<ShopDto> shopDtoList)
	{
		this.shopDtoList = shopDtoList;
	}
}
