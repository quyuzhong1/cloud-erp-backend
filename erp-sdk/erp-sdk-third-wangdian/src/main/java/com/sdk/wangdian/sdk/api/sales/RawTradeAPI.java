package com.sdk.wangdian.sdk.api.sales;

import com.sdk.wangdian.sdk.api.sales.dto.PushSelfRequest;
import com.sdk.wangdian.sdk.api.sales.dto.PushSelfResponse;
import com.sdk.wangdian.sdk.impl.Api;

import java.util.List;

public interface RawTradeAPI
{
	@Api(value = "sales.RawTrade.pushSelf", paged = false)
	PushSelfResponse pushSelf(String shopNo, List<PushSelfRequest.RawTrade> rawTradeList,
			List<PushSelfRequest.RawTradeOrder> rawTradeOrderList, List<PushSelfRequest.RawTradeDiscount> rawTradeDiscountList);
}