package cn.wangdian.erp.sdk.api.purchaseOrder.dto;

public class PurchaseOrderCreateResponse
{
	private int status ;
	private String message ;

	public int getStatus()
	{
		return status;
	}

	public void setStatus(int status)
	{
		this.status = status;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}
}
