package com.sdk.wangdian.sdk.api.purchase;

import com.sdk.wangdian.sdk.WdtErpException;
import com.sdk.wangdian.sdk.api.purchase.dto.PurchaseProviderGoodsUploadRequest;
import com.sdk.wangdian.sdk.api.purchase.dto.PurchaseProviderGoodsUploadResponse;
import com.sdk.wangdian.sdk.impl.Api;

import java.util.List;

public interface PurchaseProviderGoodsAPI
{
	@Api(value = "purchase.ProviderGoods.upload")
	PurchaseProviderGoodsUploadResponse upload (List<PurchaseProviderGoodsUploadRequest> request) throws WdtErpException;
}
