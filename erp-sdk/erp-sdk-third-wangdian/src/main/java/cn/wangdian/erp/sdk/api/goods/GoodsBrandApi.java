package cn.wangdian.erp.sdk.api.goods;

import cn.wangdian.erp.sdk.Pager;
import cn.wangdian.erp.sdk.api.goods.dto.GoodsBrandSearchRequest;
import cn.wangdian.erp.sdk.api.goods.dto.GoodsBrandSearchResponse;
import cn.wangdian.erp.sdk.impl.Api;

public interface GoodsBrandApi
{
	@Api(value = "goods.GoodsBrand.search", paged = true)
	GoodsBrandSearchResponse search(GoodsBrandSearchRequest request, Pager pager);
}
