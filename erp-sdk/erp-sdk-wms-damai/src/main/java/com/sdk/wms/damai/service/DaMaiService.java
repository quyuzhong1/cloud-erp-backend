package com.sdk.wms.damai.service;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.TypeReference;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.utils.OkHttpUtils;
import com.sdk.wms.damai.dto.request.DaMaiCancelInboundRequest;
import com.sdk.wms.damai.dto.request.DaMaiCreateInboundRequest;
import com.sdk.wms.damai.dto.request.DaMaiInventoryAgeRequest;
import com.sdk.wms.damai.dto.request.DaMaiInventoryTransRequest;
import com.sdk.wms.damai.dto.response.*;
import com.sdk.wms.damai.utils.DaMaiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
        headerMap.put("appToken","5cdf2a88fc91cb2c7befa959a6f0c0a");
        headerMap.put("appKey","68cb7beaeaf5f3caac5bba16a632ca13");
        Map<String,Object> body = new HashMap<>();
        body.put("limit",300);
        body.put("page",1);
        String bodyStr = OkHttpUtils.doPostJson("https://uatoms.greatsell.cn/omsService/non/skuApi/getSkuList",body, headerMap);
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
        return DaMaiUtils.parseToResp(bodyStr, new TypeReference<DaMaiBaseResp<List<DaMaiWarehouseResp>>>() {});
    }

    /**
     * 查询SKU
     * @param authMap
     * @return
     */
    public DaMaiPageBaseResp<List<DaMaiSkuResp>> getSkuList(Map<String,Object> authMap){
        String url = "/omsService/non/skuApi/getSkuList";
        Map<String, String> headerMap = buildHearderMap(authMap);
        int page = 1;
        int limit = 100;
        boolean hasMore = true;
        List<DaMaiSkuResp> allData = new ArrayList<>();
        while (hasMore) {
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("limit",limit);
            bodyMap.put("page",page);
            String bodyStr = OkHttpUtils.doPostJson(getPreUrl() + url, bodyMap, headerMap);
            DaMaiPageBaseResp<List<DaMaiSkuResp>> response = DaMaiUtils.parsePageToResp(bodyStr, new TypeReference<DaMaiPageBaseResp<List<DaMaiSkuResp>>>() {});
            if (StringUtils.isBlank(response.getMsg())) {
                List<DaMaiSkuResp> respList = response.getData();
                if (respList == null || respList.isEmpty()) {
                    hasMore = false;
                    continue;
                }
                if (response.getCount() != null ) {
                    allData.addAll(respList);
                    if (response.getCount() <= page * limit) {
                        hasMore = false;
                    } else {
                        page++;
                    }
                } else {
                    hasMore = false;
                }
            }else{
                // 如果请求失败，直接返回错误信息
                DaMaiPageBaseResp<List<DaMaiSkuResp>> errorResp = new DaMaiPageBaseResp<>();
                errorResp.setMsg(response.getMsg());
                return errorResp;
            }
        }
        DaMaiPageBaseResp<List<DaMaiSkuResp>> errorResp = new DaMaiPageBaseResp<>();
        errorResp.setMsg("");
        errorResp.setData(allData);
        return errorResp;
    }

    /**
     * 创建入库单
     * @param authMap
     * @return
     */
    public DaMaiBaseResp<DaMaiCreateInboundResp> createInbound(Map<String,Object> authMap, DaMaiCreateInboundRequest daMaiCreateInboundRequest){
        String path = "/omsService/non/asnAnApi/createAsnAn";
        Map<String, String> headerMap = buildHearderMap(authMap);
        ThirdWarehouseContext.setRequestJson(JSONUtil.toJsonStr(daMaiCreateInboundRequest));
        String bodyStr = OkHttpUtils.doPostJson(getPreUrl() + path, JSONUtil.toJsonStr(daMaiCreateInboundRequest), headerMap);
        ThirdWarehouseContext.setResponseJson(bodyStr);
        DaMaiBaseResp<DaMaiCreateInboundResp> response = DaMaiUtils.parseToResp(bodyStr,DaMaiCreateInboundResp.class);
        return response;
    }

    /**
     * 取消入库单
     * @param authMap
     * @return
     */
    public DaMaiBaseResp<String> cancelInbound(Map<String,Object> authMap, DaMaiCancelInboundRequest daMaiCancelInboundRequest){
        String path = "/omsService/non/asnAnApi/cancelAsnAn";
        Map<String, String> headerMap = buildHearderMap(authMap);
        ThirdWarehouseContext.setRequestJson(JSONUtil.toJsonStr(daMaiCancelInboundRequest));
        String bodyStr = OkHttpUtils.doPostJson(getPreUrl() + path, JSONUtil.toJsonStr(daMaiCancelInboundRequest), headerMap);
        ThirdWarehouseContext.setResponseJson(bodyStr);
        DaMaiBaseResp<String> response = DaMaiUtils.parseToResp(bodyStr,String.class);
        return response;
    }
    /**
     * 库存流水
     * @param authMap
     * @return
     */
    public DaMaiPageBaseResp<List<DaMaiInventoryTransResp>> getInventoryTrans(Map<String,Object> authMap, DaMaiInventoryTransRequest daMaiInventoryTransRequest){
        String url = "/omsService/non/skuInvApi/getSkuInvTrans";
        Map<String, String> headerMap = buildHearderMap(authMap);
        int page = 1;
        int limit = 100;
        boolean hasMore = true;
        List<DaMaiInventoryTransResp> allData = new ArrayList<>();
        while (hasMore) {
            daMaiInventoryTransRequest.setLimit(limit);
            daMaiInventoryTransRequest.setPage(page);
            String bodyStr = OkHttpUtils.doPostJson(getPreUrl() + url, JSONUtil.toJsonStr(daMaiInventoryTransRequest), headerMap);
            DaMaiPageBaseResp<List<DaMaiInventoryTransResp>> response = DaMaiUtils.parsePageToResp(bodyStr, new TypeReference<DaMaiPageBaseResp<List<DaMaiInventoryTransResp>>>() {});
            if (StringUtils.isBlank(response.getMsg())) {
                List<DaMaiInventoryTransResp> respList = response.getData();
                if (respList == null || respList.isEmpty()) {
                    hasMore = false;
                    continue;
                }
                if (response.getCount() != null ) {
                    allData.addAll(respList);
                    if (response.getCount() <= page * limit) {
                        hasMore = false;
                    } else {
                        page++;
                    }
                } else {
                    hasMore = false;
                }
            }else{
                // 如果请求失败，直接返回错误信息
                DaMaiPageBaseResp<List<DaMaiInventoryTransResp>> errorResp = new DaMaiPageBaseResp<>();
                errorResp.setMsg(response.getMsg());
                return errorResp;
            }
        }
        DaMaiPageBaseResp<List<DaMaiInventoryTransResp>> errorResp = new DaMaiPageBaseResp<>();
        errorResp.setMsg("");
        errorResp.setData(allData);
        return errorResp;
    }

    /**
     * 批次库龄
     * @param authMap
     * @return
     */
    public DaMaiBaseResp<String> getInventoryAge(Map<String,Object> authMap, DaMaiInventoryAgeRequest daMaiInventoryAgeRequest){
        String path = "/omsService/non/skuInvApi/getSkuBatchInv";
        Map<String, String> headerMap = buildHearderMap(authMap);
        String bodyStr = OkHttpUtils.doPostJson(getPreUrl() + path, JSONUtil.toJsonStr(daMaiInventoryAgeRequest), headerMap);
        DaMaiBaseResp<String> response = DaMaiUtils.parseToResp(bodyStr,String.class);
        return response;
    }

    private Map<String, String> buildHearderMap(Map<String, Object> authMap) {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("appToken", authMap.get("appToken").toString());
        headerMap.put("appKey", authMap.get("appKey").toString());
        return headerMap;
    }
}
