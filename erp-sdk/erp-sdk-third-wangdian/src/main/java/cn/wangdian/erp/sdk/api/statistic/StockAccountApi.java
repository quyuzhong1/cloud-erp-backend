package cn.wangdian.erp.sdk.api.statistic;

import cn.wangdian.erp.sdk.Pager;
import cn.wangdian.erp.sdk.WdtErpException;
import cn.wangdian.erp.sdk.api.statistic.dto.StockAccountSliceRequest;
import cn.wangdian.erp.sdk.api.statistic.dto.StockAccountSliceResponse;
import cn.wangdian.erp.sdk.impl.Api;

public interface StockAccountApi
{
	@Api(value = "xstockaccountslice.ExtStockAccountSlice.getAll", paged = true)
	StockAccountSliceResponse searchStockAccountSlice(StockAccountSliceRequest request, Pager pager) throws WdtErpException;
}
