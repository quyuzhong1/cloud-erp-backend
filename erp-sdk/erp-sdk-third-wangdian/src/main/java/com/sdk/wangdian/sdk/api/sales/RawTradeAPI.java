package com.sdk.wangdian.sdk.api.sales;

import com.sdk.wangdian.sdk.api.sales.dto.PushSelf2Request;
import com.sdk.wangdian.sdk.api.sales.dto.PushSelf2Response;
import com.sdk.wangdian.sdk.api.sales.dto.PushSelfRequest;
import com.sdk.wangdian.sdk.api.sales.dto.PushSelfResponse;
import com.sdk.wangdian.sdk.impl.Api;

import java.util.List;
import java.util.Map;

public interface RawTradeAPI
{
	@Api(value = "sales.RawTrade.pushSelf", paged = false)
	PushSelfResponse pushSelf(String shopNo, List<PushSelfRequest.RawTrade> rawTradeList,
			List<PushSelfRequest.RawTradeOrder> rawTradeOrderList, List<PushSelfRequest.RawTradeDiscount> rawTradeDiscountList);

	@Api(value = "sales.RawTrade.pushSelf2", paged = false)
	PushSelf2Response pushSelf2(String shopNo, List<Map<String, Object>> rawTradeList,
								List<Map<String, Object>> rawTradeOrderList);
}