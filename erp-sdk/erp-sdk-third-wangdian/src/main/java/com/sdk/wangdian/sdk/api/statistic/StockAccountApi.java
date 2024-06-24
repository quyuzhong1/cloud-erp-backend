package com.sdk.wangdian.sdk.api.statistic;

import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.WdtErpException;
import com.sdk.wangdian.sdk.api.statistic.dto.StockAccountSliceRequest;
import com.sdk.wangdian.sdk.api.statistic.dto.StockAccountSliceResponse;
import com.sdk.wangdian.sdk.impl.Api;

public interface StockAccountApi
{
	@Api(value = "xstockaccountslice.ExtStockAccountSlice.getAll", paged = true)
	StockAccountSliceResponse searchStockAccountSlice(StockAccountSliceRequest request, Pager pager) throws WdtErpException;
}
