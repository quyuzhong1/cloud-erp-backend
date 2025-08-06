package com.sdk.wms.damai.service;

import cn.hutool.json.JSONUtil;
import com.common.business.constant.BusinessCommonConstants;
import com.sdk.wms.damai.dto.request.DaMaiCancelInboundRequest;
import com.sdk.wms.damai.dto.request.DaMaiCreateInboundRequest;
import com.sdk.wms.damai.dto.request.DaMaiInventoryAgeRequest;
import com.sdk.wms.damai.dto.request.DaMaiInventoryTransRequest;
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
}