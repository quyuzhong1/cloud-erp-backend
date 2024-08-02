package com.sdk.wangdian.sdk.api.wms.stockspec;

import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.WdtErpException;
import com.sdk.wangdian.sdk.api.wms.stockspec.dto.AvailableStockQueryRequest;
import com.sdk.wangdian.sdk.api.wms.stockspec.dto.AvailableStockQueryResponse;
import com.sdk.wangdian.sdk.impl.Api;

public interface StockSpecAPI
{
	@Api(value = "wms.StockSpec.queryAvailableStock", paged = true)
	AvailableStockQueryResponse search(AvailableStockQueryRequest request, Pager pager) throws WdtErpException;
}
