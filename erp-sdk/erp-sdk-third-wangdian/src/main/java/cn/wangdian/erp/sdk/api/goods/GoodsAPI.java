package cn.wangdian.erp.sdk.api.goods;

import cn.wangdian.erp.sdk.Pager;
import cn.wangdian.erp.sdk.api.Result;
import cn.wangdian.erp.sdk.api.goods.dto.GoodsBatchPushDTO;
import cn.wangdian.erp.sdk.api.goods.dto.GoodsPushRequest;
import cn.wangdian.erp.sdk.api.goods.dto.GoodsSearchRequest;
import cn.wangdian.erp.sdk.api.goods.dto.GoodsSearchResponse;
import cn.wangdian.erp.sdk.impl.Api;

import java.util.List;

public interface GoodsAPI
{
	@Api(value = "goods.Goods.push")
	int push(GoodsPushRequest.GoodsDto goodsDto, List<GoodsPushRequest.GoodsSpecDto> goodsSpecDtos);

	@Api(value = "goods.Goods.queryWithSpec", paged = true)
	GoodsSearchResponse search(GoodsSearchRequest request, Pager pager);


	@Api(value = "goods.Goods.batchPush")
	Result batchPush(List<GoodsBatchPushDTO> dto);
}
