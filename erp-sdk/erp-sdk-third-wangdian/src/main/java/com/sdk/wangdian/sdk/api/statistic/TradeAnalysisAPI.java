package com.sdk.wangdian.sdk.api.statistic;

import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.WdtErpException;
import com.sdk.wangdian.sdk.api.statistic.dto.RefundCollectSearchRequest;
import com.sdk.wangdian.sdk.api.statistic.dto.RefundCollectSearchResponse;
import com.sdk.wangdian.sdk.impl.Api;

public interface TradeAnalysisAPI
{
	@Api(value = "statistic.StaTradeAnalysis.staRefundCollect", paged = true)
	public RefundCollectSearchResponse searchRefundCollect(RefundCollectSearchRequest request, Pager pager) throws WdtErpException;
}
