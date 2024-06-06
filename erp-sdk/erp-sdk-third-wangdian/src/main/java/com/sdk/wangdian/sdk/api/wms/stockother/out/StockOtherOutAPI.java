package com.sdk.wangdian.sdk.api.wms.stockother.out;

import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.WdtErpException;
import com.sdk.wangdian.sdk.api.wms.stockother.out.dto.StockOtherOutQueryRequest;
import com.sdk.wangdian.sdk.api.wms.stockother.out.dto.StockOtherOutQueryResponse;
import com.sdk.wangdian.sdk.impl.Api;

public interface StockOtherOutAPI
{
	@Api(value = "wms.stockother.OutQuery.queryWithDetail", paged = true)
	StockOtherOutQueryResponse queryWithDetail(StockOtherOutQueryRequest request, Pager pager) throws WdtErpException;
}
