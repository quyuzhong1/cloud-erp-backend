package cn.wangdian.erp.sdk.api.purchase;

import cn.wangdian.erp.sdk.WdtErpException;
import cn.wangdian.erp.sdk.api.purchase.dto.PurchaseProviderGoodsUploadRequest;
import cn.wangdian.erp.sdk.api.purchase.dto.PurchaseProviderGoodsUploadResponse;
import cn.wangdian.erp.sdk.impl.Api;

import java.util.List;

public interface PurchaseProviderGoodsAPI
{
	@Api(value = "purchase.ProviderGoods.upload")
	PurchaseProviderGoodsUploadResponse upload (List<PurchaseProviderGoodsUploadRequest> request) throws WdtErpException;
}
