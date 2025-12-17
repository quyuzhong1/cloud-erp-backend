package com.erp.server.wms.rocketmq.consumer;

import com.erp.server.wms.ErpServerWmsApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerWmsApplication.class})
public class PlatformOutboundConsumerServiceTest {

    @Resource
    private KingdeeB2CSoOutstockConsumer service;

    @Resource
    private PlatformOutboundConsumerService platformOutboundConsumerService;

    @Test
    public void handleTest() {
        String json = "{\"dmpOutputTaskRecordDataId\":\"1181648961531031553\",\"interceptStatus\":\"\",\"referenceNo\":\"WFHD251031000008\",\"provider\":\"iml\",\"dmpOutputTaskRecordId\":\"1183393655428493312\",\"orderStatus\":\"shipped\",\"orderCode\":\"OT80565-20251031-000012\",\"thirdOrderStatus\":\"COMPLETE_OUTBOUND\",\"abnormalProblemReason\":\"\",\"outBoundTime\":\"2025-11-04 00:00:00\",\"trackNo\":\"T-R-202510310002\",\"authId\":\"1976194554104430593\"}";
        platformOutboundConsumerService.handle(json);
    }
}