package cn.wangdian.erp.sdk.api.wms.stockout;

import cn.wangdian.erp.sdk.Pager;
import cn.wangdian.erp.sdk.WdtErpException;
import cn.wangdian.erp.sdk.api.purchasereturn.dto.PurchaseReturnCreateOrderResponse;
import cn.wangdian.erp.sdk.api.wms.stockin.dto.CreateTransferStockinRequest;
import cn.wangdian.erp.sdk.api.wms.stockout.dto.CreateOtherStockoutRequest;
import cn.wangdian.erp.sdk.api.wms.stockout.dto.CreateOtherStockoutResponse;
import cn.wangdian.erp.sdk.api.wms.stockout.dto.CreateTransferStockoutRequest;
import cn.wangdian.erp.sdk.api.wms.stockout.dto.CreateTransferStockoutResponse;
import cn.wangdian.erp.sdk.api.wms.stockout.dto.ProcessStockoutRequest;
import cn.wangdian.erp.sdk.api.wms.stockout.dto.ProcessStockoutResponse;
import cn.wangdian.erp.sdk.api.wms.stockout.dto.SalesStockoutRequest;
import cn.wangdian.erp.sdk.api.wms.stockout.dto.SalesStockoutResponse;
import cn.wangdian.erp.sdk.api.wms.stockout.dto.StockoutOtherQueryRequest;
import cn.wangdian.erp.sdk.api.wms.stockout.dto.StockoutOtherQueryResponse;
import cn.wangdian.erp.sdk.api.wms.stockout.dto.StockoutSearchRequest;
import cn.wangdian.erp.sdk.api.wms.stockout.dto.StockoutSearchResponse;
import cn.wangdian.erp.sdk.impl.Api;

import java.net.ConnectException;
import java.util.List;
import java.util.Map;

public interface StockoutAPI
{
	@Api(value = "wms.stockout.Sales.queryWithDetail", paged = true)
	SalesStockoutResponse querySales(SalesStockoutRequest request, Pager pager) throws WdtErpException;

	@Api(value = "wms.stockout.Other.createOther")
	CreateOtherStockoutResponse createOtherOutOrder(CreateOtherStockoutRequest request);

	@Api(value = "wms.stockout.Transfer.createOrder")
	CreateTransferStockoutResponse createTransferOrder(CreateTransferStockoutRequest.orderInfoDto orderInfo,
			List<CreateTransferStockoutRequest.detailDto> detailList, boolean isCheck);

	@Api(value = "wms.stockout.Process.queryWithDetail", paged = true)
	ProcessStockoutResponse searchProcess(ProcessStockoutRequest request, Pager pager);

	@Api(value = "wms.stockout.OtherQuery.queryWithDetail", paged = true)
	StockoutOtherQueryResponse searchOther(StockoutOtherQueryRequest request, Pager pager) throws ConnectException,WdtErpException;

	@Api(value = "wms.stockout.Base.search", paged = true)
	StockoutSearchResponse search(StockoutSearchRequest request, Pager pager) throws WdtErpException ;
}
