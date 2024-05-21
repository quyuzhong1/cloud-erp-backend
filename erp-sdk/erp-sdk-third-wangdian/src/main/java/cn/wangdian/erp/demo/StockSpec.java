package cn.wangdian.erp.demo;

import cn.wangdian.erp.sdk.Client;
import cn.wangdian.erp.sdk.Pager;
import cn.wangdian.erp.sdk.WdtErpException;
import cn.wangdian.erp.sdk.api.wms.stockspec.StockSpecAPI;
import cn.wangdian.erp.sdk.api.wms.stockspec.dto.AvailableStockQueryRequest;
import cn.wangdian.erp.sdk.api.wms.stockspec.dto.AvailableStockQueryResponse;
import cn.wangdian.erp.sdk.impl.ApiFactory;
import cn.wangdian.erp.sdk.impl.DefaultClient;


public class StockSpec
{
	public static void main(String[] args) throws WdtErpException
	{
		Client client = DefaultClient.get("wdterp30", "http://127.0.0.1:30000/", "zyOther",
				"423c25002f36c7445ccd7742ea5d1be4:8f034c5a36d5749a438951cde1963f2a");
		StockSpecAPI stockoutAPI = ApiFactory.get(client, StockSpecAPI.class);
		queryAvailableStock(stockoutAPI);
	}

	private static void queryAvailableStock(StockSpecAPI api) throws WdtErpException
	{
		int pageSize = 5;
		Pager pager = new Pager(pageSize, 0, true);
		AvailableStockQueryRequest request = new AvailableStockQueryRequest();
//		request.setEndTime("2023-01-01 00:00:00");
//		request.setStartTime("2023-01-01 01:00:00");
		request.setWarehouseNo("ytz1");
		AvailableStockQueryResponse response = api.search(request, pager);

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
			response = api.search(request, pager);
			System.out.println("处理数据");
		}
	}
}
