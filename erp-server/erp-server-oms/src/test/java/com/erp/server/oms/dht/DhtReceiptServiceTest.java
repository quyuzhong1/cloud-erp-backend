package com.erp.server.oms.dht;

import cn.hutool.json.JSONUtil;
import com.erp.server.oms.ErpServerOmsApplication;
import com.sdk.oms.dht.dto.DhtBaseResp;
import com.sdk.oms.dht.dto.req.DhtCommonQueryReq;
import com.sdk.oms.dht.dto.resp.DhtUserResp;
import com.sdk.oms.dht.service.DhtCommonService;
import com.sdk.oms.dht.service.DhtReceiptService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerOmsApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Profile("dev")
public class DhtReceiptServiceTest {

    @Resource
    private DhtCommonService dhtCommonService;

    @Resource
    private DhtReceiptService dhtReceiptService;

    @Test
    public void queryReceipt() {
        DhtCommonQueryReq req = new DhtCommonQueryReq();
        DhtUserResp resp1 = dhtCommonService.getUserByMobile("15007174733");
        req.setCurrentOpenUserId(resp1.getEmpList().get(0).getOpenUserId());
        String timeStr = "2025-09-10 14:30:00";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(timeStr, formatter);
        long startTimestamp = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        String endTimeStr = "2025-10-15 14:30:00";
        LocalDateTime endDateTime = LocalDateTime.parse(endTimeStr, formatter);
        long endTimestamp = endDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        DhtCommonQueryReq.DataDTO dataDTO = DhtCommonQueryReq.DataDTO.builder()
                .findExplicitTotalNum(false)
                .searchQueryInfo(DhtCommonQueryReq.DataDTO.SearchQueryInfoDTO.builder()
                        .offset(0)
                        .limit(10)
                        .fieldProjection(null)
                        .filters(null)
                        .orders(null)
                        .filters(Arrays.asList(
                                DhtCommonQueryReq.DataDTO.SearchQueryInfoDTO.FiltersDTO.builder()
                                        .fieldName("name")
                                        .fieldValues(Arrays.asList("20250911-000012"))
                                        .operator("IN")
                                        .build()
                        ))
                        .build())
                .build();
        req.setData(dataDTO);
        DhtBaseResp<String> resp = dhtReceiptService.queryReceipt(req);
        System.out.println(JSONUtil.toJsonStr(resp).replace("\\\"", "\"").replace("\"{","{").replace("}\"","}"));
    }

    @Test
    public void queryReceiptDetail() {
        DhtCommonQueryReq req = new DhtCommonQueryReq();
        DhtUserResp resp1 = dhtCommonService.getUserByMobile("15007174733");
        req.setCurrentOpenUserId(resp1.getEmpList().get(0).getOpenUserId());

        DhtCommonQueryReq.DataDTO dataDTO = DhtCommonQueryReq.DataDTO.builder()
                .findExplicitTotalNum(false)
                .searchQueryInfo(DhtCommonQueryReq.DataDTO.SearchQueryInfoDTO.builder()
                        .offset(0)
                        .limit(10)
                        .fieldProjection(null)
                        .filters(null)
                        .orders(null)
                        .filters(Arrays.asList(
                                DhtCommonQueryReq.DataDTO.SearchQueryInfoDTO.FiltersDTO.builder()
                                        .fieldName("payment_id")
                                        .fieldValues(Arrays.asList("689e9fe245d5340001d09137","68a696263e3cbc0001584412","6894659c2c37fd000686c9d9"))
                                        .operator("IN")
                                        .build()
                        ))
                        .build())
                .build();
        req.setData(dataDTO);
        DhtBaseResp<String> resp = dhtReceiptService.queryReceiptDetail(req);
        System.out.println(JSONUtil.toJsonStr(resp).replace("\\\"", "\"").replace("\"{","{").replace("}\"","}"));
    }


    @Test
    public void updateReceipt() {
        Map<String, Object> req = new java.util.HashMap<>();
        Map<String, Object> dataMap = new java.util.HashMap<>();;

        Map<String, Object> objMap = new java.util.HashMap<>();
        objMap.put("dataObjectApiName","PaymentObj");
        objMap.put("_id","68c22764039a5500071d218d");
        objMap.put("life_status","under_review");
        dataMap.put("object_data",objMap);
        req.put("data",dataMap);
        DhtBaseResp<String> resp = dhtReceiptService.updateReceipt(req);
        System.out.println(JSONUtil.toJsonStr(resp).replace("\\\"", "\"").replace("\"{","{").replace("}\"","}"));
    }
}