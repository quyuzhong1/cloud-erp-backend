package com.erp.server.dmp.service.mq;

import com.common.core.constant.RocketMqTopic;
import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.gyy.GyyDeliveryDetailEntity;
import com.erp.server.dmp.ErpServerDmpApplication;
import com.erp.server.dmp.pull.mongo.MongoService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TODO
 *
 * @Author Cloud
 * @Date 2023/2/3 17:14
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerDmpApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Profile("dev")
class MQProducerServiceTest {

    @Resource
    private MQProducerService mQProducerService;

    @Test
    void syncSendMsg() {
        mQProducerService.syncSendMsg("testId2", RocketMqTopic.DMP_TOPIC, "tag2", "{'key':'value2'}","dmp jindie");
    }

    @Test
    void testSyncSendMsg() {
    }

    @Test
    void asyncSendMsg() {
    }

    @Test
    void oneWaySendMsg() {
    }

    @Test
    void testOneWaySendMsg() {
    }

    @Test
    void sendEntity() {
    }
    @Resource
    private MongoService mongoService;
    @Test
    @Transactional(rollbackFor = Exception.class)
    void addMongoEntity() throws Exception {
        GyyDeliveryDetailEntity entity = new GyyDeliveryDetailEntity();
        entity.set_id("test002");
        mongoService.saveMongoData(entity, MongoTableNameContant.ORIGINAL_GYY_DELIVERY_DETAIL);
        throw new RuntimeException("手动增加异常");

    }
}