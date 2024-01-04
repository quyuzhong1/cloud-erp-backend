package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.json.JSONObject;
import com.common.business.dto.PlatformWarehouseDTO;
import com.erp.server.wms.ErpServerWmsApplication;
import com.erp.server.wms.service.OverseasProviderService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerWmsApplication.class})
public class PlatformWarehouseConsumerServiceTest {

    @Resource
    private PlatformWarehouseConsumerService service;

    @Resource
    private PlatformOutboundConsumerService outboundConsumerService;

    @Test
    public void handleTest() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("orderStatus","shipped");
        jsonObject.set("uniqueId","f89cef75e5a0fae06d371599726aaeb2");
        jsonObject.set("platform","goodcang");
        jsonObject.set("dmpSyncTaskId","1742012512032788481");
        jsonObject.set("referenceNo","XSDS23122700025");
        jsonObject.set("orderCode","G1149-231227-0119");
        jsonObject.set("provider","goodcang");
        jsonObject.set("warehousePlatformType","overseasWarehouse");
        outboundConsumerService.handle(jsonObject);
    }
}