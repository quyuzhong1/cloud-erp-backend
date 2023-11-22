package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.json.JSONObject;
import com.common.business.dto.PlatformWarehouseDTO;
import com.erp.server.wms.ErpServerWmsApplication;
import com.erp.server.wms.service.OverseasProviderService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerWmsApplication.class})
public class PlatformWarehouseConsumerServiceTest {

    @Resource
    private PlatformWarehouseConsumerService service;

    @Test
    public void handleTest() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("providerErpId","2");
        jsonObject.set("warehouseCode","code");
        jsonObject.set("warehouseName","name2");
        jsonObject.set("countryCode","KE");
        jsonObject.set("warehousePlatformType","overseasWarehouse");
        service.handle(jsonObject);
    }
}