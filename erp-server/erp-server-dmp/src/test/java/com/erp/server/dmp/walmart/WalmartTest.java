package com.erp.server.dmp.walmart;

import com.sdk.oms.walmart.api.WalmartStaticKey;
import com.sdk.oms.walmart.dto.walmart.WalmartTokenDTO;
import com.sdk.oms.walmart.service.WalmartSdkClientService;

import java.util.HashMap;

/**
 * @param
 * @author Jim
 * @date 2023/12/11
 * @Return
 */
public class WalmartTest {
    public static void main(String[] args) throws Exception {
        String baseUrl = "";
        String clientId = "2434a35c-7c42-4420-9618-0c179b68a8c2";
        String clientSecret = "AMW5lbVFqG2DMP4DuLezhSkbk4u0JLGUjdFlsrl_p0sagsBkYPPiQhRbEvkE4a6k6KXNKhB--RGlqPKIfhUoV28";
        //获取令牌
        baseUrl = WalmartStaticKey.baseUrl + "token";


        WalmartSdkClientService walmartSdkClientService = new WalmartSdkClientService();
        WalmartTokenDTO walmartTokenDTO = walmartSdkClientService.sendWalmartPostToken(baseUrl, clientId, clientSecret);
        System.out.println(walmartTokenDTO);
        //请求参数
        HashMap<String, Object> paramMap = new HashMap<>();
//        Integer pageSize = 50;
//        paramMap.put("limit", pageSize);
//        paramMap.put("lastModifiedStartDate", "2023-01-01T00:00:00");
//        paramMap.put("lastModifiedEndDate", "2023-12-27T00:00:00");
//        paramMap.put("createdStartDate", "2023-01-01T00:00:00");
//        paramMap.put("createdEndDate", "2023-12-27T00:00:00");
//        paramMap.put("status", "Acknowledged,Shipped");
//        paramMap.put("productInfo", "true");
//        WalmartTokenDTO s = walmartSdkClientService.sendWalmartPostToken(baseUrl, clientId, clientSecret);
        // https://marketplace.walmartapis.com/v3/
        baseUrl = WalmartStaticKey.baseUrl + "shipping/labels/carriers";
        String s = walmartSdkClientService.sendWalmartGet(baseUrl, clientId, clientSecret, walmartTokenDTO.getAccessToken(), paramMap);
        System.out.println(s);
        // {"carriers":[{"carrierId":"1000","shortName":"FedEx","carrierName":"FedEx"},{"carrierId":"1001","shortName":"USPS","carrierName":"USPS"}]}
    }
}
