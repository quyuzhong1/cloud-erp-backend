package com.sdk.wangdian.sdk.api.setting.dto;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.util.List;

public class LogisticsQueryResponse
{

	@SerializedName("total_count") private Integer total;
	@SerializedName("details") private List<Details> detailList;

	public Integer getTotal()
	{
		return total;
	}

	public void setTotal(Integer total)
	{
		this.total = total;
	}

	public List<Details> getDetailList()
	{
		return detailList;
	}

	public void setDetailList(List<Details> detailList)
	{
		this.detailList = detailList;
	}

	public static class Details
	{
		@SerializedName("logistics_name") private String logisticsName;
		@SerializedName("address") private String address;
		@SerializedName("is_preset_no") private Boolean presetNo;
		@SerializedName("logistics_no") private String logisticsNo;
		@SerializedName("send_type") private Integer sendType;
		@SerializedName("created") private String created;
		@SerializedName("mobile") private String mobile;
		@SerializedName("remark") private String remark;
		@SerializedName("ebill_api") private Integer ebillApi;
		@SerializedName("telno") private String telno;
		@SerializedName("logistics_id") private Integer logisticsId;
		@SerializedName("max_weight") private BigDecimal maxWeight;
		@SerializedName("contact") private String contact;
		@SerializedName("modified") private String modified;
		@SerializedName("logistics_type") private Integer logisticsType;

		public Boolean getDisabled() {
			return disabled;
		}

		public void setDisabled(Boolean disabled) {
			this.disabled = disabled;
		}

		@SerializedName("is_support_cod") private Boolean supportCode;
		@SerializedName("is_disabled") private Boolean disabled;

		public String getLogisticsName()
		{
			return logisticsName;
		}

		public void setLogisticsName(String logisticsName)
		{
			this.logisticsName = logisticsName;
		}

		public String getAddress()
		{
			return address;
		}

		public void setAddress(String address)
		{
			this.address = address;
		}

		public Boolean getPresetNo()
		{
			return presetNo;
		}

		public void setPresetNo(Boolean presetNo)
		{
			this.presetNo = presetNo;
		}

		public String getLogisticsNo()
		{
			return logisticsNo;
		}

		public void setLogisticsNo(String logisticsNo)
		{
			this.logisticsNo = logisticsNo;
		}

		public Integer getSendType()
		{
			return sendType;
		}

		public void setSendType(Integer sendType)
		{
			this.sendType = sendType;
		}

		public String getCreated()
		{
			return created;
		}

		public void setCreated(String created)
		{
			this.created = created;
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

		public Integer getEbillApi()
		{
			return ebillApi;
		}

		public void setEbillApi(Integer ebillApi)
		{
			this.ebillApi = ebillApi;
		}

		public String getTelno()
		{
			return telno;
		}

		public void setTelno(String telno)
		{
			this.telno = telno;
		}

		public Integer getLogisticsId()
		{
			return logisticsId;
		}

		public void setLogisticsId(Integer logisticsId)
		{
			this.logisticsId = logisticsId;
		}

		public BigDecimal getMaxWeight()
		{
			return maxWeight;
		}

		public void setMaxWeight(BigDecimal maxWeight)
		{
			this.maxWeight = maxWeight;
		}

		public String getContact()
		{
			return contact;
		}

		public void setContact(String contact)
		{
			this.contact = contact;
		}

		public String getModified()
		{
			return modified;
		}

		public void setModified(String modified)
		{
			this.modified = modified;
		}

		public Integer getLogisticsType()
		{
			return logisticsType;
		}

		public void setLogisticsType(Integer logisticsType)
		{
			this.logisticsType = logisticsType;
		}

		public Boolean getSupportCode()
		{
			return supportCode;
		}

		public void setSupportCode(Boolean supportCode)
		{
			this.supportCode = supportCode;
		}
	}
}
