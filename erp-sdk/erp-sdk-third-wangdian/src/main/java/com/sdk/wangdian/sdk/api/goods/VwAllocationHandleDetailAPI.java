package com.sdk.wangdian.sdk.api.goods;

import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.api.Result;
import com.sdk.wangdian.sdk.api.goods.dto.GoodsPushRequest;
import com.sdk.wangdian.sdk.api.goods.dto.GoodsSearchRequest;
import com.sdk.wangdian.sdk.api.goods.dto.GoodsSearchResponse;
import com.sdk.wangdian.sdk.impl.Api;

import java.util.List;
import java.util.Map;

public interface VwAllocationHandleDetailAPI
{
	@Api(value = "setting.strategy.VirtualWarehouse.create")
	Result batchPush(List<Map<String, Object>> request);
}
