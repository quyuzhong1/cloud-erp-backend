package cn.wangdian.erp.sdk.api.sales.dto;

public class LogisticsSyncSpecialOidsGetResponse
{
	/*
	 * response: {"status":0,"data":{"code":0,"message":"ok","oids":
	 * "sX1BzfRZHl_1,sX1BzfRZHl_10"}}
	 */
	private String code;
	private String message;
	private String oids;

	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public String getOids()
	{
		return oids;
	}

	public void setOids(String oids)
	{
		this.oids = oids;
	}
}
