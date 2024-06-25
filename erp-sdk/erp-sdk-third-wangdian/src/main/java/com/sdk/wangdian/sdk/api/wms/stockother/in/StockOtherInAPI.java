package com.sdk.wangdian.sdk.api.wms.stockother.in;

import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.WdtErpException;
import com.sdk.wangdian.sdk.api.wms.stockother.in.dto.StockOtherInQueryRequest;
import com.sdk.wangdian.sdk.api.wms.stockother.in.dto.StockOtherInQueryResponse;
import com.sdk.wangdian.sdk.impl.Api;

public interface StockOtherInAPI
{
	@Api(value = "wms.stockother.InQuery.queryWithDetail", paged = true)
	StockOtherInQueryResponse queryWithDetail(StockOtherInQueryRequest request, Pager pager) throws WdtErpException;
}
