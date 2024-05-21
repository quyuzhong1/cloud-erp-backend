package cn.wangdian.erp.sdk.api.sales;

import cn.wangdian.erp.sdk.api.sales.dto.PushSelfRequest;
import cn.wangdian.erp.sdk.api.sales.dto.PushSelfResponse;
import cn.wangdian.erp.sdk.impl.Api;

import java.util.List;

public interface RawTradeAPI
{
	@Api(value = "sales.RawTrade.pushSelf", paged = false)
	PushSelfResponse pushSelf(String shopNo, List<PushSelfRequest.RawTrade> rawTradeList,
			List<PushSelfRequest.RawTradeOrder> rawTradeOrderList, List<PushSelfRequest.RawTradeDiscount> rawTradeDiscountList);
}