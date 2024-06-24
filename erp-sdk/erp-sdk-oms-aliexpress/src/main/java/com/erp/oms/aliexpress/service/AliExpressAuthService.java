package com.erp.oms.aliexpress.service;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.erp.oms.aliexpress.api.IopClient;
import com.erp.oms.aliexpress.api.IopClientImpl;
import com.erp.oms.aliexpress.api.IopRequest;
import com.erp.oms.aliexpress.api.IopResponse;
import com.erp.oms.aliexpress.constants.AliexpressConstants;
import com.erp.oms.aliexpress.dto.request.RefreshTokenRequest;
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

    /**
     * 刷新token
     *
     * @param refreshTokenRequest
     * @return
     * @author yl
     * @date 2023-12-12 14:45
     */
    public JSONObject RefreshToken(RefreshTokenRequest refreshTokenRequest) throws ApiException {
        String appKey = refreshTokenRequest.getClientId();
        String appSecret = refreshTokenRequest.getClientSecret();
        String refreshToken = refreshTokenRequest.getRefreshToken();
        String baseUrl = refreshTokenRequest.getBaseUrl();
        IopClient client = new IopClientImpl(baseUrl, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName(AliexpressConstants.REFRESH_TOKEN);
        request.addApiParameter("refresh_token", refreshToken);
        IopResponse response = client.execute(request, Protocol.GOP);

        return JSONObject.parseObject(response.getBody());
    }


    public static void main(String[] args) throws Exception {
//        AliExpressAuthService service = new AliExpressAuthService();
//        JSONObject jsonObject = null;
//        Map<String, String> map = new HashMap<>();
//        map.put("clientId", "502978");
//        map.put("clientSecret", "3_502978_YNyPkGTynMfcDkZYcOXO58qf299");
//        map.put("code", "3_502978_6mwqSBdkUNNnw3ncwnXkusoj252");
//        map.put("baseUrl", "https://api-sg.aliexpress.com");
//        try {
//            jsonObject= service.generateToken(map);
//            System.out.println(jsonObject);
//        } catch (ApiException e) {
//            e.printStackTrace();
//        }
//        RefreshTokenRequest request=RefreshTokenRequest.builder().
//                clientId("503630").
//                baseUrl("https://api-sg.aliexpress.com").
//                clientSecret("PxkJJ2fLGh5HcwzhUJp267lQSbkuAFRJ").
//                refreshToken("500012000313OXYuTpgfvQltEo9rxovDDagvcjqcxxE1707591adjETAlS25O8FaLsaz").
//                build();
//
//
//        try {
//            jsonObject = service.RefreshToken(request);
//        } catch (ApiException e) {
//            e.printStackTrace();
//        }
//        System.out.println(JSONUtil.toJsonStr(jsonObject));
        String url = "https://api-sg.aliexpress.com";
        String appKey = "502978";
        String appSecret = "DfFGCAXMY7pptKfhz7IkWEa0zC0xddhY";
        String accessToken = "50000200123dJAvRobgSKEtBJjvZtxEAZfV17b52f96gJQg0OG9CCvBqT1l8Mocp35cG";
        IopClient client = new IopClientImpl(url, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName(AliexpressConstants.ALIEXPRESS_MERCHANT_PROFILE_GET);
        request.addApiParameter("simplify","true");
        IopResponse response = client.execute(request, accessToken, Protocol.TOP);
        System.out.println(JSONUtil.toJsonStr(response));
    }
}


