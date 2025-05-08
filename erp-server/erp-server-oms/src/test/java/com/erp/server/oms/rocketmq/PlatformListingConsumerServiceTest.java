package com.erp.server.oms.rocketmq;

import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.PlatformProductDTO;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.msg.dto.NoticeMsgInfoDTO;
import com.erp.model.msg.enums.NoticeTypeEnum;
import com.erp.server.oms.ErpServerOmsApplication;
import com.erp.server.oms.rocketmq.consumer.PlatformListingConsumerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Priority;
import javax.annotation.Resource;
import java.util.Collections;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerOmsApplication.class})
@TestPropertySource(properties = {"spring.cloud.nacos.discovery.namespace=dev"})
@Profile("dev")
public class PlatformListingConsumerServiceTest {

    @Resource
    private PlatformListingConsumerService platformListingConsumerService;
    @Resource
    private MQProducerService mqProducerService;

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
    @Test
    public void handle2() {
        NoticeMsgInfoDTO msgInfoDTO = new NoticeMsgInfoDTO();
        msgInfoDTO.setReceiverUserIds(Collections.singletonList("1704379714094764034"));
        msgInfoDTO.setContent("消息测试1");
        msgInfoDTO.setTitle("消息测试2");
        msgInfoDTO.setNoticeTypeEnum(NoticeTypeEnum.OMS_TASK);
        mqProducerService.sendNoticeMsg(msgInfoDTO);
    }
}