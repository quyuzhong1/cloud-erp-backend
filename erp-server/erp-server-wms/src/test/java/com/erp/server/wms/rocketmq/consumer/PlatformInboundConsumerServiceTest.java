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
        String json = "{\"sourceCode\":\"297c29b44c82fcfdc0cb3d3b2becba55\",\"dmpOutputTaskRecordDataId\":\"1978567870124539906\",\"receivingDataList\":[{\"receiveTime\":\"2025-10-16T12:57:59\",\"productSku\":\"T163\",\"thirdId\":\"297c29b44c82fcfdc0cb3d3b2becba55\",\"receiveQty\":60}],\"hasReceivedData\":true,\"receivingStatus\":\"partialSigned\",\"provider\":\"damai\",\"warehousePlatformType\":\"overseasWarehouse\",\"dmpOutputTaskRecordId\":\"1978567870211624961\",\"receivingCode\":\"ASNVLV20250922000001\",\"items\":[{\"productSku\":\"T163\",\"receivedQuantity\":60}],\"downloadTime\":\"2025-10-16T12:57:59\",\"platform\":\"damai\"}";

        platformInboundConsumerService.handle(json);
    }
}