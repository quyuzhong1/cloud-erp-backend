package com.sdk.oms.temu.service;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.core.utils.HttpCommonUtil;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.google.gson.Gson;
import com.sdk.oms.temu.dto.*;
import com.sdk.oms.temu.enums.TemuEnum;
import com.sdk.oms.temu.util.EncryptionUtils;
import io.seata.common.util.CollectionUtils;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Component
public class TemuClient {

    @Resource
    private ShopInfoFeign shopInfoFeign;

    private static final String CONTENT_TYPE_HEADER_NAME = "Content-Type";

    //https://seller.kuajingmaihuo.com/sop/view/867739977041685428#r2WKrz
    private static final String TEMU_US_URL = "http://40.118.250.12:7000/openapi/router";
//    private static final String TEMU_US_URL = "http://openapi-b-us.temudemo.com/openapi/router";
    private static final String ORDER_LIST = "bg.order.list.get";

    //https://seller.kuajingmaihuo.com/sop/view/750197804480663142#SjadVR
    private static final String TEMU_GOOD_US_URL = "http://40.118.250.12:7100/openapi/router";
//    private static final String TEMU_GOOD_US_URL = "https://openapi.kuajingmaihuo.com/openapi/router";
//    private static final String TEMU_GOOD_US_URL = "https://kj-openapi.temudemo.com/openapi/router";
    private static final String GOODS_LIST = "bg.goods.list.get";


//    private static final String APP_KEY = "73f83507e4244c78a5b83e1174bcbb19";
//    private static final String APP_SECRET = "e1557f76a9db2b8e5b335c58b6b5c01b698708ed";
//    private static final String ACCESS_TOKEN = "dnkpkcbcevl2xnflubbhdtyfftgcgkqsbozcwdoxgtdcaiysmdcqbln9";

//美区
    private static final String APP_KEY = "ab3a401ed6c265793776aa3d4c48bd6f";
    private static final String APP_SECRET = "a05e0902cf9c1b3c372680e084f1d424332284fb";
    private static final String ACCESS_TOKEN = "upskffqpqmkoltggbfegqtbs7ghjaenvzwo9kbr1bt1ee5ypb0drvfh20ih";

    private WebClient webClient;


    public static void main(String[] args) {
        String url = "http://40.118.250.12:7000/openapi/router";
        String clientSecret = "a05e0902cf9c1b3c372680e084f1d424332284fb";
        String clientId = "ab3a401ed6c265793776aa3d4c48bd6f";
        String token = "upskffqpqmkoltggbfegqtbs7ghjaenvzwo9kbr1bt1ee5ypb0drvfh20ih";
        TemuOrderReq temuCommonDTO = new TemuOrderReq();
        temuCommonDTO.setToken(token);
        temuCommonDTO.setAppSecret(clientSecret);
        temuCommonDTO.setAppKey(clientId);
        temuCommonDTO.setAreaCode("US");
        temuCommonDTO.setParentOrderSnList(Arrays.asList("PO-211-00886115267191231"));
        temuCommonDTO.setParentOrderSn("PO-211-00886115267191231");
        temuCommonDTO.setOrderSn( "211-00886023516791231");
        TemuClient temuClient = new TemuClient();
        TemuResp<TemuLogisticShipmentDTO> resp = temuClient.getLogisticsShipment(temuCommonDTO);
        System.out.println(JSONUtil.toJsonStr(resp));
    }

    public TemuResp<TemuWarehouseDTO> getWarehouseList(TemuCommonDTO temuCommonDTO){
        TemuEnum temuEnum = TemuEnum.getByCode(temuCommonDTO.getAreaCode());
        this.checkShopInfo(temuCommonDTO);
        String api = "bg.logistics.warehouse.list.get";
        Map<String, Object> params = this.buildDefaultParams(temuCommonDTO, api);
        // 追加请求路径获取签名
        String sign = EncryptionUtils.generateSignature(params, temuCommonDTO.getAppSecret());
        //加入sign签名入参
        params.put("sign", sign);

        //设置请求头
        Map<String, String> headerMap = new HashMap<>(2);
        headerMap.put("content-type", "application/json");
        String url = temuEnum.getUrl();
        ApiResult<String> apiResult = HttpCommonUtil.sendOkHttpApiResult(url , JSONUtil.toJsonStr(params),new HashMap<>(), headerMap, RequestMethod.POST);
        if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
            log.error("入参params={}, temu半托管查询仓库失败，返回值 responseMap={}",  params, JSONUtil.toJsonStr(apiResult));
            throw new ServiceException("temu半托管查询仓库失败，返回值 responseMap={}",JSONUtil.toJsonStr(apiResult));
        }
        return JSON.parseObject(apiResult.getData(),new TypeReference<TemuResp<TemuWarehouseDTO>>() {}.getType());
    }

    public TemuResp<TemuOrderDTO> getOrderList(TemuOrderReq temuOrderReq){
        TemuEnum temuEnum = TemuEnum.getByCode(temuOrderReq.getAreaCode());
        if(temuEnum == null){
            throw new ServiceException("temu区域编码错误，请检查店铺区域参数");
        }
        this.checkShopInfo(temuOrderReq);
        String api = "bg.order.list.v2.get";
        Map<String, Object> params = this.buildDefaultParams(temuOrderReq, api);
        Gson gson = new Gson();
        String jsonArray = gson.toJson(temuOrderReq.getParentOrderSnList());
        params.put("pageSize",20);
        params.put("parentOrderSnList",jsonArray);
        String typeJsonArray = gson.toJson(Arrays.asList("fulfillBySeller","fulfillByCooperativeWarehouse"));
        params.put("fulfillmentTypeList",typeJsonArray);
        // 追加请求路径获取签名
        String sign = EncryptionUtils.generateSignature(params, temuOrderReq.getAppSecret());
        //加入sign签名入参
        params.put("sign", sign);

        //设置请求头
        Map<String, String> headerMap = new HashMap<>(2);
        headerMap.put("content-type", "application/json");
        String url = temuEnum.getUrl();
        ApiResult<String> apiResult = HttpCommonUtil.sendOkHttpApiResult(url , JSONUtil.toJsonStr(params),new HashMap<>(), headerMap, RequestMethod.POST);
        if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
            log.error("入参params={}, temu半托管查询订单失败，返回值 responseMap={}",  params, JSONUtil.toJsonStr(apiResult));
            throw new ServiceException("temu半托管查询订单失败，返回值 responseMap={}",JSONUtil.toJsonStr(apiResult));
        }
        return JSON.parseObject(apiResult.getData(),new TypeReference<TemuResp<TemuOrderDTO>>() {}.getType());
    }

    public TemuResp<TemuLogisticShipmentDTO> getLogisticsShipment(TemuOrderReq temuOrderReq){
        TemuEnum temuEnum = TemuEnum.getByCode(temuOrderReq.getAreaCode());
        this.checkShopInfo(temuOrderReq);
        String api = "bg.logistics.shipment.v2.get";
        Map<String, Object> params = this.buildDefaultParams(temuOrderReq, api);
        params.put("parentOrderSn",temuOrderReq.getParentOrderSn());
        params.put("orderSn",temuOrderReq.getOrderSn());
        // 追加请求路径获取签名
        String sign = EncryptionUtils.generateSignature(params, temuOrderReq.getAppSecret());
        //加入sign签名入参
        params.put("sign", sign);

        //设置请求头
        Map<String, String> headerMap = new HashMap<>(2);
        headerMap.put("content-type", "application/json");
        String url = temuEnum.getUrl();
        ApiResult<String> apiResult = HttpCommonUtil.sendOkHttpApiResult(url , JSONUtil.toJsonStr(params),new HashMap<>(), headerMap, RequestMethod.POST);
        if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
            log.error("入参params={}, temu半托管查询订单失败，返回值 responseMap={}",  params, JSONUtil.toJsonStr(apiResult));
            throw new ServiceException("temu半托管查询订单失败，返回值 responseMap={}",JSONUtil.toJsonStr(apiResult));
        }
        return JSON.parseObject(apiResult.getData(),new TypeReference<TemuResp<TemuLogisticShipmentDTO>>() {}.getType());
    }

    public TemuCommonDTO getAuthInfo(String shopId){
        ApiResult<ShopAuthEntity> apiResult = shopInfoFeign.getShopAuthById(shopId);
        ShopAuthEntity shopAuthEntity = apiResult.getData();
        if(Objects.isNull(shopAuthEntity)){
            return null;
        }
        String extendData = shopAuthEntity.getExtendData();
        if(StringUtils.isBlank(extendData)){
            return null;
        }
        JSONObject jsonObject = JSONUtil.parseObj(extendData);
        String clientId = jsonObject.getStr("clientId");
        String clientSecret = jsonObject.getStr("clientSecret");
        if(StringUtils.isBlank(clientId) ||StringUtils.isBlank(clientSecret)|| StringUtils.isBlank(shopAuthEntity.getAreaCode()) || StringUtils.isBlank(shopAuthEntity.getAccessToken()) ) {
            return null;
        }
        TemuCommonDTO temuCommonDTO = new TemuCommonDTO();
        temuCommonDTO.setAppKey(clientId);
        temuCommonDTO.setAppSecret(clientSecret);
        temuCommonDTO.setToken(shopAuthEntity.getAccessToken());
        temuCommonDTO.setAreaCode(shopAuthEntity.getAreaCode());
        return temuCommonDTO;
    }

    public TemuResp<TemuShippingDTO> getShippingInfo(TemuShippingInfoReq temuShippingInfoReq){
        TemuEnum temuEnum = TemuEnum.getByCode(temuShippingInfoReq.getAreaCode());
        this.checkShopInfo(temuShippingInfoReq);
        String api = "bg.order.decryptshippinginfo.get";
        Map<String, Object> params = this.buildDefaultParams(temuShippingInfoReq, api);
        params.put("parentOrderSn",temuShippingInfoReq.getParentOrderSn());
//        params.put("parentOrderSn","PO-211-1820964856952199");
        // 追加请求路径获取签名
        String sign = EncryptionUtils.generateSignature(params, temuShippingInfoReq.getAppSecret());
        //加入sign签名入参
        params.put("sign", sign);

        //设置请求头
        Map<String, String> headerMap = new HashMap<>(2);
        headerMap.put("content-type", "application/json");
        String url = temuEnum.getUrl();
        ApiResult<String> apiResult = HttpCommonUtil.sendOkHttpApiResult(url , JSONUtil.toJsonStr(params),new HashMap<>(), headerMap, RequestMethod.POST);
        if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
            log.error("入参params={}, temu半托管查询发货地址失败，返回值 responseMap={}",  params, JSONUtil.toJsonStr(apiResult));
            return TemuResp.error("temu半托管查询发货地址失败");
        }
        return JSON.parseObject(apiResult.getData(),new TypeReference<TemuResp<TemuShippingDTO>>() {}.getType());
    }

    private void checkShopInfo(TemuCommonDTO temuCommonDTO){
        if (temuCommonDTO == null) {
            throw new RuntimeException("店铺信息不能为空");
        }
        if (temuCommonDTO.getAreaCode() == null) {
            throw new RuntimeException("区域不能为空");
        }
        if (temuCommonDTO.getAppKey() == null) {
            throw new RuntimeException("appkey不能为空");
        }
        if (temuCommonDTO.getAppSecret() == null) {
            throw new RuntimeException("appSecret不能为空");
        }
        if (temuCommonDTO.getToken() == null) {
            throw new RuntimeException("token不能为空");
        }
    }

    private Map<String, Object> buildDefaultParams(TemuCommonDTO temuCommonDTO, String apiType) {
        Map<String, Object> params = new HashMap<>();
        params.put("type", apiType);
        params.put("timestamp", System.currentTimeMillis() / 1000);
        params.put("app_key", temuCommonDTO.getAppKey());
        params.put("access_token", temuCommonDTO.getToken());
        return params;
    }
}
