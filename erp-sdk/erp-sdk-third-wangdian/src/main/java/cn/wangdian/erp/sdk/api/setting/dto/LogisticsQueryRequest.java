package cn.wangdian.erp.sdk.api.setting.dto;

import com.google.gson.annotations.SerializedName;

public class LogisticsQueryRequest
{
	@SerializedName("logistics_no") private String logisticsNo;
	@SerializedName("logistics_name") private String logisticsName;
	@SerializedName("hide_delete") private Boolean hideDelete;

	public String getLogisticsNo()
	{
		return logisticsNo;
	}

	public void setLogisticsNo(String logisticsNo)
	{
		this.logisticsNo = logisticsNo;
	}

	public String getLogisticsName()
	{
		return logisticsName;
	}

	public void setLogisticsName(String logisticsName)
	{
		this.logisticsName = logisticsName;
	}

	public Boolean getHideDelete()
	{
		return hideDelete;
	}

	public void setHideDelete(Boolean hideDelete)
	{
		this.hideDelete = hideDelete;
	}
}
