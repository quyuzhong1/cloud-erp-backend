package cn.wangdian.erp.demo;

import cn.wangdian.erp.sdk.Client;
import cn.wangdian.erp.sdk.Pager;
import cn.wangdian.erp.sdk.WdtErpException;
import cn.wangdian.erp.sdk.api.sales.TradeAPI;
import cn.wangdian.erp.sdk.api.sales.dto.TradeQueryRequest;
import cn.wangdian.erp.sdk.api.sales.dto.TradeQueryResponse;
import cn.wangdian.erp.sdk.impl.ApiFactory;
import cn.wangdian.erp.sdk.impl.DefaultClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TradeQuery
{
	public static void main(String[] args) throws IOException, WdtErpException
	{
		// Client client = DefaultClient.get("wdtapi3", "test", "test");
		Client client = DefaultClient.get("wdterp30", "http://172.173.174.72:30000/", "lichAPI",
				"9d083f54d85d35379a342b154059c182:865d2f6bc467a6c7da0e0db8f7029538");

		TradeAPI tradeApi = ApiFactory.get(client, TradeAPI.class);
		
		TradeQueryResponse response = tradeApi.query(buildRequest(), new Pager(20, 0, true));
		
		if (response == null || response.getTotalCount() <= 0)
		{
			System.out.println("No eligible results!");
			return;
		}

		System.out.println(
				"total: " + response.getTotalCount() + " first trade_no:" + response.getOrders().get(0).getTradeNo());
	}

	private static TradeQueryRequest buildRequest()
	{
		TradeQueryRequest request = new TradeQueryRequest();
		// request.setSrcTid("tid-D9kO2OooUy");
		request.setTradeNo("JY202007290081");

		List statusList = new ArrayList();
		statusList.add(TradeQueryRequest.STATUS_WMS_CONFIRMED);
		// statusList.add(TradeQueryRequest.STATUS_CANCEL);
		// statusList.add(TradeQueryRequest.STATUS_WAIT_CHECK);
		// request.setStatus(StringUtils.join(statusList, ","));

		// request.setWarehouseNo("warehouse_ygg_01");
		// request.setLogisticsNo("3232323333");
		// request.setStartTime("2019-10-31 00:00:00");
		// request.setEndTime("2019-10-31 01:00:00");

		return request;
	}
}
