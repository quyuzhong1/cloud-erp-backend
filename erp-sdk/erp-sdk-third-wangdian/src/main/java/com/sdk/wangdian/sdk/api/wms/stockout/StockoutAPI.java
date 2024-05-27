package com.sdk.wangdian.sdk.api.wms.stockout;

import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.WdtErpException;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.CreateOtherStockoutResponse;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.CreateTransferStockoutRequest;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.CreateTransferStockoutResponse;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.ProcessStockoutRequest;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.ProcessStockoutResponse;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.SalesStockoutRequest;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.SalesStockoutResponse;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.StockoutOtherQueryRequest;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.StockoutOtherQueryResponse;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.StockoutSearchRequest;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.StockoutSearchResponse;
import com.sdk.wangdian.sdk.impl.Api;

import java.net.ConnectException;
import java.util.List;
import java.util.Map;

public interface StockoutAPI
{
	@Api(value = "wms.stockout.Sales.queryWithDetail", paged = true)
	SalesStockoutResponse querySales(SalesStockoutRequest request, Pager pager) throws WdtErpException;

	@Api(value = "wms.stockout.Other.createOther")
	CreateOtherStockoutResponse createOtherOutOrder(Map<String, Object> request) throws WdtErpException;

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
