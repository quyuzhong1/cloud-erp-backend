package cn.wangdian.erp.sdk.api.purchaseOrder;

import cn.wangdian.erp.sdk.Pager;
import cn.wangdian.erp.sdk.WdtErpException;
import cn.wangdian.erp.sdk.api.purchaseOrder.dto.PurchaseOrderCreateRequest;
import cn.wangdian.erp.sdk.api.purchaseOrder.dto.PurchaseOrderCreateResponse;
import cn.wangdian.erp.sdk.api.purchaseOrder.dto.PurchaseOrderQueryRequest;
import cn.wangdian.erp.sdk.api.purchaseOrder.dto.PurchaseOrderQueryResponse;
import cn.wangdian.erp.sdk.impl.Api;

public interface PurchaseOrderAPI
{
	@Api(value = "purchase.PurchaseOrder.queryWithDetail", paged = true)
	PurchaseOrderQueryResponse search(PurchaseOrderQueryRequest request, Pager pager) throws WdtErpException;

	@Api(value = "purchase.PurchaseOrder.createOrder")
	PurchaseOrderCreateResponse create(PurchaseOrderCreateRequest request) throws WdtErpException ;
}
