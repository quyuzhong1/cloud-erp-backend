package com.erp.server.oms.dht;

import cn.hutool.json.JSONUtil;
import com.erp.server.oms.ErpServerOmsApplication;
import com.sdk.oms.dht.dto.DhtBaseResp;
import com.sdk.oms.dht.dto.req.*;
import com.sdk.oms.dht.dto.resp.DhtQueryCustomerAccountResp;
import com.sdk.oms.dht.dto.resp.DhtQueryCustomerAddressResp;
import com.sdk.oms.dht.dto.resp.DhtUserResp;
import com.sdk.oms.dht.service.DhtCommonService;
import com.sdk.oms.dht.service.DhtCustomerAccountService;
import com.sdk.oms.dht.service.DhtCustomerAddressService;
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
public class DhtCustomerAccountServiceTest {

    @Resource
    private DhtCommonService dhtCommonService;

    @Resource
    private DhtCustomerAccountService dhtCustomerAccountService;

    @Test
    public void queryCustomerAddress() {
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
                                        .fieldName("name")
                                        .fieldValues(Arrays.asList("NCA20250814-0015"))
                                        .operator("EQ")
                                        .build()
                        ))
                        .fieldProjection(Arrays.asList("_id","account_balance", "name", "occupied_amount", "fund_account_id","customer_id","record_type","life_status"))
                        .build())
                .build();
        req.setData(dataDTO);
        DhtBaseResp<DhtQueryCustomerAccountResp> resp = dhtCustomerAccountService.queryCustomerAccount(req);
        System.out.println(JSONUtil.toJsonStr(resp));
    }

    @Test
    public void queryCustomerAccountObj() {
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
                                        .fieldName("customer_name")
                                        .fieldValues(Arrays.asList("广州市增城潮兴商行"))
                                        .operator("EQ")
                                        .build()
                        ))
                        .fieldProjection(Arrays.asList("_id","account_balance", "name", "occupied_amount", "fund_account_id","customer_id","record_type","life_status"))
                        .build())
                .build();
        req.setData(dataDTO);
        DhtBaseResp<DhtQueryCustomerAccountResp> resp = dhtCustomerAccountService.queryCustomerAccountObj(req);
        System.out.println(JSONUtil.toJsonStr(resp));
    }
}