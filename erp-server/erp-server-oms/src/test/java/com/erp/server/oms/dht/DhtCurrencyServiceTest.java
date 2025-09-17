package com.erp.server.oms.dht;

import cn.hutool.json.JSONUtil;
import com.erp.server.oms.ErpServerOmsApplication;
import com.sdk.oms.dht.dto.DhtBaseResp;
import com.sdk.oms.dht.dto.req.*;
import com.sdk.oms.dht.dto.resp.DhtQueryCurrencyResp;
import com.sdk.oms.dht.dto.resp.DhtQueryCustomerResp;
import com.sdk.oms.dht.dto.resp.DhtUserResp;
import com.sdk.oms.dht.service.DhtCommonService;
import com.sdk.oms.dht.service.DhtCurrencyService;
import com.sdk.oms.dht.service.DhtCustomerService;
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
public class DhtCurrencyServiceTest {

    @Resource
    private DhtCommonService dhtCommonService;

    @Resource
    private DhtCurrencyService dhtCurrencyService;

    @Test
    public void queryCurrency() {
        DhtBaseResp<DhtQueryCurrencyResp> resp = dhtCurrencyService.queryCurrency();
        System.out.println(JSONUtil.toJsonStr(resp));
    }

    @Test
    public void createCurrency() {
        DhtUserResp dhtUserResp = dhtCommonService.getUserByMobile("15007174733");
        DhtCreateCurrencyReq dhtCreateCurrencyReq = new DhtCreateCurrencyReq();
        dhtCreateCurrencyReq.setCurrentOpenUserId(dhtUserResp.getEmpList().get(0).getOpenUserId());
        dhtCreateCurrencyReq.setData(DhtCreateCurrencyReq.DataDTO.builder()
                .currencyCode("EUR")
                        .exchangeRate("7")
                .build());
        DhtBaseResp<String> resp = dhtCurrencyService.createCurrency(dhtCreateCurrencyReq);
        System.out.println(JSONUtil.toJsonStr(resp));
    }

    @Test
    public void updateCurrency() {
        DhtUserResp dhtUserResp = dhtCommonService.getUserByMobile("15007174733");
        DhtUpdateCurrencyReq dhtCreateCurrencyReq = new DhtUpdateCurrencyReq();
        dhtCreateCurrencyReq.setCurrentOpenUserId(dhtUserResp.getEmpList().get(0).getOpenUserId());
        DhtUpdateCurrencyReq.DataDTO dataDTO = new DhtUpdateCurrencyReq.DataDTO();
        dataDTO.setExchangeRateList(Arrays.asList(DhtUpdateCurrencyReq.DataDTO.ExchangeRateListDTO.builder()
                .currencyCode("CAD")
                .exchangeRate("5.1")
                .build()));
        dhtCreateCurrencyReq.setData(dataDTO);
        DhtBaseResp<String> resp = dhtCurrencyService.updateCurrency(dhtCreateCurrencyReq);
        System.out.println(JSONUtil.toJsonStr(resp));
    }
}