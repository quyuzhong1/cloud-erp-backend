package com.erp.server.oms.dht;

import cn.hutool.json.JSONUtil;
import com.erp.server.oms.ErpServerOmsApplication;
import com.sdk.oms.dht.dto.DhtBaseResp;
import com.sdk.oms.dht.dto.req.*;
import com.sdk.oms.dht.dto.resp.DhtQueryCustomerAddressResp;
import com.sdk.oms.dht.dto.resp.DhtQueryCustomerResp;
import com.sdk.oms.dht.dto.resp.DhtUserResp;
import com.sdk.oms.dht.service.DhtCommonService;
import com.sdk.oms.dht.service.DhtCustomerAddressService;
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
public class DhtCustomerAddressServiceTest {

    @Resource
    private DhtCommonService dhtCommonService;

    @Resource
    private DhtCustomerAddressService dhtCustomerAddressService;

    @Test
    public void createCustomerAddress() {
        DhtOperationCustomerAddressReq dhtOperationCustomerReq = new DhtOperationCustomerAddressReq();
        DhtUserResp resp1 = dhtCommonService.getUserByMobile("15007174733");
        dhtOperationCustomerReq.setCurrentOpenUserId(resp1.getEmpList().get(0).getOpenUserId());

        DhtOperationCustomerAddressReq.DataDTO dataDTO = new DhtOperationCustomerAddressReq.DataDTO();
        DhtOperationCustomerAddressReq.DataDTO.ObjectDataDTO objectDataDTO = DhtOperationCustomerAddressReq.DataDTO.ObjectDataDTO.builder()
                .addType("other")
                .recordType("default__c")
                .accountId("68a81d1efc8aab0007f43cea")
                .erpCustomerAddressCode("TEST_CUST_ADDR_20250825")
                .address("test address")
                .remark("test remark")
                .isDefaultAddress(true)
                .phone("12345678901")
                .build();
        dataDTO.setObjectData(objectDataDTO);
        dhtOperationCustomerReq.setData(dataDTO);
        DhtBaseResp<String> resp = dhtCustomerAddressService.createCustomerAddress(dhtOperationCustomerReq);
        System.out.println(JSONUtil.toJsonStr(resp));
    }


    @Test
    public void updateCustomerAddress() {
        DhtOperationCustomerAddressReq dhtOperationCustomerReq = new DhtOperationCustomerAddressReq();
        DhtUserResp resp1 = dhtCommonService.getUserByMobile("15007174733");
        dhtOperationCustomerReq.setCurrentOpenUserId(resp1.getEmpList().get(0).getOpenUserId());

        DhtOperationCustomerAddressReq.DataDTO dataDTO = new DhtOperationCustomerAddressReq.DataDTO();
        DhtOperationCustomerAddressReq.DataDTO.ObjectDataDTO objectDataDTO = DhtOperationCustomerAddressReq.DataDTO.ObjectDataDTO.builder()
                .addType("other")
                .id("68abd43e6647f10006146e6a")
                .recordType("default__c")
                .accountId("68a81d1efc8aab0007f43cea")
                .erpCustomerAddressCode("TEST_CUST_ADDR_202508222")
                .address("test address2")
                .remark("test remark2")
                .isDefaultAddress(true)
                .phone("12345678901")
                .build();
        dataDTO.setObjectData(objectDataDTO);
        dhtOperationCustomerReq.setData(dataDTO);
        DhtBaseResp<String> resp = dhtCustomerAddressService.updateCustomerAddress(dhtOperationCustomerReq);
        System.out.println(JSONUtil.toJsonStr(resp));
    }

    @Test
    public void queryCustomerAddress() {
        DhtCommonQueryReq req = new DhtCommonQueryReq();
        DhtUserResp resp1 = dhtCommonService.getUserByMobile("15007174733");
        req.setCurrentOpenUserId(resp1.getEmpList().get(0).getOpenUserId());
        DhtCommonQueryReq.DataDTO dataDTO = DhtCommonQueryReq.DataDTO.builder()
                .dataObjectApiName("AccountObj")
                .findExplicitTotalNum(false)
                .searchQueryInfo(DhtCommonQueryReq.DataDTO.SearchQueryInfoDTO.builder()
                        .offset(0)
                        .limit(10)
                        .fieldProjection(null)
                        .filters(null)
                        .orders(null)
                        .filters(Arrays.asList(
                                DhtCommonQueryReq.DataDTO.SearchQueryInfoDTO.FiltersDTO.builder()
                                        .fieldName("erp_customer_address_code__c")
                                        .fieldValues(Arrays.asList("TEST_CUST_ADDR_202508222"))
                                        .operator("EQ")
                                        .build()
                        ))
                        .fieldProjection(Arrays.asList("_id","erp_customer_address_code__c", "name", "object_describe_api_name", "record_type", "life_status"))
                        .build())
                .build();
        req.setData(dataDTO);
        DhtBaseResp<DhtQueryCustomerAddressResp> resp = dhtCustomerAddressService.queryCustomerAddress(req);
        System.out.println(JSONUtil.toJsonStr(resp));
    }


    @Test
    public void enableCustomerAddress() {
        DhtRecoverReq req = new DhtRecoverReq();
        DhtUserResp resp1 = dhtCommonService.getUserByMobile("15007174733");
        req.setCurrentOpenUserId(resp1.getEmpList().get(0).getOpenUserId());
        DhtRecoverReq.DataDTO dataDTO = DhtRecoverReq.DataDTO.builder()
                .idList(Arrays.asList("68abd43e6647f10006146e6a"))
                .build();
        req.setData(dataDTO);
        DhtBaseResp<String> resp = dhtCustomerAddressService.enableCustomerAddress(req);
        System.out.println(JSONUtil.toJsonStr(resp));
    }

    @Test
    public void invalidCustomerAddress() {
        DhtInvalidReq req = new DhtInvalidReq();
        DhtUserResp resp1 = dhtCommonService.getUserByMobile("15007174733");
        req.setCurrentOpenUserId(resp1.getEmpList().get(0).getOpenUserId());
        DhtInvalidReq.DataDTO dataDTO = DhtInvalidReq.DataDTO.builder()
                .objectDataId("68abd43e6647f10006146e6a")
                .build();
        req.setData(dataDTO);
        DhtBaseResp<String> resp = dhtCustomerAddressService.invalidCustomerAddress(req);
        System.out.println(JSONUtil.toJsonStr(resp));
    }


    @Test
    public void deleteCustomerAddress() {
        DhtDeleteReq req = new DhtDeleteReq();
        DhtUserResp resp1 = dhtCommonService.getUserByMobile("15007174733");
        req.setCurrentOpenUserId(resp1.getEmpList().get(0).getOpenUserId());
        DhtDeleteReq.DataDTO dataDTO = DhtDeleteReq.DataDTO.builder()
                .idList(Arrays.asList("68abd43e6647f10006146e6a"))
                .build();
        req.setData(dataDTO);
        DhtBaseResp<String> resp = dhtCustomerAddressService.deleteCustomerAddress(req);
        System.out.println(JSONUtil.toJsonStr(resp));
    }
}