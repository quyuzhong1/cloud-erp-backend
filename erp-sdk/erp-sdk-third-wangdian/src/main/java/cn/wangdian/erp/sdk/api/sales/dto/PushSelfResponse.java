package cn.wangdian.erp.sdk.api.sales.dto;

import com.google.gson.annotations.SerializedName;

public class PushSelfResponse
{
	/**
	 * {"status":0,"data":{"chg_count":0,"new_count":1}}
	 */
	@SerializedName("chg_count")
	private Integer chgCount;
	@SerializedName("new_count")
	private Integer newCount;

	public Integer getChgCount()
	{
		return chgCount;
	}

	public void setChgCount(Integer chgCount)
	{
		this.chgCount = chgCount;
	}

	public Integer getNewCount()
	{
		return newCount;
	}

	public void setNewCount(Integer newCount)
	{
		this.newCount = newCount;
	}
}
