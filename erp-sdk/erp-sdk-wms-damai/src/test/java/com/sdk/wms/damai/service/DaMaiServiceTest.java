package com.sdk.wms.damai.service;

import cn.hutool.json.JSONUtil;
import com.common.business.constant.BusinessCommonConstants;
import com.sdk.wms.damai.dto.request.*;
import com.sdk.wms.damai.dto.response.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={BusinessCommonConstants.class,DaMaiService.class})
public class DaMaiServiceTest {

    private final Map<String,Object> authMap = new HashMap<>();
    {
        authMap.put("appToken","5cdf2a88fc91cbc2c7befa959a6f0c0a");
        authMap.put("appKey","68cb7beaeaf5f3caac5bba16a632ca13");
    }

    @Resource
    private DaMaiService daMaiService;

    @Test
    public void getWarehouseList() {
        DaMaiBaseResp<List<DaMaiWarehouseResp>> resp = daMaiService.getWarehouseList(authMap);
        System.out.println(JSONUtil.toJsonStr(resp));
    }

    @Test
    public void getSkuList() {
        DaMaiPageBaseResp<List<DaMaiSkuResp>> resp = daMaiService.getSkuList(authMap);
        System.out.println(JSONUtil.toJsonStr(resp));
    }

    @Test
    public void createInbound() {
        DaMaiCreateInboundRequest request = DaMaiCreateInboundRequest.builder()
                .whCode("CATOR4")
                .custReferenceNo("WJTEST0801")
                .arrivalTime("2025-08-02 00:00:00")
                .logisticsTrackingNo("123456")
                .asnAnSkuList(Arrays.asList(
                        DaMaiCreateInboundRequest.AsnAnSkuListDTO.builder()
                                .custSkuCode("P8D-TEST0051")
                                .custLotNo("20250801")
                                .custPackageNo("ZXGG001")
                                .totalSkuQty(1)
                                .packQty(1)
                                .build()
                ))
                .build();


        DaMaiBaseResp<DaMaiCreateInboundResp> resp = daMaiService.createInbound(authMap,request);
        System.out.println(JSONUtil.toJsonStr(resp));
    }

    @Test
    public void cancelInbound() {

        DaMaiBaseResp<String> resp = daMaiService.cancelInbound(authMap,new DaMaiCancelInboundRequest("ASNP8D20250801000001"));
        System.out.println(JSONUtil.toJsonStr(resp));
    }

    @Test
    public void getInventoryTrans() {
        DaMaiInventoryTransRequest daMaiInventoryTransRequest = new DaMaiInventoryTransRequest();
        daMaiInventoryTransRequest.setStartOperationTime("2025-07-01 00:00:00");
        daMaiInventoryTransRequest.setEndOperationTime("2025-08-01 00:00:00");
        DaMaiPageBaseResp<List<DaMaiInventoryTransResp>> resp = daMaiService.getInventoryTrans(authMap,daMaiInventoryTransRequest);
        System.out.println(JSONUtil.toJsonStr(resp));
    }

    @Test
    public void getInventoryAge() {
        DaMaiInventoryAgeRequest daMaiInventoryAgeRequest = new DaMaiInventoryAgeRequest();
        daMaiInventoryAgeRequest.setCustomerSkuCodeList(Arrays.asList("TEST-PHONE","TEST-001"));
        daMaiInventoryAgeRequest.setPage(1);
        daMaiInventoryAgeRequest.setLimit(200);
        DaMaiBaseResp<String> resp = daMaiService.getInventoryAge(authMap,daMaiInventoryAgeRequest);
        System.out.println(JSONUtil.toJsonStr(resp));
    }


    @Test
    public void getChannel() {
        DaMaiBaseResp<List<DaMaiChannelResp>> resp = daMaiService.getChannel(authMap);
        System.out.println(JSONUtil.toJsonStr(resp));
    }

    @Test
    public void getInventory() {
        DaMaiPageBaseResp<List<DaMaiInventoryResp>> resp = daMaiService.getInventory(authMap);
        System.out.println(JSONUtil.toJsonStr(resp));
    }

    @Test
    public void calculateFee() {
        DaMaiCalculateFeeRequest daMaiCalculateFeeRequest = DaMaiCalculateFeeRequest.builder()
                .consigneeCountryCode("JIAYOU-001")
                .grossWeight("0.5")
                .length("12")
                .width("11")
                .height("10")
                .packageQty("1")
                .residentialFlag("1")
                .podFlag("0")
                .build();
        DaMaiPageBaseResp<List<DaMaiCalculateFeeResp>> resp = daMaiService.calculateFee(authMap,daMaiCalculateFeeRequest);
        System.out.println(JSONUtil.toJsonStr(resp));
    }

    @Test
    public void createOrder() {
        DaMaiCreateOrderRequest daMaiCreateOrderRequest = DaMaiCreateOrderRequest.builder()
                .carriersCode("JIAYOU-001")
                .custRefNo("wj20250801111")
                .whCode("CAVCR1")
                .shippingType("CHANNEL")
                .endProviderCode("")
                .consigneeName("test")
                .consigneeCountryCode("CA")
                .consigneeProvince("California")
                .consigneeCity("Los Angeles")
                .consigneeAddress1("123 Main St")
                .soSkuList(Arrays.asList(
                        DaMaiCreateOrderRequest.SoSkuListDTO.builder()
                                .custSkuCode("TEST-PHONE")
                                .skuQty(1)
                                .build(),
                        DaMaiCreateOrderRequest.SoSkuListDTO.builder()
                                .custSkuCode("TEST-001")
                                .skuQty(2)
                                .build()
                ))
//                .soSkuDeclaredList(Arrays.asList(
//                        DaMaiCreateOrderRequest.SoSkuDeclaredListDTO.builder()
//                                .skuCode("TEST-PHONE")
//                                .skuNameEn("Test Phone")
//                                .declaredWeight("123")
//                                .declaredValue("100.00")
//                                .skuQty(1)
//                                .build(),
//                        DaMaiCreateOrderRequest.SoSkuDeclaredListDTO.builder()
//                                .skuCode("TEST-001")
//                                .skuNameEn("Test Item 001")
//                                .declaredWeight("123")
//                                .declaredValue("50.00")
//                                .skuQty(1)
//                                .build()
//                ))
                .build();
        DaMaiBaseResp<DaMaiCreateOrderResp> resp = daMaiService.createOrder(authMap,daMaiCreateOrderRequest);
        System.out.println(JSONUtil.toJsonStr(resp));
    }



    @Test
    public void cancelOrder() {
        DaMaiCancelOrderRequest daMaiCancelOrderRequest = new DaMaiCancelOrderRequest();
        daMaiCancelOrderRequest.setSoNo("ODP8D202508110001");
        DaMaiBaseResp<String> resp = daMaiService.cancelOrder(authMap,daMaiCancelOrderRequest);
        System.out.println(JSONUtil.toJsonStr(resp));
    }

    @Test
    public void getOrderList() {
        DaMaiGetOrderRequest daMaiCancelOrderRequest = new DaMaiGetOrderRequest();
        daMaiCancelOrderRequest.setCustRefNoList(Arrays.asList("wj2025080801","wj20250801111","OD-7DX-20250718-0001"));
        DaMaiBaseResp<List<DaMaiGetOrderResp>>  resp = daMaiService.getOrderList(authMap,daMaiCancelOrderRequest);
        System.out.println(JSONUtil.toJsonStr(resp));
    }
}