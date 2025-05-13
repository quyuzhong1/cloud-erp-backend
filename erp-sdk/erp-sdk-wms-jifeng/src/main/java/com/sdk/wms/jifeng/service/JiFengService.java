package com.sdk.wms.jifeng.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.common.core.utils.OkHttpUtils;
import com.sdk.wms.jifeng.dto.request.JiFengAuthRequest;
import com.sdk.wms.jifeng.dto.response.*;
import com.sdk.wms.jifeng.utils.JiFengUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;


@Slf4j
@Component
public class JiFengService {

    public static void main(String[] args) {
        JiFengService jiFengService = new JiFengService();
        Map<String,Object> authMap = new HashMap<>();
        authMap.put("domain","sureparcel");
        authMap.put("accessToken","63e18ce9b4dd4f71a66efd07feb972e5");
        authMap.put("appKey","a03b35bf7f0c4c4f8e23e0599b5be649");
        authMap.put("userId","7471");
        authMap.put("appToken","f9af8dc7afea488991a216485987746c");

        JiFengBaseResp<List<JiFengOnlineChannelResp.RowsDTO>>  jiFengBaseResp = jiFengService.getOnlineChannel(authMap);
        System.out.println(jiFengBaseResp.getData().size());
    }
//    public static void main(String[] args) {
//        String clientId = "a03b35bf7f0c4c4f8e23e0599b5be649";
//        String clientSecret = "f9af8dc7afea488991a216485987746c";
//        String email = "wuliubu@ulanzi.cn";
//        String token = "fbcea272c37b43d59376a48d0be197cd";
//        String url = "sureparcel";
//        String refreshToken = "9cf835a0f73247b787857fb118a9514f";
//        Integer userId = 7471;
//        JiFengService jiFengService = new JiFengService();
//        JiFengBaseResp<JiFengTokenResp> resp = jiFengService.refreshToken(JiFengAuthRequest.builder().userId(userId).refreshToken(refreshToken).token(token).email(email).domain(url).clientId(clientId).clientSecret(clientSecret).key("vLdBchPpgi").build());
//        System.out.println(resp);
//    }

    public JiFengBaseResp<String> authorize(JiFengAuthRequest jiFengAuthRequest){
        String path = "/api/oauth/authorize";
        String url = getUrl(jiFengAuthRequest.getDomain());

        Map<String, Object> param = new HashMap<>();
        param.put("clientId",jiFengAuthRequest.getClientId());
        param.put("email",jiFengAuthRequest.getEmail());
        param.put("token",jiFengAuthRequest.getToken());
        param.put("domain",url);
        String bodyStr = OkHttpUtils.doGet(url+path, param, new HashMap<>());
        return JiFengUtils.parseToJiFengResp(bodyStr, String.class);
    }

    public JiFengBaseResp<JiFengTokenResp> accessToken(JiFengAuthRequest jiFengAuthRequest){
        String path = "/api/oauth/accessToken";
        String url = getUrl(jiFengAuthRequest.getDomain());

        Map<String, Object> param = new HashMap<>();
        param.put("clientId",jiFengAuthRequest.getClientId());
        param.put("clientSecret",jiFengAuthRequest.getClientSecret());
        param.put("key",jiFengAuthRequest.getKey());
        String bodyStr = OkHttpUtils.doGet(url+path, param, new HashMap<>());
        return JiFengUtils.parseToJiFengResp(bodyStr, JiFengTokenResp.class);
    }

    /**
     * 刷新token
     * @param jiFengAuthRequest
     * @return
     */
    public JiFengBaseResp<JiFengTokenResp> refreshToken(JiFengAuthRequest jiFengAuthRequest){
        String path = "/api/oauth/refreshToken";
        String url = getUrl(jiFengAuthRequest.getDomain());

        Map<String, Object> param = new HashMap<>();
        param.put("clientId",jiFengAuthRequest.getClientId());
        param.put("clientSecret",jiFengAuthRequest.getClientSecret());
        param.put("refreshToken",jiFengAuthRequest.getRefreshToken());
        param.put("userId",jiFengAuthRequest.getUserId());
        String bodyStr = OkHttpUtils.doGet(url+path, param, new HashMap<>());
        return JiFengUtils.parseToJiFengResp(bodyStr, JiFengTokenResp.class);
    }

    /**
     * 查询仓库
     * @param authMap
     * @return
     */
    public JiFengBaseResp<List<JiFengWarehouseResp>> getWarehouseList(Map<String,Object> authMap){
        String path = "/api/warehouse/getList";
        String url = getUrl(authMap.get("domain").toString());

        Map<String, String> headerMap = buildHearderMap(authMap, path);
        String bodyStr = OkHttpUtils.doPostJson(url+path, new HashMap<>(), headerMap);
        return JiFengUtils.parseToJiFengResp(bodyStr, new TypeReference<JiFengBaseResp<List<JiFengWarehouseResp>>>() {});
    }

    /**
     * 查询线下物流
     * @param authMap
     * @param warehouseCode
     * @return
     */
    public JiFengBaseResp<List<JiFengOfflineChannelResp>> getOfflineChannel(Map<String,Object> authMap, String warehouseCode){
        String path = "/api/logistics/offline/page";
        String url = getUrl(authMap.get("domain").toString());

        Map<String, String> headerMap = buildHearderMap(authMap, path);

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("code",warehouseCode);
        String bodyStr = OkHttpUtils.doPostJson(url+path, bodyMap, headerMap);
        return JiFengUtils.parseToJiFengResp(bodyStr, new TypeReference<JiFengBaseResp<List<JiFengOfflineChannelResp>>>() {});
    }


    /**
     * 查询线上物流
     * @param authMap
     * @return
     */
    public JiFengBaseResp<List<JiFengOnlineChannelResp.RowsDTO>> getOnlineChannel(Map<String,Object> authMap){
        String path = "/api/logistics/online/page";
        String url = getUrl(authMap.get("domain").toString());

        List<JiFengOnlineChannelResp.RowsDTO> allData = new ArrayList<>();
        int pageNo = 1;
        boolean hasMore = true;

        while (hasMore) {
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("pageNo", pageNo);
            bodyMap.put("pageSize", 300); // 每页大小，可根据实际情况调整

            Map<String, String> headerMap = buildHearderMap(authMap, path);
            String bodyStr = OkHttpUtils.doPostJson(url + path, bodyMap, headerMap);
            JiFengBaseResp<JiFengOnlineChannelResp> response = JiFengUtils.parseToJiFengResp(bodyStr,JiFengOnlineChannelResp.class);

            if (response.getCode() == 0) {
                JiFengOnlineChannelResp.PageDTO pageData = response.getData().getPage();
                if (pageData != null && pageData.getPageNo() != null) {
                    allData.addAll(pageData.getRows());

                    // 判断是否还有下一页
                    if (pageData.getRows().isEmpty()) {
                        hasMore = false;
                    } else {
                        pageNo++;
                    }
                } else {
                    hasMore = false;
                }
            } else {
                // 如果请求失败，直接返回错误信息
                JiFengBaseResp<List<JiFengOnlineChannelResp.RowsDTO>> errorResp = new JiFengBaseResp<>();
                errorResp.setCode(response.getCode());
                errorResp.setMessage(response.getMessage());
                errorResp.setRequestId(response.getRequestId());
                return errorResp;
            }
        }

        JiFengBaseResp<List<JiFengOnlineChannelResp.RowsDTO>> result = new JiFengBaseResp<>();
        result.setCode(0);
        result.setMessage("success");
        result.setData(allData);
        return result;
    }

    private Map<String, String> buildHearderMap(Map<String, Object> authMap, String path) {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("url", path);
        headerMap.put("method","post");
        headerMap.put("accessToken", authMap.get("accessToken").toString());
        headerMap.put("clientId", authMap.get("appKey").toString());
        headerMap.put("timestamp",System.currentTimeMillis()+"");
        headerMap.put("nonce", String.valueOf(ThreadLocalRandom.current().nextInt(10, 100)));
        headerMap.put("userId", authMap.get("userId").toString());
        headerMap.put("sign",JiFengUtils.sign(authMap.get("appToken").toString(),headerMap));
        return headerMap;
    }

    private String getUrl(String domain) {
        return "https://" + domain + ".jfwms.com";
    }
}
