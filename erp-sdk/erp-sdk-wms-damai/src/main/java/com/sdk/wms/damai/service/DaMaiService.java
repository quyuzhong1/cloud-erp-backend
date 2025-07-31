package com.sdk.wms.damai.service;

import com.alibaba.fastjson.TypeReference;
import com.common.business.constant.BusinessCommonConstants;
import com.common.core.utils.OkHttpUtils;
import com.sdk.wms.damai.dto.response.DaMaiBaseResp;
import com.sdk.wms.damai.dto.response.DaMaiWarehouseResp;
import com.sdk.wms.damai.utils.DaMaiUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Component
public class DaMaiService {

    private String getPreUrl(){
        if (BusinessCommonConstants.hasProfile("prod")) {
            return "";
        } else {
            return "https://uatoms.greatsell.cn";
        }
    }

    public static void main(String[] args) {
        Map<String,String> headerMap = new HashMap<>();
        headerMap.put("appToken","5cdf2a88fc91cbc2c7befa959a6f0c0a");
        headerMap.put("appKey","68cb7beaeaf5f3caac5bba16a632ca13");
        String bodyStr = OkHttpUtils.doPostJson("https://uatoms.greatsell.cn/omsService/non/baseApi/getWarehouse", new HashMap<>(), headerMap);
        System.out.println(bodyStr);
    }

    /**
     * 查询仓库
     * @param authMap
     * @return
     */
    public DaMaiBaseResp<List<DaMaiWarehouseResp>> getWarehouseList(Map<String,Object> authMap){
        String url = "/omsService/non/baseApi/getWarehouse";
        Map<String, String> headerMap = buildHearderMap(authMap);
        Map<String, Object> bodyMap = new HashMap<>();
        String bodyStr = OkHttpUtils.doPostJson(getPreUrl() + url, bodyMap, headerMap);
        return DaMaiUtils.parseToJiFengResp(bodyStr, new TypeReference<DaMaiBaseResp<List<DaMaiWarehouseResp>>>() {});
    }

    private Map<String, String> buildHearderMap(Map<String, Object> authMap) {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("appToken", authMap.get("appToken").toString());
        headerMap.put("appKey", authMap.get("appKey").toString());
        return headerMap;
    }
}
