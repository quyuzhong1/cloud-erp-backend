package cn.wangdian.erp.sdk.api.sales.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class LogisticsSyncUpdateDto
{

	// 同步成功
	public static final byte SYNC_SUCCESS = 0;
	// 还需同步
	public static final byte SYNC_FAIL = 2;
	// 同步失败
	public static final byte SYNC_RETRY = -100;

	@SerializedName("sync_id")
	private Integer syncId;
	@SerializedName("status")
	private Byte status;
	@SerializedName("error_msg")
	private String errorMessage;// 若成功传""

	public LogisticsSyncUpdateDto()
	{
	}

	public LogisticsSyncUpdateDto(Integer syncId, Byte status, String errorMessage)
	{
		this.syncId = syncId;
		this.status = status;
		this.errorMessage = errorMessage;
	}

	public Integer getSyncId()
	{
		return syncId;
	}

	public void setSyncId(Integer syncId)
	{
		this.syncId = syncId;
	}

	public Byte getStatus()
	{
		return status;
	}

	public void setStatus(Byte status)
	{
		this.status = status;
	}

	public String getErrorMessage()
	{
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}
}
