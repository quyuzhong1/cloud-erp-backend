package com.sdk.wms.weishi.service;

import cn.hutool.json.JSONUtil;
import com.common.business.constant.BusinessCommonConstants;
import com.sdk.wms.weishi.dto.request.WeiShiProductRequest;
import com.sdk.wms.weishi.dto.request.WeiShiStockAgeRequest;
import com.sdk.wms.weishi.dto.request.WeiShiStockRequest;
import com.sdk.wms.weishi.dto.response.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={WeiShiService.class, BusinessCommonConstants.class})
public class WeiShiServiceTest {

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    @Resource
    private WeiShiService weiShiService;

    @Test
    public void accessToken() {
    }

    @Test
    public void getWarehouseList() {
    }

    @Test
    public void querySkuList() {
        Map<String,Object> authMap = new HashMap<>();
        authMap.put("appKey","613cefbb29a34ab5af3f26c3a04ff6a7");
        WeiShiBaseResp<WeiShiTokenResp> tokenRespWeiShiBaseResp = weiShiService.accessToken(authMap);
        System.out.println(JSONUtil.toJsonStr(tokenRespWeiShiBaseResp));
        authMap.put("accessToken", tokenRespWeiShiBaseResp.getData().getAccessToken());
        WeiShiProductRequest weiShiProductRequest = new WeiShiProductRequest();
        weiShiProductRequest.setAuthMap(authMap);
        weiShiProductRequest.setStartTime("2024-03-01 00:00:00");
        weiShiProductRequest.setEndTime("2025-04-07 00:00:00");
        WeiShiBaseResp<List<WeiShiProductResp.ListDTO>>  resp = weiShiService.querySkuList(weiShiProductRequest);
        System.out.println(JSONUtil.toJsonStr(resp));
    }

    @Test
    public void getStockAll() {
        Map<String,Object> authMap = new HashMap<>();
        authMap.put("appKey","613cefbb29a34ab5af3f26c3a04ff6a7");
        WeiShiBaseResp<WeiShiTokenResp> tokenRespWeiShiBaseResp = weiShiService.accessToken(authMap);
        System.out.println(JSONUtil.toJsonStr(tokenRespWeiShiBaseResp));
        authMap.put("accessToken", tokenRespWeiShiBaseResp.getData().getAccessToken());
        WeiShiStockRequest weiShiProductRequest = new WeiShiStockRequest();
        weiShiProductRequest.setAuthMap(authMap);
        WeiShiBaseResp<List<WeiShiStockResp.DataDTO.ListDTO>>  resp = weiShiService.getStockAll(weiShiProductRequest);
        System.out.println(JSONUtil.toJsonStr(resp));
    }

    @Test
    public void queryStockSkuAgeList() {
        Map<String,Object> authMap = new HashMap<>();
        authMap.put("appKey","613cefbb29a34ab5af3f26c3a04ff6a7");
        WeiShiBaseResp<WeiShiTokenResp> tokenRespWeiShiBaseResp = weiShiService.accessToken(authMap);
        System.out.println(JSONUtil.toJsonStr(tokenRespWeiShiBaseResp));
        authMap.put("accessToken", tokenRespWeiShiBaseResp.getData().getAccessToken());
        WeiShiStockAgeRequest weiShiProductRequest = new WeiShiStockAgeRequest();
        weiShiProductRequest.setAuthMap(authMap);
        WeiShiBaseResp<List<WeiShiStockAgeResp.ListDTO>>  resp = weiShiService.queryStockSkuAgeList(weiShiProductRequest);
        System.out.println(JSONUtil.toJsonStr(resp));
    }
}