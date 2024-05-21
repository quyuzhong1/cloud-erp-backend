package cn.wangdian.erp.demo;

import cn.wangdian.erp.sdk.Client;
import cn.wangdian.erp.sdk.Pager;
import cn.wangdian.erp.sdk.WdtErpException;
import cn.wangdian.erp.sdk.api.goods.GoodsAPI;
import cn.wangdian.erp.sdk.api.goods.dto.GoodsSearchRequest;
import cn.wangdian.erp.sdk.api.goods.dto.GoodsSearchResponse;
import cn.wangdian.erp.sdk.api.statistic.StockAccountApi;
import cn.wangdian.erp.sdk.api.statistic.dto.StockAccountSliceRequest;
import cn.wangdian.erp.sdk.api.statistic.dto.StockAccountSliceResponse;
import cn.wangdian.erp.sdk.impl.ApiFactory;
import cn.wangdian.erp.sdk.impl.DefaultClient;

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
