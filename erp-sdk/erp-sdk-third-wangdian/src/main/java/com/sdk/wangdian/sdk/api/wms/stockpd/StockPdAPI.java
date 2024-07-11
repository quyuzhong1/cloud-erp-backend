package com.sdk.wangdian.sdk.api.wms.stockpd;

import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.WdtErpException;
import com.sdk.wangdian.sdk.api.wms.stockpd.dto.StockPdQueryDetailRequest;
import com.sdk.wangdian.sdk.api.wms.stockpd.dto.StockPdQueryDetailResponse;
import com.sdk.wangdian.sdk.api.wms.stockpd.dto.StockPdQueryRequest;
import com.sdk.wangdian.sdk.api.wms.stockpd.dto.StockPdQueryResponse;
import com.sdk.wangdian.sdk.impl.Api;

public interface StockPdAPI
{
    @Api(value = "wms.StockPd.queryStockPd", paged = true)
    StockPdQueryResponse search(StockPdQueryRequest request, Pager pager) throws WdtErpException;

    @Api(value = "wms.StockPd.queryStockPdDetail", paged = true)
    StockPdQueryDetailResponse search(StockPdQueryDetailRequest request, Pager pager) throws WdtErpException;
}
