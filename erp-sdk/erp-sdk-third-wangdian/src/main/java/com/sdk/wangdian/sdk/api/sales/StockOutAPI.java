package com.sdk.wangdian.sdk.api.sales;

import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.api.sales.dto.StockOutQueryRequest;
import com.sdk.wangdian.sdk.api.sales.dto.StockOutQueryResponse;
import com.sdk.wangdian.sdk.impl.Api;

public interface StockOutAPI
{
	@Api(value = "wms.stockout.Sales.queryWithDetail", paged = true)
	StockOutQueryResponse query(StockOutQueryRequest request, Pager pager);
}
