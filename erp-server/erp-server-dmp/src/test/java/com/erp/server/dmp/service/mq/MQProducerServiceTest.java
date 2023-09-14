package com.erp.server.dmp.service.mq;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.constant.MongoTableNameContant;
import com.erp.model.dmp.entity.DmpOrderItemEntity;
import com.erp.model.dmp.entity.DmpSkuCostEntity;
import com.erp.model.dmp.gyy.GyyDeliveryDetailEntity;
import com.erp.server.dmp.ErpServerDmpApplication;
import com.erp.server.dmp.pull.mapper.DmpOrderItemMapper;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.DmpOrderItemService;
import com.erp.server.dmp.service.DmpSkuCostService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @Author Cloud
 * @Date 2023/2/3 17:14
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerDmpApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Profile("dev")
public  class MQProducerServiceTest {

//    @Resource
//    private MQProducerService mQProducerService;

    @Resource
    private DmpOrderItemService dmpOrderItemService;

    @Resource
    private DmpOrderItemMapper dmpOrderItemMapper;

    @Resource
    private DmpSkuCostService dmpSkuCostService;

    @Test
    public void syncSendMsg() {
        List<DmpSkuCostEntity> dmpSkuCostEntities = dmpOrderItemMapper.listDmpSkuCostEntity();
        dmpSkuCostEntities.forEach(req -> {
            req.setId(IdWorker.getIdStr());
        });
        dmpSkuCostService.saveBatch(dmpSkuCostEntities);
//        mQProducerService.syncSendMsg("testId2", RocketMqTopic.DMP_ERP_ORDER_TOPIC, "tag2", "{'key':'value2'}","dmp jindie");


    }

    @Test
    public void testSendBatch() {
        List<DmpOrderItemEntity> itemEntityList = dmpOrderItemService.listByIds(Arrays.asList("1679163638713159686"));
        List<DmpOrderItemEntity> itemEntityList1 = dmpOrderItemService.splitOrderItem(itemEntityList);
        System.out.println(itemEntityList1);

//

//        List<CfgApiFieldMapController.TestMq> list = IntStream.rangeClosed(1, 10)
//                .mapToObj(x -> new CfgApiFieldMapController.TestMq(String.valueOf(x), LocalDateTime.now(), Arrays.asList(String.valueOf(x)), "tag2"))
//                .collect(Collectors.toList());
//        mQProducerService.sendBachMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, "tag2", list);
    }

    @Test
    public void asyncSendMsg() {
    }

    @Test
    public void oneWaySendMsg() {
    }

    @Test
    public void testOneWaySendMsg() {
        String activeProfile = SpringUtil.getActiveProfile();
        System.out.println("activeProfile = " + activeProfile);
    }

    @Test
    public void sendEntity() {


    }
    @Resource
    private MongoService mongoService;
    @Test
    public void addMongoEntity() throws Exception {
        GyyDeliveryDetailEntity entity = new GyyDeliveryDetailEntity();
        entity.set_id("test002");
        mongoService.saveMongoData(entity, MongoTableNameContant.ORIGINAL_GYY_DELIVERY_DETAIL);
        throw new RuntimeException("手动增加异常");

    }



}