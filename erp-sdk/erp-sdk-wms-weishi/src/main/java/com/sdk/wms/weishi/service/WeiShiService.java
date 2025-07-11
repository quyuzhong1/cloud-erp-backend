package com.sdk.wms.weishi.service;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.TypeReference;
import com.common.business.constant.BusinessCommonConstants;
import com.common.core.utils.OkHttpUtils;
import com.sdk.wms.weishi.dto.request.WeiShiBaseRequest;
import com.sdk.wms.weishi.dto.request.WeiShiProductRequest;
import com.sdk.wms.weishi.dto.response.WeiShiBaseResp;
import com.sdk.wms.weishi.dto.response.WeiShiProductResp;
import com.sdk.wms.weishi.dto.response.WeiShiTokenResp;
import com.sdk.wms.weishi.dto.response.WeiShiWarehouseResp;
import com.sdk.wms.weishi.utils.WeiShiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Component
public class WeiShiService {

    private final String preUrl = "http://218.17.123.141:808/prod-api";

    private final String api = "/omsapi/api";

    private final String apiUrl = preUrl + api;

    public static void main(String[] args) {
        WeiShiService weiShiService = new WeiShiService();
        Map<String,Object> authMap = new HashMap<>();
        authMap.put("appKey","613cefbb29a34ab5af3f26c3a04ff6a7");
        authMap.put("accessToken","769cf24c-3463-4374-b52d-b276b3051188");

        WeiShiBaseResp<List<WeiShiWarehouseResp>> weiShiBaseResp = weiShiService.getWarehouseList(authMap);
//        WeiShiBaseResp<WeiShiTokenResp> weiShiBaseResp = weiShiService.accessToken(authMap);
        System.out.println(JSONUtil.toJsonStr(weiShiBaseResp));

    }

    public WeiShiBaseResp<WeiShiTokenResp> accessToken(Map<String,Object> authMap){
        String path = "/omsapi/auth/omsLoginBySecretKey/" + authMap.get("appKey").toString();
        String bodyStr = OkHttpUtils.doGet(preUrl +path, new HashMap<>(), new HashMap<>());
        return WeiShiUtils.parseToJiFengResp(bodyStr, WeiShiTokenResp.class);
    }


    /**
     * 查询仓库
     * @param authMap
     * @return
     */
    public WeiShiBaseResp<List<WeiShiWarehouseResp>> getWarehouseList(Map<String,Object> authMap){
        String action = "getAllWarehouse";
        Map<String, String> headerMap = buildHearderMap(authMap);
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("action", action);
        bodyMap.put("data", "{}");
        String bodyStr = OkHttpUtils.doPostJson(apiUrl, bodyMap, headerMap);
        return WeiShiUtils.parseToJiFengResp(bodyStr, new TypeReference<WeiShiBaseResp<List<WeiShiWarehouseResp>>>() {});
    }

    /**
     * 查询产品
     * @return
     */
    public WeiShiBaseResp<List<WeiShiProductResp.ListDTO>> querySkuList(WeiShiProductRequest weiShiProductRequest){
        String action = "querySkuList";
        Map<String, String> headerMap = buildHearderMap(weiShiProductRequest.getAuthMap());

        int page = 1;
        int limit = 100;
        boolean hasMore = true;
        List<WeiShiProductResp.ListDTO> allData = new ArrayList<>();
        while (hasMore) {
            weiShiProductRequest.setPage(page);
            weiShiProductRequest.setLimit(limit);
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("action", action);
            bodyMap.put("data", JSONUtil.toJsonStr(weiShiProductRequest));
            String bodyStr = OkHttpUtils.doPostJson(apiUrl, bodyMap, headerMap);
            WeiShiBaseResp<List<WeiShiProductResp>> response = WeiShiUtils.parseToJiFengResp(bodyStr, new TypeReference<WeiShiBaseResp<List<WeiShiProductResp>>>() {});
            if (response.getCode() == 200) {
                List<WeiShiProductResp> respList = response.getData();
                if (respList == null || respList.isEmpty()) {
                    hasMore = false;
                    continue;
                }
                WeiShiProductResp weiShiProductResp = respList.get(0);
                if (weiShiProductResp != null && weiShiProductResp.getTotal() != null && CollectionUtils.isNotEmpty(weiShiProductResp.getList())) {
                    allData.addAll(weiShiProductResp.getList());
                    if (!BusinessCommonConstants.hasProfile("prod")){
                        weiShiProductResp.setTotal(500);
                    }
                    if (weiShiProductResp.getTotal() <= page * limit) {
                        hasMore = false;
                    } else {
                        page++;
                    }
                } else {
                    hasMore = false;
                }
            }else{
                // 如果请求失败，直接返回错误信息
                WeiShiBaseResp<List<WeiShiProductResp.ListDTO>> errorResp = new WeiShiBaseResp<>();
                errorResp.setCode(response.getCode());
                errorResp.setMsg(response.getMsg());
                errorResp.setSuccess(true);
                return errorResp;
            }
        }
        WeiShiBaseResp<List<WeiShiProductResp.ListDTO>> result = new WeiShiBaseResp<>();
        result.setCode(200);
        result.setData(allData);
        return result;
    }


    private Map<String, String> buildHearderMap(Map<String, Object> authMap) {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", authMap.get("accessToken").toString());
        return headerMap;
    }

}
