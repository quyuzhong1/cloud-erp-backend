package com.sdk.oms.temu.service;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.core.utils.HttpCommonUtil;
import com.sdk.oms.temu.dto.TemuResp;
import com.sdk.oms.temu.dto.TemuShopInfoDTO;
import com.sdk.oms.temu.dto.TemuWarehouseDTO;
import com.sdk.oms.temu.enums.TemuEnum;
import com.sdk.oms.temu.util.EncryptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class TemuClient {

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
        TemuClient temuClient = new TemuClient();
        TemuResp<TemuWarehouseDTO> resp = temuClient.getWarehouseList(new TemuShopInfoDTO("US",clientId, clientSecret, token));
        System.out.println(resp);
    }

    public TemuResp<TemuWarehouseDTO> getWarehouseList(TemuShopInfoDTO temuShopInfoDTO){
        TemuEnum temuEnum = TemuEnum.getByCode(temuShopInfoDTO.getAreaCode());
        this.checkShopInfo(temuShopInfoDTO);
        String api = "bg.logistics.warehouse.list.get";
        Map<String, Object> params = this.buildDefaultParams(temuShopInfoDTO, api);
        // 追加请求路径获取签名
        String sign = EncryptionUtils.generateSignature(params, temuShopInfoDTO.getAppSecret());
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

    private void checkShopInfo(TemuShopInfoDTO temuShopInfoDTO){
        if (temuShopInfoDTO == null) {
            throw new RuntimeException("店铺信息不能为空");
        }
        if (temuShopInfoDTO.getAreaCode() == null) {
            throw new RuntimeException("区域不能为空");
        }
        if (temuShopInfoDTO.getAppKey() == null) {
            throw new RuntimeException("appkey不能为空");
        }
        if (temuShopInfoDTO.getAppSecret() == null) {
            throw new RuntimeException("appSecret不能为空");
        }
        if (temuShopInfoDTO.getToken() == null) {
            throw new RuntimeException("token不能为空");
        }
    }

    private Map<String, Object> buildDefaultParams(TemuShopInfoDTO temuShopInfoDTO,String apiType) {
        Map<String, Object> params = new HashMap<>();
        params.put("type", apiType);
        params.put("timestamp", System.currentTimeMillis() / 1000);
        params.put("app_key", temuShopInfoDTO.getAppKey());
        params.put("access_token", temuShopInfoDTO.getToken());
        return params;
    }

}
