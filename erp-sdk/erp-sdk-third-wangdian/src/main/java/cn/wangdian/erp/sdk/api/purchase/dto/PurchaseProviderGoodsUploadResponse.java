package cn.wangdian.erp.sdk.api.purchase.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PurchaseProviderGoodsUploadResponse
{
	@SerializedName("error_list")
	private List<String> errorList ;

	public List<String> getErrorList()
	{
		return errorList;
	}

	public void setErrorList(List<String> errorList)
	{
		this.errorList = errorList;
	}
}
