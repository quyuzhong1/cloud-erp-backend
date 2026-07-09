package com.erp.server.wms.rocketmq.consumer;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.PlatformInboundDTO;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.erp.server.wms.ErpServerWmsApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerWmsApplication.class})
public class PlatformInboundConsumerServiceTest {

    @Resource
    private KingdeeB2CSoOutstockConsumer service;

    @Resource
    private PlatformInboundConsumerService platformInboundConsumerService;

    @Test
    public void handleTest() {
        String json = "{\"sourceCode\":\"FHD260609000001\",\"dmpOutputTaskRecordDataId\":\"2064233384325967874\",\"receivingDataList\":[{\"receiveTime\":\"2026-06-09T11:35:12\",\"productSku\":\"0605+2529+2821\",\"defectiveProductFlag\":false,\"thirdId\":\"1197_2026-06-09_0605+2529+2821\",\"receiveQty\":1,\"receiveUser\":\"管理员\"}],\"hasReceivedData\":true,\"receivingStatus\":\"toBeSigned\",\"provider\":\"wego\",\"warehousePlatformType\":\"overseasWarehouse\",\"dmpOutputTaskRecordId\":\"2064233385377861634\",\"receivingCode\":\"Q202606090001\",\"items\":[{\"productSku\":\"0605+2529+2821\",\"receivedQuantity\":1}],\"downloadTime\":\"2026-06-09T11:35:12\",\"platform\":\"wego\"}";

        platformInboundConsumerService.handle(json);
    }
}