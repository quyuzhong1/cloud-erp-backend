package com.sdk.wangdian.sdk.api.goods;

import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.api.Result;
import com.sdk.wangdian.sdk.api.goods.dto.GoodsPushRequest;
import com.sdk.wangdian.sdk.api.goods.dto.GoodsSearchRequest;
import com.sdk.wangdian.sdk.api.goods.dto.GoodsSearchResponse;
import com.sdk.wangdian.sdk.impl.Api;

import java.util.List;
import java.util.Map;

public interface GoodsAPI
{
	@Api(value = "goods.Goods.push")
	int push(GoodsPushRequest.GoodsDto goodsDto, List<GoodsPushRequest.GoodsSpecDto> goodsSpecDtos);

	@Api(value = "goods.Goods.queryWithSpec", paged = true)
    GoodsSearchResponse search(GoodsSearchRequest request, Pager pager);


	@Api(value = "goods.Goods.batchPush")
	Result batchPush(List<Map<String, Object>> request);
}
