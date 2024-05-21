package cn.wangdian.erp.sdk.api.setting.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PurchaseProviderQueryResponse
{
	@SerializedName("total_count")
	private Integer totalCount;

	@SerializedName("details")
	private List<DetailItem> detailList;

	public static class DetailItem{

		@SerializedName("zip")
		private String zip;

		@SerializedName("qq")
		private String qq;

		@SerializedName("website")
		private String website;

		@SerializedName("address")
		private String address;

		@SerializedName("last_purchase_time")
		private Long lastPurchaseTime;

		@SerializedName("wangwang")
		private String wangwang;

		@SerializedName("is_disabled")
		private Boolean disabled;

		@SerializedName("mobile")
		private String mobile;

		@SerializedName("arrive_cycle_days")
		private Integer arriveCycleDays;

		@SerializedName("remark")
		private String remark;

		@SerializedName("telno")
		private String telno;

		@SerializedName("deleted")
		private Integer deleted;

		@SerializedName("contact")
		private String contact;

		@SerializedName("follower_name")
		private String followerName;

		@SerializedName("provider_name")
		private String providerName;

		@SerializedName("fax")
		private String fax;

		@SerializedName("email")
		private String email;

		@SerializedName("provider_no")
		private String providerNo;

		public String getZip()
		{
			return zip;
		}

		public void setZip(String zip)
		{
			this.zip = zip;
		}

		public String getQq()
		{
			return qq;
		}

		public void setQq(String qq)
		{
			this.qq = qq;
		}

		public String getWebsite()
		{
			return website;
		}

		public void setWebsite(String website)
		{
			this.website = website;
		}

		public String getAddress()
		{
			return address;
		}

		public void setAddress(String address)
		{
			this.address = address;
		}

		public Long getLastPurchaseTime()
		{
			return lastPurchaseTime;
		}

		public void setLastPurchaseTime(Long lastPurchaseTime)
		{
			this.lastPurchaseTime = lastPurchaseTime;
		}

		public String getWangwang()
		{
			return wangwang;
		}

		public void setWangwang(String wangwang)
		{
			this.wangwang = wangwang;
		}

		public Boolean getDisabled()
		{
			return disabled;
		}

		public void setDisabled(Boolean disabled)
		{
			this.disabled = disabled;
		}

		public String getMobile()
		{
			return mobile;
		}

		public void setMobile(String mobile)
		{
			this.mobile = mobile;
		}

		public Integer getArriveCycleDays()
		{
			return arriveCycleDays;
		}

		public void setArriveCycleDays(Integer arriveCycleDays)
		{
			this.arriveCycleDays = arriveCycleDays;
		}

		public String getRemark()
		{
			return remark;
		}

		public void setRemark(String remark)
		{
			this.remark = remark;
		}

		public String getTelno()
		{
			return telno;
		}

		public void setTelno(String telno)
		{
			this.telno = telno;
		}

		public Integer getDeleted()
		{
			return deleted;
		}

		public void setDeleted(Integer deleted)
		{
			this.deleted = deleted;
		}

		public String getContact()
		{
			return contact;
		}

		public void setContact(String contact)
		{
			this.contact = contact;
		}

		public String getFollowerName()
		{
			return followerName;
		}

		public void setFollowerName(String followerName)
		{
			this.followerName = followerName;
		}

		public String getProviderName()
		{
			return providerName;
		}

		public void setProviderName(String providerName)
		{
			this.providerName = providerName;
		}

		public String getFax()
		{
			return fax;
		}

		public void setFax(String fax)
		{
			this.fax = fax;
		}

		public String getEmail()
		{
			return email;
		}

		public void setEmail(String email)
		{
			this.email = email;
		}

		public String getProviderNo()
		{
			return providerNo;
		}

		public void setProviderNo(String providerNo)
		{
			this.providerNo = providerNo;
		}
	}

	public Integer getTotalCount()
	{
		return totalCount;
	}

	public void setTotalCount(Integer totalCount)
	{
		this.totalCount = totalCount;
	}

	public List<DetailItem> getDetailList()
	{
		return detailList;
	}

	public void setDetailList(List<DetailItem> detailList)
	{
		this.detailList = detailList;
	}
}
