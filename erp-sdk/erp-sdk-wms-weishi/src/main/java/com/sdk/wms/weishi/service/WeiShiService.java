package com.sdk.wms.weishi.service;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.TypeReference;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.utils.OkHttpUtils;
import com.sdk.wms.weishi.dto.request.*;
import com.sdk.wms.weishi.dto.response.*;
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

    private final String preUrl = "https://test8.toms.360lion.com:2444";

    private final String api = "/prod-api/omsapi/api";

    private final String apiUrl = preUrl + api;

    public static void main(String[] args) {
//        WeiShiService weiShiService = new WeiShiService();
//        Map<String,Object> authMap = new HashMap<>();
//        authMap.put("appKey","613cefbb29a34ab5af3f26c3a04ff6a7");
//        authMap.put("accessToken","769cf24c-3463-4374-b52d-b276b3051188");
//
//        WeiShiBaseResp<List<WeiShiWarehouseResp>> weiShiBaseResp = weiShiService.getWarehouseList(authMap);
////        WeiShiBaseResp<WeiShiTokenResp> weiShiBaseResp = weiShiService.accessToken(authMap);
//        System.out.println(JSONUtil.toJsonStr(weiShiBaseResp));
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("action", "1232");
        bodyMap.put("version", "2.0");
        WeiShiCreateOutboundRequest weiShiCreateOutboundRequest = new WeiShiCreateOutboundRequest();
        weiShiCreateOutboundRequest.setLabelFile("1231");
        bodyMap.put("data", JSONUtil.toJsonStr(weiShiCreateOutboundRequest));
        System.out.println(JSONUtil.toJsonStr(bodyMap));
    }

    public WeiShiBaseResp<WeiShiTokenResp> accessToken(Map<String,Object> authMap){
        String path = "/prod-api/omsapi/auth/omsLoginBySecretKey/" + authMap.get("appKey").toString();
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


    /**
     * 查询库存
     * @return
     */
    public WeiShiBaseResp<List<WeiShiStockResp.DataDTO.ListDTO>> getStockAll(WeiShiStockRequest weiShiStockRequest){
        String action = "getStockAll";
        Map<String, String> headerMap = buildHearderMap(weiShiStockRequest.getAuthMap());

        int page = 1;
        int limit = 100;
        boolean hasMore = true;
        List<WeiShiStockResp.DataDTO.ListDTO> allData = new ArrayList<>();
        while (hasMore) {
            weiShiStockRequest.setPage(page);
            weiShiStockRequest.setLimit(limit);
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("action", action);
            bodyMap.put("data", JSONUtil.toJsonStr(weiShiStockRequest));
            String bodyStr = OkHttpUtils.doPostJson(apiUrl, bodyMap, headerMap);
            WeiShiBaseResp<WeiShiStockResp> response = WeiShiUtils.parseToJiFengResp(bodyStr, new TypeReference<WeiShiBaseResp<WeiShiStockResp>>() {});
            if (response.getCode() == 200) {
                List<WeiShiStockResp.DataDTO.ListDTO> respList = response.getData().getData().getList();
                if (respList == null || respList.isEmpty()) {
                    hasMore = false;
                    continue;
                }
                WeiShiStockResp.DataDTO weiShiStockResp = response.getData().getData();
                if (weiShiStockResp.getTotal() != null && CollectionUtils.isNotEmpty(weiShiStockResp.getList())) {
                    allData.addAll(respList);
                    if (!BusinessCommonConstants.hasProfile("prod")){
                        weiShiStockResp.setTotal(500);
                    }
                    if (weiShiStockResp.getTotal() <= page * limit) {
                        hasMore = false;
                    } else {
                        page++;
                    }
                } else {
                    hasMore = false;
                }
            }else{
                // 如果请求失败，直接返回错误信息
                WeiShiBaseResp<List<WeiShiStockResp.DataDTO.ListDTO>> errorResp = new WeiShiBaseResp<>();
                errorResp.setCode(response.getCode());
                errorResp.setMsg(response.getMsg());
                errorResp.setSuccess(true);
                return errorResp;
            }
        }
        WeiShiBaseResp<List<WeiShiStockResp.DataDTO.ListDTO>> result = new WeiShiBaseResp<>();
        result.setCode(200);
        result.setData(allData);
        return result;
    }

    /**
     * 查询库龄
     * @return
     */
    public WeiShiBaseResp<List<WeiShiStockAgeResp.ListDTO>> queryStockSkuAgeList(WeiShiStockAgeRequest weiShiStockAgeRequest){
        String action = "queryStockSkuAgeList";
        Map<String, String> headerMap = buildHearderMap(weiShiStockAgeRequest.getAuthMap());

        int page = 1;
        int limit = 100;
        boolean hasMore = true;
        List<WeiShiStockAgeResp.ListDTO> allData = new ArrayList<>();
        while (hasMore) {
            weiShiStockAgeRequest.setPage(page);
            weiShiStockAgeRequest.setLimit(limit);
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("action", action);
            bodyMap.put("data", JSONUtil.toJsonStr(weiShiStockAgeRequest));
            String bodyStr = OkHttpUtils.doPostJson(apiUrl, bodyMap, headerMap);
            WeiShiBaseResp<WeiShiStockAgeResp> response = WeiShiUtils.parseToJiFengResp(bodyStr, new TypeReference<WeiShiBaseResp<WeiShiStockAgeResp>>() {});
            if (response.getCode() == 200) {
                List<WeiShiStockAgeResp.ListDTO> respList = response.getData().getList();
                if (respList == null || respList.isEmpty()) {
                    hasMore = false;
                    continue;
                }
                WeiShiStockAgeResp weiShiStockResp = response.getData();
                if (weiShiStockResp.getTotal() != null && CollectionUtils.isNotEmpty(weiShiStockResp.getList())) {
                    allData.addAll(respList);
                    if (!BusinessCommonConstants.hasProfile("prod")){
                        weiShiStockResp.setTotal(500);
                    }
                    if (weiShiStockResp.getTotal() <= page * limit) {
                        hasMore = false;
                    } else {
                        page++;
                    }
                } else {
                    hasMore = false;
                }
            }else{
                // 如果请求失败，直接返回错误信息
                WeiShiBaseResp<List<WeiShiStockAgeResp.ListDTO>> errorResp = new WeiShiBaseResp<>();
                errorResp.setCode(response.getCode());
                errorResp.setMsg(response.getMsg());
                errorResp.setSuccess(true);
                return errorResp;
            }
        }
        WeiShiBaseResp<List<WeiShiStockAgeResp.ListDTO>> result = new WeiShiBaseResp<>();
        result.setCode(200);
        result.setData(allData);
        return result;
    }
    /**
     * 创建入库单
     * @return
     */
    public WeiShiBaseResp<String> createInbound(WeiShiCreateInboundRequest weiShiCreateInboundRequest,Map<String,Object> authMap){
        String action = "createInboundOrder";
        Map<String, String> headerMap = buildHearderMap(authMap);
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("action", action);
        bodyMap.put("data", JSONUtil.toJsonStr(weiShiCreateInboundRequest));
        log.warn("纬狮创建入库单请求参数: {}", JSONUtil.toJsonStr(weiShiCreateInboundRequest));
        String bodyStr = OkHttpUtils.doPostJson(apiUrl, bodyMap, headerMap);
        ThirdWarehouseContext.setRequestJson(JSONUtil.toJsonStr(bodyMap));
        ThirdWarehouseContext.setResponseJson(bodyStr);
        return WeiShiUtils.parseToJiFengResp(bodyStr, new TypeReference<WeiShiBaseResp<String>>() {});
    }

    /**
     * 更新入库单
     * @return
     */
    public WeiShiBaseResp<String> updateInbound(WeiShiCreateInboundRequest weiShiCreateInboundRequest,Map<String,Object> authMap){
        String action = "updateInboundOrder";
        Map<String, String> headerMap = buildHearderMap(authMap);
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("action", action);
        bodyMap.put("data", JSONUtil.toJsonStr(weiShiCreateInboundRequest));
        log.warn("纬狮更新入库单请求参数: {}", JSONUtil.toJsonStr(weiShiCreateInboundRequest));
        String bodyStr = OkHttpUtils.doPostJson(apiUrl, bodyMap, headerMap);
        ThirdWarehouseContext.setRequestJson(JSONUtil.toJsonStr(bodyMap));
        ThirdWarehouseContext.setResponseJson(bodyStr);
        return WeiShiUtils.parseToJiFengResp(bodyStr, new TypeReference<WeiShiBaseResp<String>>() {});
    }

    /**
     * 取消入库单
     * @return
     */
    public WeiShiBaseResp<String> cancelInbound(WeiShiCancelInboundRequest weiShiCancelInboundRequest,Map<String,Object> authMap){
        String action = "cancelInboundOrder";
        Map<String, String> headerMap = buildHearderMap(authMap);
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("action", action);
        bodyMap.put("data", JSONUtil.toJsonStr(weiShiCancelInboundRequest));
        log.warn("纬狮取消入库单请求参数: {}", JSONUtil.toJsonStr(weiShiCancelInboundRequest));
        String bodyStr = OkHttpUtils.doPostJson(apiUrl, bodyMap, headerMap);
        ThirdWarehouseContext.setRequestJson(JSONUtil.toJsonStr(bodyMap));
        ThirdWarehouseContext.setResponseJson(bodyStr);
        return WeiShiUtils.parseToJiFengResp(bodyStr, new TypeReference<WeiShiBaseResp<String>>() {});
    }

    /**
     * 查询入库单
     * @return
     */
    public WeiShiBaseResp<WeiShiInboundResp> getInbound(WeiShiQueryInboundRequest weiShiCancelInboundRequest,Map<String,Object> authMap){
        String action = "getInboundOrderDetails";
        Map<String, String> headerMap = buildHearderMap(authMap);
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("action", action);
        bodyMap.put("data", JSONUtil.toJsonStr(weiShiCancelInboundRequest));
        log.warn("纬狮查询入库单请求参数: {}", JSONUtil.toJsonStr(bodyMap));
        String bodyStr = OkHttpUtils.doPostJson(apiUrl, bodyMap, headerMap);
        return WeiShiUtils.parseToJiFengResp(bodyStr, new TypeReference<WeiShiBaseResp<WeiShiInboundResp>>() {});
    }

    /**
     * 查询物流产品
     * @return
     */
    public WeiShiBaseResp<List<WeiShiChannelResp>> getLogisticProductList(WeiShiLogisticProductRequest weiShiCancelInboundRequest,Map<String,Object> authMap){
        String action = "getProductList";
        Map<String, String> headerMap = buildHearderMap(authMap);
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("action", action);
        bodyMap.put("data", JSONUtil.toJsonStr(weiShiCancelInboundRequest));
        String bodyStr = OkHttpUtils.doPostJson(apiUrl, bodyMap, headerMap);
        return WeiShiUtils.parseToJiFengResp(bodyStr, new TypeReference<WeiShiBaseResp<List<WeiShiChannelResp>>>() {});
    }


    /**
     * 创建出库单
     * @return
     */
    public WeiShiBaseResp<WeiShiCreateOutboundResp> createOutbound(WeiShiCreateOutboundRequest weiShiCreateOutboundRequest,Map<String,Object> authMap){
        String action = "createSkuOrder";
        Map<String, String> headerMap = buildHearderMap(authMap);
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("action", action);
        bodyMap.put("data", JSONUtil.toJsonStr(weiShiCreateOutboundRequest));
        bodyMap.put("version", "2.0");
        log.warn("纬狮创建出库单请求参数: {}", JSONUtil.toJsonStr(weiShiCreateOutboundRequest));
        String bodyStr = OkHttpUtils.doPostJson(apiUrl, bodyMap, headerMap);
        ThirdWarehouseContext.setRequestJson(JSONUtil.toJsonStr(bodyMap));
        ThirdWarehouseContext.setResponseJson(bodyStr);
        return WeiShiUtils.parseToJiFengResp(bodyStr, new TypeReference<WeiShiBaseResp<WeiShiCreateOutboundResp>>() {});
    }

    /**
     * 取消出库单
     * @return
     */
    public WeiShiBaseResp<String> cancelOutbound(WeiShiCancelOutboundRequest weiShiCancelOutboundRequest,Map<String,Object> authMap){
        String action = "cancelSkuOrder";
        Map<String, String> headerMap = buildHearderMap(authMap);
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("action", action);
        bodyMap.put("data", JSONUtil.toJsonStr(weiShiCancelOutboundRequest));
        log.warn("纬狮取消出库单请求参数: {}", JSONUtil.toJsonStr(weiShiCancelOutboundRequest));
        String bodyStr = OkHttpUtils.doPostJson(apiUrl, bodyMap, headerMap);
        log.warn("纬狮取消出库单响应参数: {}", JSONUtil.toJsonStr(weiShiCancelOutboundRequest));
        ThirdWarehouseContext.setRequestJson(JSONUtil.toJsonStr(bodyMap));
        ThirdWarehouseContext.setResponseJson(bodyStr);
        return WeiShiUtils.parseToJiFengResp(bodyStr, new TypeReference<WeiShiBaseResp<String>>() {});
    }

    /**
     * 出库单查询
     * @return
     */
    public WeiShiBaseResp<WeiShiOutboundResp> getOutbound(WeiShiGetOutboundRequest weiShiGetOutboundRequest,Map<String,Object> authMap){
        String action = "getSkuOrderDetail";
        Map<String, String> headerMap = buildHearderMap(authMap);
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("action", action);
        bodyMap.put("data", JSONUtil.toJsonStr(weiShiGetOutboundRequest));
        String bodyStr = OkHttpUtils.doPostJson(apiUrl, bodyMap, headerMap);
        return WeiShiUtils.parseToJiFengResp(bodyStr, new TypeReference<WeiShiBaseResp<WeiShiOutboundResp>>() {});
    }


    private Map<String, String> buildHearderMap(Map<String, Object> authMap) {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", authMap.get("accessToken").toString());
        return headerMap;
    }

}
