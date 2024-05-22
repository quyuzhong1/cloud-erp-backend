package com.sdk.wangdian.demo;

import com.sdk.wangdian.sdk.Client;
import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.WdtErpException;
import com.sdk.wangdian.sdk.api.statistic.StockAccountApi;
import com.sdk.wangdian.sdk.api.statistic.dto.StockAccountSliceRequest;
import com.sdk.wangdian.sdk.api.statistic.dto.StockAccountSliceResponse;
import com.sdk.wangdian.sdk.impl.ApiFactory;
import com.sdk.wangdian.sdk.impl.DefaultClient;

public class StockAccount
{
	public static void main(String[] args) throws WdtErpException
	{
		// Client client = DefaultClient.get("wdtapi3", "test", "test");
		Client client = DefaultClient.get("wdterp30", "http://127.0.0.1:30000/", "POS",
				"c1fbdd70132de9300d23a05a5f63e150:cc169a95acdaa9ffaed4e9d59f93efaa");
		StockAccountApi stockAccountApi = ApiFactory.get(client, StockAccountApi.class);

		//		goodsPushTest(goodsApi);

		search(stockAccountApi);
	}

	static void search(StockAccountApi stockAccountApi) throws WdtErpException
	{
		StockAccountSliceRequest request = new StockAccountSliceRequest();
		request.setDate("2023-04-10");
		request.setWarehouseNos("ytz");
		StockAccountSliceResponse response = stockAccountApi.searchStockAccountSlice(request, new Pager(10, 0, true));

		if (response.getTotal() == null || response.getTotal() == 0)
		{
			System.out.println("No eligible results!");
			return;
		}

		System.out.println("total: " + response.getTotal());
		for (StockAccountSliceResponse.Detail detail : response.getDetailList())
		{
			System.out.print("goods no:" + detail.getGoodsNo() + " modified : " + detail.getGoodsName());

		}
	}
}
