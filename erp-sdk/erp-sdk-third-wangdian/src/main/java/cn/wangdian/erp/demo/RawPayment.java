package cn.wangdian.erp.demo;

import cn.wangdian.erp.sdk.Client;
import cn.wangdian.erp.sdk.Pager;
import cn.wangdian.erp.sdk.api.finance.PlatformBillAPI;
import cn.wangdian.erp.sdk.api.finance.dto.RawPaymentSearchRequest;
import cn.wangdian.erp.sdk.api.finance.dto.RawPaymentSearchResponse;
import cn.wangdian.erp.sdk.impl.ApiFactory;
import cn.wangdian.erp.sdk.impl.DefaultClient;

public class RawPayment
{
	public static void main(String[] args)
	{
		// Client client = DefaultClient.get("wdtapi3", "test", "test");
		Client client = DefaultClient.get("wdterp30", "http://192.168.10.211:30000/", "POS",
				"c1fbdd70132de9300d23a05a5f63e150:cc169a95acdaa9ffaed4e9d59f93efaa");
		PlatformBillAPI platformBillAPI = ApiFactory.get(client, PlatformBillAPI.class);

		rawPaymentSearch(platformBillAPI);
	}

	private static void rawPaymentSearch(PlatformBillAPI platformBillApi)
	{

		RawPaymentSearchRequest request = new RawPaymentSearchRequest();
		request.setStartTime("2020-8-02 00:00:00");
		request.setEndTime("2020-09-01 00:00:00");
		RawPaymentSearchResponse response = null;
		try
		{
			response = platformBillApi.searchRawPayment(request, new Pager(50, 0, true));
		}
		catch (cn.wangdian.erp.sdk.WdtErpException e)
		{
			e.printStackTrace();
		}
		for (RawPaymentSearchResponse.detailInfoDto order : response.getDetailList())
		{
			System.out.println(order.toString());
		}
	}
}
