package cn.wangdian.erp.sdk.api.purchasereturn;

import java.util.List;

import cn.wangdian.erp.sdk.Pager;
import cn.wangdian.erp.sdk.WdtErpException;
import cn.wangdian.erp.sdk.api.purchasereturn.dto.PurchaseReturnCreateOrderRequest;
import cn.wangdian.erp.sdk.api.purchasereturn.dto.PurchaseReturnCreateOrderResponse;
import cn.wangdian.erp.sdk.api.purchasereturn.dto.PurchaseReturnQueryRequest;
import cn.wangdian.erp.sdk.api.purchasereturn.dto.PurchaseReturnQueryResponse;
import cn.wangdian.erp.sdk.impl.Api;

public interface PurchaseReturnAPI
{
	@Api(value = "purchase.PurchaseReturn.createOrder")
	PurchaseReturnCreateOrderResponse createOrder(PurchaseReturnCreateOrderRequest.PurchaseReturnOrderInfo orderInfo,
			List<PurchaseReturnCreateOrderRequest.PurchaseReturnDetail> detailList, boolean isSubmit);

	@Api(value = "purchase.PurchaseReturn.queryWithDetail", paged = true)
	PurchaseReturnQueryResponse search(PurchaseReturnQueryRequest request, Pager pager) throws WdtErpException;
}
