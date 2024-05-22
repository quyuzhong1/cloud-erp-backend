package com.sdk.wangdian.sdk.api.goods;

import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.api.goods.dto.GoodsBrandSearchRequest;
import com.sdk.wangdian.sdk.api.goods.dto.GoodsBrandSearchResponse;
import com.sdk.wangdian.sdk.impl.Api;

public interface GoodsBrandApi
{
	@Api(value = "goods.GoodsBrand.search", paged = true)
    GoodsBrandSearchResponse search(GoodsBrandSearchRequest request, Pager pager);
}
