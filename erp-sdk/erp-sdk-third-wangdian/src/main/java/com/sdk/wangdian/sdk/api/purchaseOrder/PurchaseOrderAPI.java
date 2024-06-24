package com.sdk.wangdian.sdk.api.purchaseOrder;

import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.WdtErpException;
import com.sdk.wangdian.sdk.api.purchaseOrder.dto.PurchaseOrderCreateRequest;
import com.sdk.wangdian.sdk.api.purchaseOrder.dto.PurchaseOrderCreateResponse;
import com.sdk.wangdian.sdk.api.purchaseOrder.dto.PurchaseOrderQueryRequest;
import com.sdk.wangdian.sdk.api.purchaseOrder.dto.PurchaseOrderQueryResponse;
import com.sdk.wangdian.sdk.impl.Api;

public interface PurchaseOrderAPI
{
	@Api(value = "purchase.PurchaseOrder.queryWithDetail", paged = true)
	PurchaseOrderQueryResponse search(PurchaseOrderQueryRequest request, Pager pager) throws WdtErpException;

	@Api(value = "purchase.PurchaseOrder.createOrder")
	PurchaseOrderCreateResponse create(PurchaseOrderCreateRequest request) throws WdtErpException ;
}
