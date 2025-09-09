package com.erp.server.oms.dht;

import cn.hutool.json.JSONUtil;
import com.erp.server.oms.ErpServerOmsApplication;
import com.sdk.oms.dht.dto.DhtAuthDTO;
import com.sdk.oms.dht.dto.DhtBaseResp;
import com.sdk.oms.dht.dto.req.*;
import com.sdk.oms.dht.dto.resp.DhtQueryCustomerResp;
import com.sdk.oms.dht.dto.resp.DhtUserResp;
import com.sdk.oms.dht.service.DhtCommonService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Arrays;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerOmsApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Profile("dev")
public class DhtCommonServiceTest {

    @Resource
    private DhtCommonService dhtCommonService;

    @Test
    public void getCorpAccessToken() {
        DhtAuthDTO resp = dhtCommonService.getCorpAccessToken();
        System.out.println(JSONUtil.toJsonStr(resp));
    }

    @Test
    public void getUserByMobile() {
        DhtUserResp resp = dhtCommonService.getUserByMobile("15007174733");
        System.out.println(JSONUtil.toJsonStr(resp));
    }

    @Test
    public void queryObj() {
        DhtQueryObjReq req = new DhtQueryObjReq();
        DhtUserResp resp1 = dhtCommonService.getUserByMobile("15007174733");
        req.setCurrentOpenUserId(resp1.getEmpList().get(0).getOpenUserId());
        req.setData(DhtQueryObjReq.DataDTO.builder()
                .includeDetail(true)
                .apiName("PaymentObj")
                .build());
        String resp = dhtCommonService.queryObj(req);
        System.out.println(JSONUtil.toJsonStr(resp));
    }

    @Test
    public void download() {
        DhtDownloadReq req = new DhtDownloadReq();
        req.setMediaTypeDesc("IMAGE");
        req.setIgonreMediaIdConvert(true);
        req.setMediaId("N_202508_07_1a88a44dd657466e944a365dcf90733f.jpeg");
        String resp = dhtCommonService.download(req);
        System.out.println(JSONUtil.toJsonStr(resp));
    }

    @Test
    public void simpleQuery() {
        DhtUserResp resp1 = dhtCommonService.getUserByMobile("15007174733");
        DhtSimpleQueryReq dhtSimpleQueryReq = DhtSimpleQueryReq.builder()
                .currentOpenUserId(resp1.getEmpList().get(0).getOpenUserId())
                .data(DhtSimpleQueryReq.DataDTO.builder()
                        .dataObjectApiName("MtCurrencyObj")
                        .searchQueryInfo(DhtSimpleQueryReq.DataDTO.SearchQueryInfoDTO.builder()
                                .filters(Arrays.asList(DhtSimpleQueryReq.DataDTO.SearchQueryInfoDTO.FiltersDTO.builder()
                                        .operator("eq")
                                        .fieldName("currency_code")
                                        .fieldValues(Arrays.asList("USD"))
                                        .build()))
                                .build())
                        .fieldProjection(Arrays.asList("currency_code","name","symbol"))
                        .build())
                .build();
        String resp = dhtCommonService.simpleQuery(dhtSimpleQueryReq);
        System.out.println(JSONUtil.toJsonStr(resp));
    }
}