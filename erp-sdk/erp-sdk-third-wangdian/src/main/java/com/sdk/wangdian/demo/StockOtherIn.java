package com.sdk.wangdian.demo;

import com.sdk.wangdian.sdk.Client;
import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.WdtErpException;
import com.sdk.wangdian.sdk.api.wms.stockother.in.StockOtherInAPI;
import com.sdk.wangdian.sdk.api.wms.stockother.in.dto.StockOtherInQueryRequest;
import com.sdk.wangdian.sdk.api.wms.stockother.in.dto.StockOtherInQueryResponse;
import com.sdk.wangdian.sdk.impl.ApiFactory;
import com.sdk.wangdian.sdk.impl.DefaultClient;

public class StockOtherIn
{
	public static void main(String[] args) throws WdtErpException
	{
		Client client = DefaultClient.get("wdterp30", "http://127.0.0.1:30000/", "zyOther",
				"423c25002f36c7445ccd7742ea5d1be4:8f034c5a36d5749a438951cde1963f2a");
		StockOtherInAPI stockOtherInAPI = ApiFactory.get(client, StockOtherInAPI.class);

		queryWithDetail(stockOtherInAPI);
	}

	private static void queryWithDetail(StockOtherInAPI api) throws WdtErpException
	{
		int pageSize = 5;
		Pager pager = new Pager(pageSize, 0, true);
		StockOtherInQueryRequest request = new StockOtherInQueryRequest();
		request.setStartTime("2022-09-01");
		request.setEndTime("2022-09-30");
		StockOtherInQueryResponse response = api.queryWithDetail(request, pager);

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
			response = api.queryWithDetail(request, pager);
			System.out.println("处理数据");
		}
	}

}
