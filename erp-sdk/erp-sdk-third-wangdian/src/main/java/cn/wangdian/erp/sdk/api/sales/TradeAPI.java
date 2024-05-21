package cn.wangdian.erp.sdk.api.sales;

import cn.wangdian.erp.sdk.Pager;
import cn.wangdian.erp.sdk.api.sales.dto.TradeQueryRequest;
import cn.wangdian.erp.sdk.api.sales.dto.TradeQueryResponse;
import cn.wangdian.erp.sdk.impl.Api;

public interface TradeAPI
{
	@Api(value = "sales.TradeQuery.queryWithDetail", paged = true)
	TradeQueryResponse query(TradeQueryRequest request, Pager pager);
}