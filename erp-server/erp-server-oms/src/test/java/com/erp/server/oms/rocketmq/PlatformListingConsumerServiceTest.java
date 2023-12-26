package com.erp.server.oms.rocketmq;

import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.PlatformProductDTO;
import com.erp.server.oms.ErpServerOmsApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerOmsApplication.class})
public class PlatformListingConsumerServiceTest {

    @Resource
    private PlatformListingConsumerService platformListingConsumerService;

    @Test
    public void handle() {
        PlatformProductDTO dto = new PlatformProductDTO();
        dto.setPlatform("goodcang");
        dto.setPlatformSkuNo("QCA2JN-011200EU");
        dto.setPlatformSkuName("测试日41121246");
        dto.setProductImageUrl("1454566");
        dto.setProductSpec("log34444656534");
        JSONObject json = (JSONObject) JSONObject.toJSON(dto);
        platformListingConsumerService.handle(json);
    }
}