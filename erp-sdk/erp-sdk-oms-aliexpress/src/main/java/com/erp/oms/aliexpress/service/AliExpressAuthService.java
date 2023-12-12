package com.erp.oms.aliexpress.service;

import com.alibaba.fastjson.JSONObject;
import com.erp.oms.aliexpress.api.IopClient;
import com.erp.oms.aliexpress.api.IopClientImpl;
import com.erp.oms.aliexpress.api.IopRequest;
import com.erp.oms.aliexpress.api.IopResponse;
import com.erp.oms.aliexpress.constants.AliexpressConstants;
import com.erp.oms.aliexpress.enums.Protocol;
import com.erp.oms.aliexpress.util.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 速卖通订单服务
 *
 * @author yl
 * @date 2023-11-22
 */
@Slf4j
@Component
public class AliExpressAuthService {


    public JSONObject generateToken(Map<String, String> map) throws ApiException {
        String appKey = map.getOrDefault("clientId", "");
        String appSecret = map.getOrDefault("clientSecret", "");
        String code = map.getOrDefault("code", "");
        String baseUrl = map.getOrDefault("baseUrl", "");
        IopClient client = new IopClientImpl(baseUrl, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName(AliexpressConstants.TOKEN_CREATE);
        request.addApiParameter("code", code);
        IopResponse response = client.execute(request, Protocol.GOP);
        return JSONObject.parseObject(response.getBody());

    }

    public JSONObject RefreshToken() throws ApiException {
        return null;
    }


    public static void main(String[] args) {
        Map<String, String> map = new HashMap<>();
        map.put("clientId", "503630");
        map.put("clientSecret", "PxkJJ2fLGh5HcwzhUJp267lQSbkuAFRJ");
        map.put("code", "3_503630_q96LGrFAaP3qexDAZwkt37th3201");
        map.put("baseUrl", "https://api-sg.aliexpress.com");
        AliExpressAuthService service = new AliExpressAuthService();
        JSONObject jsonObject = null;
        try {
            jsonObject = service.generateToken(map);
        } catch (ApiException e) {
            e.printStackTrace();
        }
        System.out.println(jsonObject);
    }
}


