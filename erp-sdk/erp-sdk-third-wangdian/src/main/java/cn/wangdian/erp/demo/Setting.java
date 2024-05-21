package cn.wangdian.erp.demo;

import cn.wangdian.erp.sdk.Client;
import cn.wangdian.erp.sdk.Pager;
import cn.wangdian.erp.sdk.WdtErpException;
import cn.wangdian.erp.sdk.api.setting.SettingAPI;
import cn.wangdian.erp.sdk.api.setting.dto.LogisticsQueryRequest;
import cn.wangdian.erp.sdk.api.setting.dto.LogisticsQueryResponse;
import cn.wangdian.erp.sdk.api.setting.dto.ShopQueryRequest;
import cn.wangdian.erp.sdk.api.setting.dto.ShopQueryResponse;
import cn.wangdian.erp.sdk.api.setting.dto.WarehouseQueryRequest;
import cn.wangdian.erp.sdk.api.setting.dto.WarehouseQueryResponse;
import cn.wangdian.erp.sdk.impl.ApiFactory;
import cn.wangdian.erp.sdk.impl.DefaultClient;

public class Setting
{

	public static void main(String[] args) throws WdtErpException
	{
		// Client client = DefaultClient.get("wdtapi3", "test", "test");
		Client client = DefaultClient.get("wdterp30", "http://127.0.0.1:30000/", "POS",
				"c1fbdd70132de9300d23a05a5f63e150:cc169a95acdaa9ffaed4e9d59f93efaa");
		SettingAPI settingApi = ApiFactory.get(client, SettingAPI.class);

//		queryWarehouse(settingApi);
//		queryShop(settingApi);
		queryLogistics(settingApi);
	}

	private static void queryWarehouse(SettingAPI settingApi)
	{
		WarehouseQueryRequest request = new WarehouseQueryRequest();
		request.setWarehouseNo("1001");
		// request.setWarehouseName("LJ测试仓");
		// request.setType(WarehouseQueryRequest.TYPE_INNER);
		// request.setSubType(WarehouseQueryRequest.SUB_TYPE_DEFAULT);

		WarehouseQueryResponse response = settingApi.queryWarehouse(request, new Pager(2, 0, true));
		if (response.getTotal() == null || response.getTotal() == 0)
		{
			System.out.println("No eligible results!");
			return;
		}

		System.out.print("total : " + response.getTotal());
		for (WarehouseQueryResponse.WarehouseDto warehouseDto : response.getWarehouseList())
		{
			System.out.print(" warehouse no : " + warehouseDto.getWarehouseNo() + " ");
		}
	}

	private static void queryShop(SettingAPI api) throws WdtErpException
	{
		ShopQueryRequest request = new ShopQueryRequest();
		request.setShopNo("daj012");

		Pager pager = new Pager(4, 0, true);
		ShopQueryResponse response = api.search(request, pager);
		System.out.println(response.getTotal());
	}

	private static void queryLogistics(SettingAPI api) throws WdtErpException
	{
		int pageSize = 4;
		LogisticsQueryRequest request = new LogisticsQueryRequest();
		request.setLogisticsNo("logistics_no");

		Pager pager = new Pager(pageSize, 0, true);
		LogisticsQueryResponse response = api.queryLogistics(request, pager);

		Integer total = response.getTotal();
		if (null == total || pageSize >= total)
		{
			System.out.println("处理数据");
			return ;
		}

		int totalPage = (total % pageSize == 0 ? total / pageSize : total / pageSize + 1) - 1;
		pager.setCalcTotal(false); //后续翻页不需要计算总条数, 可以大大减少请求时间
		for (int i = totalPage; i >= 0; i--)// 从后向前翻页
		{
			pager.setPageNo(i);
			System.out.print("pager: page_size:" + pageSize + "  page_no: " + i + "  ");
			response = api.queryLogistics(request, pager);
			System.out.println("处理数据");
		}
	}
}
