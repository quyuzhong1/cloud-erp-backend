package cn.wangdian.erp.sdk.api.purchasereturn.dto;

public class PurchaseReturnCreateOrderResponse 
{
/*
{
    "message": "CR201701010002",
    "status": 0
}
*/
	private String message;
	private Integer status;
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	
}
