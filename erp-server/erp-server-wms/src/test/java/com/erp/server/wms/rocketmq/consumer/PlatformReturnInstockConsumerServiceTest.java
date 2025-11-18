package com.erp.server.wms.rocketmq.consumer;

import com.erp.server.wms.ErpServerWmsApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerWmsApplication.class})
public class PlatformReturnInstockConsumerServiceTest {

    @Resource
    private KingdeeB2CSoOutstockConsumer service;

    @Resource
    private RestCloudPlatformNewReturnInstockConsumerService restCloudPlatformNewReturnInstockConsumerService;

    @Test
    public void handleTest() {
        String json = "{\n" +
                "  \"sourceId\": \"\",\n" +
                "  \"platformReturnOrderNo\": \"WRI2025080100002\",\n" +
                "  \"reason\": \"\",\n" +
                "  \"putawayTime\": \"2025-08-01 09:57:07\",\n" +
                "  \"platform\": \"iml\",\n" +
                "  \"orderReferenceNo\": \"\",\n" +
                "  \"authId\": \"1976194554104430593\",\n" +
                "  \"warehouseCode\": \"\",\n" +
                "  \"dmpOutputTaskRecordDataId\": \"1180152941215232001\",\n" +
                "  \"createTime\": \"2025-10-27 11:47:32\",\n" +
                "  \"productDetailList\": [\n" +
                "    {\n" +
                "      \"realQty\": \"1\",\n" +
                "      \"productSku\": \"22298-GD-V1S\",\n" +
                "      \"mustQty\": \"1\",\n" +
                "      \"receiveQty\": \"1\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"platformOrderNo\": \"\",\n" +
                "  \"returnLogisticCode\": \"46343175619-2\",\n" +
                "  \"dmpOutputTaskRecordId\": \"1181655983894835200\",\n" +
                "  \"returnType\": \"claim\",\n" +
                "  \"status\": \"\"\n" +
                "}";
        restCloudPlatformNewReturnInstockConsumerService.handle(json);
    }
}