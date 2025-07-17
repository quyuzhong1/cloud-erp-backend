package com.sdk.wms.weishi.service;

import cn.hutool.json.JSONUtil;
import com.common.business.constant.BusinessCommonConstants;
import com.sdk.wms.weishi.dto.request.*;
import com.sdk.wms.weishi.dto.response.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
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
    public void querySkuList() {
        Map<String,Object> authMap = new HashMap<>();
        authMap.put("appKey","38aff6340627409da49ddf0bf3cbe854");
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
    public void getWarehouseList() {
        Map<String,Object> authMap = new HashMap<>();
        authMap.put("appKey","38aff6340627409da49ddf0bf3cbe854");
        WeiShiBaseResp<WeiShiTokenResp> tokenRespWeiShiBaseResp = weiShiService.accessToken(authMap);
        System.out.println(JSONUtil.toJsonStr(tokenRespWeiShiBaseResp));
        authMap.put("accessToken", tokenRespWeiShiBaseResp.getData().getAccessToken());
        WeiShiStockRequest weiShiProductRequest = new WeiShiStockRequest();
        weiShiProductRequest.setAuthMap(authMap);
        WeiShiBaseResp<List<WeiShiWarehouseResp>>  resp = weiShiService.getWarehouseList(authMap);
        System.out.println(JSONUtil.toJsonStr(resp));
    }

    @Test
    public void getStockAll() {
        Map<String,Object> authMap = new HashMap<>();
        authMap.put("appKey","38aff6340627409da49ddf0bf3cbe854");
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
        authMap.put("appKey","38aff6340627409da49ddf0bf3cbe854");
        WeiShiBaseResp<WeiShiTokenResp> tokenRespWeiShiBaseResp = weiShiService.accessToken(authMap);
        System.out.println(JSONUtil.toJsonStr(tokenRespWeiShiBaseResp));
        authMap.put("accessToken", tokenRespWeiShiBaseResp.getData().getAccessToken());
        WeiShiStockAgeRequest weiShiProductRequest = new WeiShiStockAgeRequest();
        weiShiProductRequest.setAuthMap(authMap);
        WeiShiBaseResp<List<WeiShiStockAgeResp.ListDTO>>  resp = weiShiService.queryStockSkuAgeList(weiShiProductRequest);
        System.out.println(JSONUtil.toJsonStr(resp));
    }
    @Test
    public void createInbound() {
        Map<String,Object> authMap = new HashMap<>();
        authMap.put("appKey","38aff6340627409da49ddf0bf3cbe854");
        WeiShiBaseResp<WeiShiTokenResp> tokenRespWeiShiBaseResp = weiShiService.accessToken(authMap);
        System.out.println(JSONUtil.toJsonStr(tokenRespWeiShiBaseResp));
        authMap.put("accessToken", tokenRespWeiShiBaseResp.getData().getAccessToken());
        WeiShiCreateInboundRequest request = WeiShiCreateInboundRequest.builder()
                .inboundType("SKU")
                .inboundMode("CUSTOMER")
                .transportType("LOCAL_DELIVERY")
                .contact(WeiShiCreateInboundRequest.ContactDTO.builder()
                        .city("深圳市")
                        .contactName("测试")
                        .countryCode("CN")
                        .phone("13800138000")
                        .state("广东省")
                        .street("测试街道")
                        .build())
                .trackingNo("1234567890")
                .destWarehouseCode("WTST")
                .expectedArriveDate(LocalDateTime.now().plusDays(2).format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)))
                .inboundBoxList(Arrays.asList(
                        WeiShiCreateInboundRequest.InboundBoxListDTO.builder()
                                .boxCode("FHDTEST0717001")
                                .boxLength("1")
                                .boxWeight("2")
                                .boxHeight("3")
                                .boxWeight("10")
                                .sysBoxSeq(1)
                                .inboundSkuList(Arrays.asList(
                                        WeiShiCreateInboundRequest.InboundBoxListDTO.InboundSkuListDTO.builder()
                                                .skuCode("Bar_PQ008")
                                                .quantity(10)
                                                .build()
                                ))
                                .build()
                                )
                )
                .remark("123")
                .appointmentPickingStartTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)))
                .appointmentPickingEndTime(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)))
//                .deliveryVoucherBase64("base64")
                .build();
        WeiShiBaseResp<String>  resp = weiShiService.createInbound(request,authMap);
        System.out.println(JSONUtil.toJsonStr(resp));
    }

    @Test
    public void cancelInbound() {
        Map<String,Object> authMap = new HashMap<>();
        authMap.put("appKey","38aff6340627409da49ddf0bf3cbe854");
        WeiShiBaseResp<WeiShiTokenResp> tokenRespWeiShiBaseResp = weiShiService.accessToken(authMap);
        System.out.println(JSONUtil.toJsonStr(tokenRespWeiShiBaseResp));
        authMap.put("accessToken", tokenRespWeiShiBaseResp.getData().getAccessToken());
        WeiShiCancelInboundRequest weiShiProductRequest = new WeiShiCancelInboundRequest();
        weiShiProductRequest.setOrderNo("RV2408170007");
        WeiShiBaseResp<String>  resp = weiShiService.cancelInbound(weiShiProductRequest,authMap);
        System.out.println(JSONUtil.toJsonStr(resp));
    }

    @Test
    public void getInbound() {
        Map<String,Object> authMap = new HashMap<>();
        authMap.put("appKey","38aff6340627409da49ddf0bf3cbe854");
        WeiShiBaseResp<WeiShiTokenResp> tokenRespWeiShiBaseResp = weiShiService.accessToken(authMap);
        System.out.println(JSONUtil.toJsonStr(tokenRespWeiShiBaseResp));
        authMap.put("accessToken", tokenRespWeiShiBaseResp.getData().getAccessToken());
        WeiShiQueryInboundRequest weiShiProductRequest = new WeiShiQueryInboundRequest();
        weiShiProductRequest.setOrderNoList(Arrays.asList("RV012507170007"));
        weiShiProductRequest.setPageSize(50);
        weiShiProductRequest.setPageNum(1);
        WeiShiBaseResp<WeiShiInboundResp>  resp = weiShiService.getInbound(weiShiProductRequest,authMap);
        System.out.println(JSONUtil.toJsonStr(resp));
    }

    @Test
    public void createOutbound() {
        Map<String,Object> authMap = new HashMap<>();
        authMap.put("appKey","38aff6340627409da49ddf0bf3cbe854");
        WeiShiBaseResp<WeiShiTokenResp> tokenRespWeiShiBaseResp = weiShiService.accessToken(authMap);
        System.out.println(JSONUtil.toJsonStr(tokenRespWeiShiBaseResp));
        authMap.put("accessToken", tokenRespWeiShiBaseResp.getData().getAccessToken());
        WeiShiCreateOutboundRequest weiShiCreateOutboundRequest = WeiShiCreateOutboundRequest.builder()
                .referNo("WJTEST0717001")
                .warehouseCode("WTST")
                .platformCode("OTHER")
                .orderType(0)
                .productCode("WMX1008")
                .remark("123456")
                .useSpecifiedMaterial("false")
                .skuList(Arrays.asList(WeiShiCreateOutboundRequest.SkuListDTO.builder()
                                .skuCode("PQ008")
                                .quantity(1)
                        .build()))
                .recipient(WeiShiCreateOutboundRequest.RecipientDTO.builder()
                        .name("Bryan Emilio Garces")
                        .taxno("")
                        .postcode("93230")
                        .mobile("5538039447")
                        .email("123456@163.com")
                        .state("Veracruz")
                        .city("Poza Rica De Hidalgo")
                        .street("Las Palmas  Referencia: En Restaurante Leggero Entre: Calle 3 y Calle 7")
                        .countrycode("MX")
                        .build())
                .build();
        WeiShiBaseResp<WeiShiCreateOutboundResp>  resp = weiShiService.createOutbound(weiShiCreateOutboundRequest,authMap);
        System.out.println(JSONUtil.toJsonStr(resp));
    }

    @Test
    public void cancelOutbound() {
        Map<String,Object> authMap = new HashMap<>();
        authMap.put("appKey","38aff6340627409da49ddf0bf3cbe854");
        WeiShiBaseResp<WeiShiTokenResp> tokenRespWeiShiBaseResp = weiShiService.accessToken(authMap);
        System.out.println(JSONUtil.toJsonStr(tokenRespWeiShiBaseResp));
        authMap.put("accessToken", tokenRespWeiShiBaseResp.getData().getAccessToken());
        WeiShiCancelOutboundRequest weiShiCreateOutboundRequest = new WeiShiCancelOutboundRequest();
        weiShiCreateOutboundRequest.setOrderNo("SU0151982327016");
        weiShiCreateOutboundRequest.setReason("测试");
        WeiShiBaseResp<String>  resp = weiShiService.cancelOutbound(weiShiCreateOutboundRequest,authMap);
        System.out.println(JSONUtil.toJsonStr(resp));
    }

    @Test
    public void getLogisticProductList() {
        Map<String,Object> authMap = new HashMap<>();
        authMap.put("appKey","38aff6340627409da49ddf0bf3cbe854");
        WeiShiBaseResp<WeiShiTokenResp> tokenRespWeiShiBaseResp = weiShiService.accessToken(authMap);
        System.out.println(JSONUtil.toJsonStr(tokenRespWeiShiBaseResp));
        authMap.put("accessToken", tokenRespWeiShiBaseResp.getData().getAccessToken());
        WeiShiLogisticProductRequest weiShiProductRequest = new WeiShiLogisticProductRequest();
        weiShiProductRequest.setWarehouseCode("WTST");
        weiShiProductRequest.setIsMultiPackage(0);
        WeiShiBaseResp<List<WeiShiChannelResp>>  resp = weiShiService.getLogisticProductList(weiShiProductRequest,authMap);
        System.out.println(JSONUtil.toJsonStr(resp));
    }
}