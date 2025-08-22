package com.erp.server.oms.dht;

import cn.hutool.json.JSONUtil;
import com.erp.server.oms.ErpServerOmsApplication;
import com.sdk.oms.dht.dto.DhtAuthDTO;
import com.sdk.oms.dht.dto.DhtBaseResp;
import com.sdk.oms.dht.dto.req.*;
import com.sdk.oms.dht.dto.resp.DhtQueryCustomerResp;
import com.sdk.oms.dht.dto.resp.DhtUserResp;
import com.sdk.oms.dht.service.DhtCommonService;
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
public class DhtCustomerServiceTest {

    @Resource
    private DhtCommonService dhtCommonService;

    @Resource
    private DhtCustomerService dhtCustomerService;

    @Test
    public void createCustomer() {
        DhtOperationCustomerReq dhtOperationCustomerReq = new DhtOperationCustomerReq();
        DhtUserResp resp1 = dhtCommonService.getUserByMobile("15007174733");
        dhtOperationCustomerReq.setCurrentOpenUserId(resp1.getEmpList().get(0).getOpenUserId());
        DhtOperationCustomerReq.DataDTO dataDTO = new DhtOperationCustomerReq.DataDTO();
        dataDTO.setObjectData(DhtOperationCustomerReq.DataDTO.ObjectDataDTO.builder()
                        .dataObjectApiName("AccountObj")
                        .name("test123")
//                        .salesOrganization("ORG01")
                        .erpCustomerCode("TEST_CUST_20250821")
                        .currency("CNY")
                        .country("CN")
                        .objectDescribeApiName("AccountObj")
                        .customerStatus("option_access_approval__c")
                        .recordType("default__c")
                .build());
        dhtOperationCustomerReq.setData(dataDTO);
        DhtBaseResp<String> resp = dhtCustomerService.createCustomer(dhtOperationCustomerReq);
        System.out.println(JSONUtil.toJsonStr(resp));
    }

    @Test
    public void updateCustomer() {
        DhtOperationCustomerReq dhtOperationCustomerReq = new DhtOperationCustomerReq();
        DhtUserResp resp1 = dhtCommonService.getUserByMobile("15007174733");
        dhtOperationCustomerReq.setCurrentOpenUserId(resp1.getEmpList().get(0).getOpenUserId());
        dhtOperationCustomerReq.setTriggerWorkFlow(false);
        dhtOperationCustomerReq.setTriggerApprovalFlow(false);
        DhtOperationCustomerReq.DataDTO dataDTO = new DhtOperationCustomerReq.DataDTO();
        dataDTO.setObjectData(DhtOperationCustomerReq.DataDTO.ObjectDataDTO.builder()
                .dataObjectApiName("AccountObj")
                .name("wjtest123")
                        .tel("123456798")
                                .id("68a6eae8c832c800064d2ac6")
//                        .salesOrganization("ORG01")
                .erpCustomerCode("Teset-Cust-001")
                .currency("CNY")
                .country("CN")
                .objectDescribeApiName("AccountObj")
                .customerStatus("option_access_approval__c")
                .recordType("default__c")
                .build());
        dhtOperationCustomerReq.setData(dataDTO);
        DhtBaseResp<String> resp = dhtCustomerService.updateCustomer(dhtOperationCustomerReq);
        System.out.println(JSONUtil.toJsonStr(resp));
    }

    @Test
    public void queryCustomer() {
        DhtQueryCustomerReq req = new DhtQueryCustomerReq();
        DhtUserResp resp1 = dhtCommonService.getUserByMobile("15007174733");
        req.setCurrentOpenUserId(resp1.getEmpList().get(0).getOpenUserId());
        DhtQueryCustomerReq.DataDTO dataDTO = DhtQueryCustomerReq.DataDTO.builder()
                .dataObjectApiName("AccountObj")
                .findExplicitTotalNum(false)
                .searchQueryInfo(DhtQueryCustomerReq.DataDTO.SearchQueryInfoDTO.builder()
                        .offset(0)
                        .limit(10)
                        .fieldProjection(null)
                        .filters(null)
                        .orders(null)
                        .filters(Arrays.asList(
                                DhtQueryCustomerReq.DataDTO.SearchQueryInfoDTO.FiltersDTO.builder()
                                        .fieldName("erp_number__c")
                                        .fieldValues(Arrays.asList("TEST_CUST_20250821"))
                                        .operator("EQ")
                                        .build()
                        ))
                        .fieldProjection(Arrays.asList("_id","erp_number__c","account_no", "name", "object_describe_api_name", "record_type", "customer_status__c","account_status"))
                        .build())
                .build();
        req.setData(dataDTO);
        DhtBaseResp<DhtQueryCustomerResp> resp = dhtCustomerService.queryCustomer(req);
        System.out.println(JSONUtil.toJsonStr(resp));
    }


    @Test
    public void invalidCustomer() {
        DhtInvalidCustomerReq req = new DhtInvalidCustomerReq();
        DhtUserResp resp1 = dhtCommonService.getUserByMobile("15007174733");
        req.setCurrentOpenUserId(resp1.getEmpList().get(0).getOpenUserId());
        DhtInvalidCustomerReq.DataDTO dataDTO = DhtInvalidCustomerReq.DataDTO.builder()
                .objectDataId("68a6f6cd9ed17d0007831c27")
                .build();
        req.setData(dataDTO);
        DhtBaseResp<String> resp = dhtCustomerService.invalidCustomer(req);
        System.out.println(JSONUtil.toJsonStr(resp));
    }

    @Test
    public void enableCustomer() {
        DhtEnableCustomerReq req = new DhtEnableCustomerReq();
        DhtUserResp resp1 = dhtCommonService.getUserByMobile("15007174733");
        req.setCurrentOpenUserId(resp1.getEmpList().get(0).getOpenUserId());
        DhtEnableCustomerReq.DataDTO dataDTO = DhtEnableCustomerReq.DataDTO.builder()
                .idList(Arrays.asList("68a6f6cd9ed17d0007831c27"))
                .build();
        req.setData(dataDTO);
        DhtBaseResp<String> resp = dhtCustomerService.enableCustomer(req);
        System.out.println(JSONUtil.toJsonStr(resp));
    }

    @Test
    public void deleteCustomer() {
        DhtDeleteCustomerReq req = new DhtDeleteCustomerReq();
        DhtUserResp resp1 = dhtCommonService.getUserByMobile("15007174733");
        req.setCurrentOpenUserId(resp1.getEmpList().get(0).getOpenUserId());
        DhtDeleteCustomerReq.DataDTO dataDTO = DhtDeleteCustomerReq.DataDTO.builder()
                .idList(Arrays.asList("68a6f6cd9ed17d0007831c27"))
                .build();
        req.setData(dataDTO);
        DhtBaseResp<String> resp = dhtCustomerService.deleteCustomer(req);
        System.out.println(JSONUtil.toJsonStr(resp));
    }
}