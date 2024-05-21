package cn.wangdian.erp.demo;

import cn.wangdian.erp.sdk.Client;
import cn.wangdian.erp.sdk.WdtErpException;
import cn.wangdian.erp.sdk.api.wms.stockin.StockinAPI;
import cn.wangdian.erp.sdk.api.wms.stockin.dto.CreateOtherStockinRequest;
import cn.wangdian.erp.sdk.api.wms.stockin.dto.CreateOtherStockinResponse;
import cn.wangdian.erp.sdk.impl.ApiFactory;
import cn.wangdian.erp.sdk.impl.DefaultClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateStockin
{
    public static void main(String[] args) throws WdtErpException
    {
        //        Client client = DefaultClient.get("wdtapi3", "test", "test");
        Client client = DefaultClient.get("wdterp30", "http://192.168.2.194:30000/", "spw", "5ac3c8376bac90d8ffef3379e4f3627e:5cfaddbc0e08710d8b1f92f2cdb0b6e3");
        StockinAPI stockinAPI = ApiFactory.get(client, StockinAPI.class);
        refundStockin(stockinAPI);
    }

    private static CreateOtherStockinResponse refundStockin(StockinAPI stockinAPI) throws WdtErpException
    {
        CreateOtherStockinRequest request = new CreateOtherStockinRequest();
        CreateOtherStockinRequest.GoodsList inrequest1 = new CreateOtherStockinRequest.GoodsList();
        CreateOtherStockinRequest.GoodsList inrequest2 = new CreateOtherStockinRequest.GoodsList();
        request.setWarehouseNo("lzx");
        request.setRemark("测试1");
        request.setReason("1");
        inrequest1.setNum(BigDecimal.valueOf(20));
        Map<String, Object> map2 = new HashMap<String, Object>();
        inrequest2.setNum(BigDecimal.valueOf(60));
        List<CreateOtherStockinRequest.GoodsList> list=new ArrayList<>();
        list.add(inrequest1);
        list.add(inrequest2);
        request.setGoodsList(list);

        CreateOtherStockinResponse response = stockinAPI.createOtherOrder(request);

        return response;
    }
}
