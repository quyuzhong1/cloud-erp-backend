package com.erp.server.wms.rocketmq.consumer;

import com.erp.server.wms.ErpServerWmsApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerWmsApplication.class})
public class PlatformInventoryConsumerServiceTest {

    @Resource
    private PlatformInventoryConsumerService service;

    @Test
    public void sendWarnMsg() {
//        service.sendWarnMsg("1729403606890713089");
    }
}