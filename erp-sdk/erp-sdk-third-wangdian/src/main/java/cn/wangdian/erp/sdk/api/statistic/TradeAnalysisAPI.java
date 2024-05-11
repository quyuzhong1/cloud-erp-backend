package cn.wangdian.erp.sdk.api.statistic;

import cn.wangdian.erp.sdk.Pager;
import cn.wangdian.erp.sdk.WdtErpException;
import cn.wangdian.erp.sdk.api.statistic.dto.RefundCollectSearchRequest;
import cn.wangdian.erp.sdk.api.statistic.dto.RefundCollectSearchResponse;
import cn.wangdian.erp.sdk.impl.Api;

public interface TradeAnalysisAPI
{
	@Api(value = "statistic.StaTradeAnalysis.staRefundCollect", paged = true)
	public RefundCollectSearchResponse searchRefundCollect(RefundCollectSearchRequest request, Pager pager) throws WdtErpException;
}
