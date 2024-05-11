package cn.wangdian.erp.sdk.api.sales.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LogisticsSyncUpdateResponse
{
	/*
	 * {"status":0,"data":[]}
	 */
	@SerializedName("data")
	private List<String> errorMessages;

	public List<String> getErrorMessages()
	{
		return errorMessages;
	}

	public void setErrorMessages(List<String> errorMessages)
	{
		this.errorMessages = errorMessages;
	}
}
