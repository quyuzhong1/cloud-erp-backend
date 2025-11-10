package com.sdk.wms.jifeng.service;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.utils.OkHttpUtils;
import com.sdk.wms.jifeng.dto.request.JiFengAuthRequest;
import com.sdk.wms.jifeng.dto.request.JiFengCreateInboundRequest;
import com.sdk.wms.jifeng.dto.request.JiFengCreateOutboundRequest;
import com.sdk.wms.jifeng.dto.request.JiFengReturnOrderRequest;
import com.sdk.wms.jifeng.dto.response.*;
import com.sdk.wms.jifeng.utils.JiFengUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


@Slf4j
@Component
public class JiFengService {

    public static void main(String[] args) {
        JiFengService jiFengService = new JiFengService();
        Map<String,Object> authMap = new HashMap<>();
        authMap.put("domain","sureparcel");
        authMap.put("accessToken","d57ee2fa8c224d3089a301845afa4b64");
        authMap.put("appKey","a03b35bf7f0c4c4f8e23e0599b5be649");
        authMap.put("userId","7471");
        authMap.put("appToken","f9af8dc7afea488991a216485987746c");
//        List<String> erpNo = new ArrayList<>();
//        erpNo.add("WFHD25101603694");
        JiFengBaseResp<JiFengCreateInboundResp> a = jiFengService.cancelInbound(authMap,"IN5200096","FHD25110100002");
        System.out.println(JSONObject.toJSONString( a));
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
    public JiFengBaseResp<JiFengOfflineChannelResp> getOfflineChannel(Map<String,Object> authMap, String warehouseCode){
        String path = "/api/logistics/offline/page";
        String url = getUrl(authMap.get("domain").toString());

        Map<String, String> headerMap = buildHearderMap(authMap, path);

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("code",warehouseCode);
        String bodyStr = OkHttpUtils.doPostJson(url+path, bodyMap, headerMap);
        return JiFengUtils.parseToJiFengResp(bodyStr, new TypeReference<JiFengBaseResp<JiFengOfflineChannelResp>>() {});
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

    /**
     * 查询产品
     * @param authMap
     * @return
     */
    public JiFengBaseResp<List<JiFengProductResp.RowsDTO>> getProductList(Map<String,Object> authMap){
        String path = "/api/sku/detail";
        String url = getUrl(authMap.get("domain").toString());
        List<JiFengProductResp.RowsDTO> allData = new ArrayList<>();
        int pageNo = 1;
        boolean hasMore = true;

        while (hasMore) {
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("pageNo", pageNo);
            bodyMap.put("pageSize", 50); // 每页大小，可根据实际情况调整
            Map<String, String> headerMap = buildHearderMap(authMap, path);
            String bodyStr = OkHttpUtils.doPostJson(url+path, bodyMap, headerMap);
            JiFengBaseResp<JiFengProductResp> response = JiFengUtils.parseToJiFengResp(bodyStr,JiFengProductResp.class);
            if(Objects.isNull(response)){
                log.error("极风获取产品列表失败，返回结果为空,返回值:{}",bodyStr);
                return null;
            }
            if (response.getCode() == 0) {
                JiFengProductResp pageData = response.getData();
                if (pageData != null && pageData.getPageNo() != null) {
                    allData.addAll(pageData.getRows());

                    // 判断是否还有下一页
                    if (pageData.getRows().isEmpty() || pageData.getTotalPage() <= pageNo) {
                        hasMore = false;
                    } else {
                        pageNo++;
                    }
                } else {
                    hasMore = false;
                }
            } else {
                // 如果请求失败，直接返回错误信息
                JiFengBaseResp<List<JiFengProductResp.RowsDTO>> errorResp = new JiFengBaseResp<>();
                errorResp.setCode(response.getCode());
                errorResp.setMessage(response.getMessage());
                errorResp.setRequestId(response.getRequestId());
                return errorResp;
            }
        }

        JiFengBaseResp<List<JiFengProductResp.RowsDTO>> result = new JiFengBaseResp<>();
        result.setCode(0);
        result.setMessage("success");
        result.setData(allData);
        return result;
    }

    /**
     * 查询库存
     * @param authMap
     * @return
     */
    public JiFengBaseResp<List<JiFengInventoryResp.RowsDTO>> getInventoryList(Map<String,Object> authMap,String warehouseCode){
        String path = "/api/inventory/queryInventory";
        String url = getUrl(authMap.get("domain").toString());
        List<JiFengInventoryResp.RowsDTO> allData = new ArrayList<>();
        int pageNo = 1;
        boolean hasMore = true;

        while (hasMore) {
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("warehouse",warehouseCode);
            bodyMap.put("pageNo", pageNo);
            bodyMap.put("pageSize", 50); // 每页大小，可根据实际情况调整
            Map<String, String> headerMap = buildHearderMap(authMap, path);
            String bodyStr = OkHttpUtils.doPostJson(url+path, bodyMap, headerMap);
            JiFengBaseResp<JiFengInventoryResp> response = JiFengUtils.parseToJiFengResp(bodyStr,JiFengInventoryResp.class);
            if(Objects.isNull(response)){
                log.error("极风获取库存列表失败，返回结果为空,返回值:{}",bodyStr);
                return null;
            }
            if (response.getCode() == 0) {
                JiFengInventoryResp.PageDTO pageData = response.getData().getPage();
                if (pageData != null && pageData.getPageNo() != null) {
                    allData.addAll(pageData.getRows());

                    // 判断是否还有下一页
                    if (pageData.getRows().isEmpty() || pageData.getTotalPage() <= pageNo) {
                        hasMore = false;
                    } else {
                        pageNo++;
                    }
                } else {
                    hasMore = false;
                }
            } else {
                // 如果请求失败，直接返回错误信息
                JiFengBaseResp<List<JiFengInventoryResp.RowsDTO>> errorResp = new JiFengBaseResp<>();
                errorResp.setCode(response.getCode());
                errorResp.setMessage(response.getMessage());
                errorResp.setRequestId(response.getRequestId());
                return errorResp;
            }
        }

        JiFengBaseResp<List<JiFengInventoryResp.RowsDTO>> result = new JiFengBaseResp<>();
        result.setCode(0);
        result.setMessage("success");
        result.setData(allData);
        return result;
    }

    /**
     * 创建入库单
     * @param authMap
     * @return
     */
    public JiFengBaseResp<JiFengCreateInboundResp> createInbound(Map<String,Object> authMap, JiFengCreateInboundRequest jiFengCreateInboundRequest){
        String path = "/api/inbound/create";
        String url = getUrl(authMap.get("domain").toString());
        Map<String, String> headerMap = buildHearderMap(authMap, path);
        ThirdWarehouseContext.setRequestJson(JSONUtil.toJsonStr(jiFengCreateInboundRequest));
        String bodyStr = OkHttpUtils.doPostJson(url+path, JSONUtil.toJsonStr(jiFengCreateInboundRequest), headerMap);
        ThirdWarehouseContext.setResponseJson(bodyStr);
        JiFengBaseResp<JiFengCreateInboundResp> response = JiFengUtils.parseToJiFengResp(bodyStr,JiFengCreateInboundResp.class);
        return response;
    }
    /**
     * 取消入库单
     * @param authMap
     * @return
     */
    public JiFengBaseResp<JiFengCreateInboundResp> cancelInbound(Map<String,Object> authMap, String inboundNo,String erpNo){
        String path = "/api/inbound/cancel";
        String url = getUrl(authMap.get("domain").toString());
        Map<String, String> headerMap = buildHearderMap(authMap, path);
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("inboundNo",inboundNo);
        paramMap.put("erpNo",erpNo);
        ThirdWarehouseContext.setRequestJson(JSONUtil.toJsonStr(paramMap));
        String bodyStr = OkHttpUtils.doPostJson(url+path, paramMap, headerMap);
        ThirdWarehouseContext.setResponseJson(bodyStr);
        JiFengBaseResp<JiFengCreateInboundResp> response = JiFengUtils.parseToJiFengResp(bodyStr,JiFengCreateInboundResp.class);
        if(Objects.isNull(response)){
            log.error("极风取消入库单失败，返回结果为空,返回值:{}",bodyStr);
            return JiFengBaseResp.error("极风取消入库单详情失败，返回结果为空");
        }
        return response;
    }
    /**
     * 查询入库单
     * @param authMap
     * @return
     */
    public JiFengBaseResp<JiFengInboundResp> getInbound(Map<String,Object> authMap, String inboundNo){
        String path = "/api/inbound/get";
        String url = getUrl(authMap.get("domain").toString());
        Map<String, String> headerMap = buildHearderMap(authMap, path);
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("inboundNo",inboundNo);
        String bodyStr = OkHttpUtils.doPostJson(url+path, paramMap, headerMap);
        JiFengBaseResp<JiFengInboundResp> response = JiFengUtils.parseToJiFengResp(bodyStr,JiFengInboundResp.class);
        if(Objects.isNull(response)){
            log.error("极风获取入库单详情失败，返回结果为空,返回值:{}",bodyStr);
            return JiFengBaseResp.error("极风获取入库单详情失败，返回结果为空");
        }
        return response;
    }

    /**
     * 创建出库单
     * @param authMap
     * @return
     */
    public JiFengBaseResp<String> createOutbound(Map<String,Object> authMap, JiFengCreateOutboundRequest request){
        String path = "/api/order/create";
        String url = getUrl(authMap.get("domain").toString());
        Map<String, String> headerMap = buildHearderMap(authMap, path);
        if(!BusinessCommonConstants.hasProfile("prod")){
            request.setPickingNote("测试单");
        }
        ThirdWarehouseContext.setRequestJson(JSONUtil.toJsonStr(request));
        String bodyStr = OkHttpUtils.doPostJson(url+path, JSONUtil.toJsonStr(request), headerMap);
        ThirdWarehouseContext.setResponseJson(bodyStr);
        JiFengBaseResp<String> response = JiFengUtils.parseToJiFengResp(bodyStr,String.class);
        return response;
    }

    /**
     * 查询出库单
     * @param authMap
     * @param erpNo
     * @return
     */
    public JiFengBaseResp<JiFengOutboundResp> getOutBound(Map<String,Object> authMap, String erpNo){
        String path = "/api/order/get";
        String url = getUrl(authMap.get("domain").toString());
        Map<String, String> headerMap = buildHearderMap(authMap, path);
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("erpNo",erpNo);
        String bodyStr = OkHttpUtils.doPostJson(url+path, paramMap, headerMap);
        JiFengBaseResp<JiFengOutboundResp> response = JiFengUtils.parseToJiFengResp(bodyStr,JiFengOutboundResp.class);
        if(Objects.isNull(response)){
            log.error("极风获取入库单详情失败，返回结果为空,返回值:{}",bodyStr);
            return JiFengBaseResp.error("极风获取入库单详情失败，返回结果为空");
        }
        return response;
    }

    /**
     * 取消出库单
     * @param authMap
     * @return
     */
    public JiFengBaseResp<String> cancelOutbound(Map<String,Object> authMap, String erpNo){
        String path = "/api/order/cancel";
        String url = getUrl(authMap.get("domain").toString());
        Map<String, String> headerMap = buildHearderMap(authMap, path);
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("erpNo",erpNo);
        ThirdWarehouseContext.setRequestJson(JSONUtil.toJsonStr(paramMap));
        String bodyStr = OkHttpUtils.doPostJson(url+path, paramMap, headerMap);
        ThirdWarehouseContext.setResponseJson(bodyStr);
        JiFengBaseResp<String> response = JiFengUtils.parseToJiFengResp(bodyStr,String.class);
        return response;
    }

    /**
     * 查询订单
     * @param authMap
     * @return
     */
    public JiFengBaseResp<List<JiFengOutboundResp>> getOrder(Map<String,Object> authMap, List<String> erpNo){
        String path = "/api/order/batchGet";
        String url = getUrl(authMap.get("domain").toString());
        Map<String, String> headerMap = buildHearderMap(authMap, path);
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("erpNoList", erpNo);
        String bodyStr = OkHttpUtils.doPostJson(url+path, paramMap, headerMap);
        JiFengBaseResp<List<JiFengOutboundResp>> response = JiFengUtils.parseToJiFengResp(bodyStr,new TypeReference<JiFengBaseResp<List<JiFengOutboundResp>>>() {});
        return response;
    }


    /**
     * 查询退货订单
     * @param authMap
     * @return
     */
    public JiFengBaseResp<List<JiFengReturnOrderResp.RowsDTO>> getReturnOrder(Map<String,Object> authMap, JiFengReturnOrderRequest request){
        String path = "/api/inbound/return/getByPage";
        String url = getUrl(authMap.get("domain").toString());
        List<JiFengReturnOrderResp.RowsDTO> allData = new ArrayList<>();
        int pageNo = 1;
        boolean hasMore = true;

        while (hasMore) {
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("beginTime",request.getBeginTime());
            bodyMap.put("endTime",request.getEndTime());
            bodyMap.put("pageNo", pageNo);
            bodyMap.put("pageSize", 50); // 每页大小，可根据实际情况调整
            Map<String, String> headerMap = buildHearderMap(authMap, path);
            String bodyStr = OkHttpUtils.doPostJson(url+path, bodyMap, headerMap);
            JiFengBaseResp<JiFengReturnOrderResp> response = JiFengUtils.parseToJiFengResp(bodyStr,JiFengReturnOrderResp.class);

            if(Objects.isNull(response)){
                log.error("极风获取退货订单列表失败，返回结果为空,返回值:{}",bodyStr);
                return null;
            }
            if (response.getCode() == 0) {
                JiFengReturnOrderResp pageData = response.getData();
                if (pageData != null && pageData.getPageNo() != null && CollectionUtils.isNotEmpty(pageData.getRows())) {
                    allData.addAll(pageData.getRows());

                    // 判断是否还有下一页
                    if (pageData.getRows().isEmpty() || pageData.getTotalPage() <= pageNo) {
                        hasMore = false;
                    } else {
                        pageNo++;
                    }
                } else {
                    hasMore = false;
                }
            } else {
                // 如果请求失败，直接返回错误信息
                JiFengBaseResp<List<JiFengReturnOrderResp.RowsDTO>> errorResp = new JiFengBaseResp<>();
                errorResp.setCode(response.getCode());
                errorResp.setMessage(response.getMessage());
                errorResp.setRequestId(response.getRequestId());
                return errorResp;
            }
        }

        JiFengBaseResp<List<JiFengReturnOrderResp.RowsDTO>> result = new JiFengBaseResp<>();
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
