package cn.wangdian.erp.sdk.api.sales;

import cn.wangdian.erp.sdk.Pager;
import cn.wangdian.erp.sdk.api.sales.dto.StockOutQueryRequest;
import cn.wangdian.erp.sdk.api.sales.dto.StockOutQueryResponse;
import cn.wangdian.erp.sdk.impl.Api;

public interface StockOutAPI
{
	@Api(value = "wms.stockout.Sales.queryWithDetail", paged = true)
	StockOutQueryResponse query(StockOutQueryRequest request, Pager pager);
}
