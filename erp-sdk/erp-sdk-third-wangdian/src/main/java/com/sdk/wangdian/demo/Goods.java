package com.sdk.wangdian.demo;

import com.sdk.wangdian.sdk.Client;
import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.api.goods.GoodsAPI;
import com.sdk.wangdian.sdk.api.goods.dto.GoodsPushRequest;
import com.sdk.wangdian.sdk.api.goods.dto.GoodsSearchRequest;
import com.sdk.wangdian.sdk.api.goods.dto.GoodsSearchResponse;
import com.sdk.wangdian.sdk.impl.ApiFactory;
import com.sdk.wangdian.sdk.impl.DefaultClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Goods
{
	public static void main(String[] args)
	{
		// Client client = DefaultClient.get("wdtapi3", "test", "test");
		Client client = DefaultClient.get("wdterp30", "http://192.168.1.41:30000/", "POS",
				"c1fbdd70132de9300d23a05a5f63e150:cc169a95acdaa9ffaed4e9d59f93efaa");
		GoodsAPI goodsApi = ApiFactory.get(client, GoodsAPI.class);

//		goodsPushTest(goodsApi);

		GoodsSearchTest(goodsApi);
	}

	private static void goodsPushTest(GoodsAPI goodsApi)
	{
		GoodsPushRequest.GoodsDto goodsDto = new GoodsPushRequest.GoodsDto();
		goodsDto.setGoodsNo("test_lz123");
		goodsDto.setGoodsName("test_lz123");

		GoodsPushRequest.GoodsSpecDto goodsSpecDto = new GoodsPushRequest.GoodsSpecDto();
		goodsSpecDto.setSpecNo("test_lz123");
		goodsSpecDto.setBarcode("test_lz123");
		goodsSpecDto.setSpecName("test_lz123");
		goodsSpecDto.setMarketPrice(new BigDecimal("12"));
		goodsSpecDto.setRetailPrice(new BigDecimal("12"));
		List<GoodsPushRequest.GoodsSpecDto> goodsSpecDtos = new ArrayList<>();
		goodsSpecDtos.add(goodsSpecDto);

		int goodsId = goodsApi.push(goodsDto, goodsSpecDtos);
		System.out.println(goodsId);
	}

	private static void GoodsSearchTest(GoodsAPI goodsApi)
	{
		GoodsSearchRequest request = new GoodsSearchRequest();
		request.setSpecNo("daba3");
		GoodsSearchResponse response = goodsApi.search(request, new Pager(10, 0, true));

		if (response.getTotal() == null || response.getTotal() == 0)
		{
			System.out.println("No eligible results!");
			return;
		}

		System.out.println("total: " + response.getTotal());
		for (GoodsSearchResponse.GoodsSearchGoodsDto goodsDto : response.getGoodsInfos())
		{
			System.out.print("goods no:" + goodsDto.getGoodsNo() + " modified : " + goodsDto.getGoodsModified());
			for (GoodsSearchResponse.GoodsSearchSpecDto specDto : goodsDto.getSpecDtos())
			{
				System.out.println(" spec no:" + specDto.getSpecNo() + " modified : " + specDto.getSpecModified());
			}
		}
	}
}
