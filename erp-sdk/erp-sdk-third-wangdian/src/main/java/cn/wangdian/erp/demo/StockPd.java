package cn.wangdian.erp.demo;

import cn.wangdian.erp.sdk.Client;
import cn.wangdian.erp.sdk.Pager;
import cn.wangdian.erp.sdk.WdtErpException;
import cn.wangdian.erp.sdk.api.wms.stockpd.StockPdAPI;
import cn.wangdian.erp.sdk.api.wms.stockpd.dto.StockPdQueryDetailRequest;
import cn.wangdian.erp.sdk.api.wms.stockpd.dto.StockPdQueryDetailResponse;
import cn.wangdian.erp.sdk.api.wms.stockpd.dto.StockPdQueryRequest;
import cn.wangdian.erp.sdk.api.wms.stockpd.dto.StockPdQueryResponse;
import cn.wangdian.erp.sdk.impl.ApiFactory;
import cn.wangdian.erp.sdk.impl.DefaultClient;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.util.Date;

public class StockPd
{
    public static void main(String[] args) throws WdtErpException
    {
        // Client client = DefaultClient.get("wdtapi3", "test", "test");
        Client client = DefaultClient.get("wdterp30", "http://127.0.0.1:30000/", "POS",
                "c1fbdd70132de9300d23a05a5f63e150:cc169a95acdaa9ffaed4e9d59f93efaa");
        StockPdAPI stockPdAPI = ApiFactory.get(client, StockPdAPI.class);

    //    queryTest(stockPdAPI);

        queryDetailTest(stockPdAPI);
    }

    private static void queryTest(StockPdAPI stockPdAPI) throws WdtErpException
    {
        StockPdQueryRequest request = new StockPdQueryRequest();
        Date now = new Date();
        request.setEndTime(DateFormatUtils.format(now, "yyyy-MM-dd HH:mm:ss"));
        request.setStartTime(DateFormatUtils.format(DateUtils.addDays(now, -30), "yyyy-MM-dd HH:mm:ss"));
        StockPdQueryResponse response = stockPdAPI.search(request, new Pager(10, 0, true));

        for (StockPdQueryResponse.Item item : response.getData())
        {
            System.out.println("goods no:" + item.getOrderNo() + " modified : " + item.getModified());

        }
    }
    private static void queryDetailTest(StockPdAPI stockPdAPI) throws WdtErpException
    {//PD2023041906
        StockPdQueryDetailRequest request = new StockPdQueryDetailRequest();
        request.setPdNo("PD2023041906");
        StockPdQueryDetailResponse response = stockPdAPI.search(request, new Pager(10, 0, true));

        for (StockPdQueryDetailResponse.DetailItem item : response.getOrders())
        {
            System.out.println("goods no:" + item.getGoodsNo() + " modified : " + item.getModified());
        }
    }
}
