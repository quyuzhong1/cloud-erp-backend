package com.sdk.wangdian.sdk.api.purchasereturn;

import java.util.List;

import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.WdtErpException;
import com.sdk.wangdian.sdk.api.purchasereturn.dto.PurchaseReturnCreateOrderRequest;
import com.sdk.wangdian.sdk.api.purchasereturn.dto.PurchaseReturnCreateOrderResponse;
import com.sdk.wangdian.sdk.api.purchasereturn.dto.PurchaseReturnQueryRequest;
import com.sdk.wangdian.sdk.api.purchasereturn.dto.PurchaseReturnQueryResponse;
import com.sdk.wangdian.sdk.impl.Api;

public interface PurchaseReturnAPI
{
	@Api(value = "purchase.PurchaseReturn.createOrder")
	PurchaseReturnCreateOrderResponse createOrder(PurchaseReturnCreateOrderRequest.PurchaseReturnOrderInfo orderInfo,
			List<PurchaseReturnCreateOrderRequest.PurchaseReturnDetail> detailList, boolean isSubmit);

	@Api(value = "purchase.PurchaseReturn.queryWithDetail", paged = true)
	PurchaseReturnQueryResponse search(PurchaseReturnQueryRequest request, Pager pager) throws WdtErpException;
}
