package com.erp.oms.aliexpress.service;

import com.erp.oms.aliexpress.api.IopClient;
import com.erp.oms.aliexpress.api.IopClientImpl;
import com.erp.oms.aliexpress.api.IopRequest;
import com.erp.oms.aliexpress.api.IopResponse;
import com.erp.oms.aliexpress.enums.Protocol;
import com.erp.oms.aliexpress.util.ApiException;

public class AliExpressDliveryOrderService {

    public static void main(String[] args) throws ApiException {


        String appKey = "502978";
        String appSecret = "DfFGCAXMY7pptKfhz7IkWEa0zC0xddhY";
        String baseUrl = "https://api-sg.aliexpress.com";
//        String apiName = AliexpressConstants.DECLARE_DELIVER;
//        String token = "500002000383xXYuTpfDpvgviHHR2uUB9yHxEIwiRSF7Dgx9Mz12af849325O8FaLsaz";
//        IopClient client = new IopClientImpl(baseUrl, appKey, appSecret);
//        IopRequest request = new IopRequest();
//        request.addApiParameter("simplify", "true");
//        request.addApiParameter("country", "123");
//        request.addApiParameter("warehouseCustomerId", "123");
//
//        request.setApiName("/qimen/aliexpress/warehouse/baseinfo/get");
//        IopResponse response = client.execute(request, Protocol.GOP);
//        String body = response.getBody();
//        System.out.println(body);

        IopClient client = new IopClientImpl(baseUrl, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName("/qimen/aliexpress/warehouse/baseinfo/get");
        request.addApiParameter("country", "123");
        request.addApiParameter("warehouseCustomerId", "123");
        request.addApiParameter("systemType", "oms");
        request.setHttpMethod("GET");
        IopResponse response = client.execute(request, Protocol.GOP);
        System.out.println(response.getBody());

    }
}
