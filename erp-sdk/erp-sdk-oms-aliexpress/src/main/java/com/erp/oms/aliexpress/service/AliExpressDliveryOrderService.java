package com.erp.oms.aliexpress.service;

import com.alibaba.fastjson.JSONObject;
import com.common.core.exception.ServiceException;
import com.erp.oms.aliexpress.api.IopClient;
import com.erp.oms.aliexpress.api.IopClientImpl;
import com.erp.oms.aliexpress.api.IopRequest;
import com.erp.oms.aliexpress.api.IopResponse;
import com.erp.oms.aliexpress.constants.AliexpressConstants;
import com.erp.oms.aliexpress.enums.Protocol;
import com.erp.oms.aliexpress.util.ApiException;
import io.seata.common.util.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AliExpressDliveryOrderService {

    public IopResponse getDelivery(Map<String, String> authMap, List<String> orderIds) throws ApiException {
        String appKey = authMap.get("clientId");
        String appSecret = authMap.get("clientSecret");
        String token = authMap.get("token");
        String url = authMap.get("url");
        if (StringUtils.isBlank(url)){
            url = AliexpressConstants.BASE_URL;
        }
        validate(appKey,appSecret,token,url);
        IopClient client = new IopClientImpl(url, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName(AliexpressConstants.ALIEXPRESS_ASCP_FFO_QUERY);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("biz_type", 288000);
        paramMap.put("customer_order_number_list", orderIds);
        request.addApiParameter("customer_order_number_list", JSONObject.toJSONString(paramMap));
        IopResponse response = client.execute(request, token, Protocol.TOP);
        return response;
//        return JSONObject.parseObject(response.getBody(), LabelResult.class);
    }

    private void validate(String appKey,String appSecret,String token,String url){
        if (StringUtils.isBlank(appKey) || StringUtils.isBlank(appSecret) || StringUtils.isBlank(token) || StringUtils.isBlank(token) ) throw new ServiceException("授权信息不能为空");
    }

    public static void main(String[] args) throws ApiException {
        String appKey = "502978";
        String appSecret = "DfFGCAXMY7pptKfhz7IkWEa0zC0xddhY";
        String baseUrl = "https://api-sg.aliexpress.com";
        String token = "50000700205zwXSma1e2c8625cUveuFpAPxMtBCci0HmUhuSd9BfP4kTtgICMZMOGaag";
        IopClient client = new IopClientImpl(baseUrl, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName(AliexpressConstants.ALIEXPRESS_ASCP_FFO_QUERY);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("biz_type", 288000);
        paramMap.put("page_index", 5);
        paramMap.put("page_size", 20);
        paramMap.put("customer_order_number_list", Arrays.asList(""));
        request.addApiParameter("fulfillment_forward_order_query", JSONObject.toJSONString(paramMap));
        IopResponse response = client.execute(request, token, Protocol.TOP);
        System.out.println(response.getBody());

    }
}
