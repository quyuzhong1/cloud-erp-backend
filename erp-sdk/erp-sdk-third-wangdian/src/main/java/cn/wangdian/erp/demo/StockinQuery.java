package cn.wangdian.erp.demo;

import cn.wangdian.erp.sdk.Client;
import cn.wangdian.erp.sdk.Pager;
import cn.wangdian.erp.sdk.WdtErpException;
import cn.wangdian.erp.sdk.api.wms.stockin.StockinAPI;
import cn.wangdian.erp.sdk.api.wms.stockin.dto.OtherStockinRequest;
import cn.wangdian.erp.sdk.api.wms.stockin.dto.OtherStockinResponse;
import cn.wangdian.erp.sdk.impl.ApiFactory;
import cn.wangdian.erp.sdk.impl.DefaultClient;
import java.io.IOException;


public class StockinQuery
{
    public static void main(String[] args) throws IOException, WdtErpException
    {
        //        Client client = DefaultClient.get("wdtapi3", "test", "test");
        Client client = DefaultClient.get("wdterp30", "http://192.168.10.194:30000/openapi", "zyOther", "423c25002f36c7445ccd7742ea5d1be4:8f034c5a36d5749a438951cde1963f2a");
        StockinAPI stockinAPI = ApiFactory.get(client, StockinAPI.class);
        
        OtherStockinRequest request = new OtherStockinRequest();
    	
    	request.setStartTime("2020-06-2 00:00:00");
    	request.setEndTime("2020-06-11 00:00:00");
        
        OtherStockinResponse response = stockinAPI.queryWithDetail(request, new Pager(20, 0, true));
		if (response == null)
		{
			System.out.println("No eligible results!");
			return;
		}
    }

    private static OtherStockinRequest buildRequest()
    {
    	OtherStockinRequest request = new OtherStockinRequest();
    	
    	request.setStartTime("2020-06-2 00:00:00");
    	request.setEndTime("2020-06-11 00:00:00");
    	
        return request;
    }
}
