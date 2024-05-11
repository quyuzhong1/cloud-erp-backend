package cn.wangdian.erp.demo;

import cn.wangdian.erp.sdk.Client;
import cn.wangdian.erp.sdk.api.wms.stockout.StockoutAPI;
import cn.wangdian.erp.sdk.api.wms.stockout.dto.CreateOtherStockoutRequest;
import cn.wangdian.erp.sdk.api.wms.stockout.dto.CreateOtherStockoutResponse;
import cn.wangdian.erp.sdk.impl.ApiFactory;
import cn.wangdian.erp.sdk.impl.DefaultClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CreateStockout
{
    public static void main(String[] args)
    {
        //        Client client = DefaultClient.get("wdtapi3", "test", "test");
        Client client = DefaultClient.get("wdterp30", "http://192.168.84.1:30000/openapi", "zyOther", "423c25002f36c7445ccd7742ea5d1be4:8f034c5a36d5749a438951cde1963f2a");
        StockoutAPI stockoutAPI = ApiFactory.get(client, StockoutAPI.class);
        refundStockOut(stockoutAPI);
    }

    public static CreateOtherStockoutResponse refundStockOut(StockoutAPI stockoutAPI)
    {
        CreateOtherStockoutRequest request = new CreateOtherStockoutRequest();
        CreateOtherStockoutRequest.GoodsList inrequest1 = new CreateOtherStockoutRequest.GoodsList();
        CreateOtherStockoutRequest.GoodsList inrequest2 = new CreateOtherStockoutRequest.GoodsList();
        request.setWarehouseNo("1001");
        request.setOuterNo("CG201911286258");
        request.setRemark("测试1");
        request.setReason("1");
        inrequest1.setSpecNo("sjdhlg");
        inrequest1.setPositionNo("J-3");
        inrequest1.setNum(BigDecimal.valueOf(3));
        List<CreateOtherStockoutRequest.GoodsList> list=new ArrayList<>();
        list.add(inrequest1);
        request.setGoodsList(list);

        CreateOtherStockoutResponse response = stockoutAPI.createOtherOutOrder(request);
        return response ;

    }
}
