package com.erp.server.dmp.service.mq;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.MongoTableNameContant;
import com.common.business.dto.UniqueDto;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.utils.MapUtil;
import com.erp.model.dmp.entity.DmpOrderItemSplitEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.gyy.GyyDeliveryDetailEntity;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonFbaShipmentDTO;
import com.erp.server.dmp.ErpServerDmpApplication;
import com.erp.server.dmp.pull.mapper.DmpOrderItemSplitMapper;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.DmpOrderItemSplitService;
import com.erp.server.dmp.service.DmpSkuCostService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
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
    private DmpOrderItemSplitService dmpOrderItemSplitService;

    @Resource
    private DmpOrderItemSplitMapper dmpOrderItemSplitMapper;

    @Resource
    private DmpSkuCostService dmpSkuCostService;

    @Resource
    private RedisTemplate redisTemplate;


    @Test
    public void redisAddTest(){
        String groupId = "test:666666:order";
        List<String> list = Arrays.asList(
                "{\"apiCode\":\"amazon.test.order.api\",\"apiName\":\"亚马逊订单接口\",\"apiParam\":{},\"billType\":\"order\",\"createTime\":\"2023-12-15T12:10:00.736\",\"dictPlatform\":\"Amazon\",\"id\":\"1735512507616858113\",\"intervalTime\":1800,\"lastTime\":\"2024-01-02T12:30:00\",\"nextTime\":\"2024-01-03T00:30:00\",\"operateType\":\"pull\",\"platformCategory\":\"third_system\",\"retryTimes\":0,\"shopId\":\"1735512178561126404\",\"shopName\":\"欧洲5站土耳其\",\"status\":1}",
                "{\"apiCode\":\"amazon.test.order.api\",\"apiName\":\"亚马逊订单接口\",\"apiParam\":{},\"billType\":\"order\",\"createTime\":\"2023-12-15T12:10:00.826\",\"dictPlatform\":\"Amazon\",\"id\":\"1735512507994345474\",\"intervalTime\":1800,\"lastTime\":\"2024-01-02T12:30:00\",\"nextTime\":\"2024-01-03T00:30:00\",\"operateType\":\"pull\",\"platformCategory\":\"third_system\",\"retryTimes\":0,\"shopId\":\"1735512178565320709\",\"shopName\":\"欧洲5站西班牙\",\"status\":1}"
        );
        for (String s : list) {
            redisTemplate.boundListOps(groupId).leftPush(s);
        }
    }

    @Test
    public void redisARemoveTest(){
        String groupId = "test:666666:order";
        List<String> list = Arrays.asList(
                "{\"apiCode\":\"amazon.test.order.api\",\"apiName\":\"亚马逊订单接口\",\"apiParam\":{},\"billType\":\"order\",\"createTime\":\"2023-12-15T12:10:00.736\",\"dictPlatform\":\"Amazon\",\"id\":\"1735512507616858113\",\"intervalTime\":1800,\"lastTime\":\"2024-01-02T12:30:00\",\"nextTime\":\"2024-01-03T00:30:00\",\"operateType\":\"pull\",\"platformCategory\":\"third_system\",\"retryTimes\":0,\"shopId\":\"1735512178561126404\",\"shopName\":\"欧洲5站土耳其\",\"status\":1}"
        );
        for (String s : list) {
            redisTemplate.boundListOps(groupId).remove(0, s);
        }
    }

    @Test
    public void syncSendMsg() {
//        List<DmpSkuCostEntity> dmpSkuCostEntities = dmpOrderItemMapper.listDmpSkuCostEntity();
//        dmpSkuCostEntities.forEach(req -> {
//            req.setId(IdWorker.getIdStr());
//        });
//        dmpSkuCostService.saveBatch(dmpSkuCostEntities);
//        mQProducerService.syncSendMsg("testId2", RocketMqTopic.DMP_ERP_ORDER_TOPIC, "tag2", "{'key':'value2'}","dmp jindie");


    }

    @Test
    public void testSendBatch() {
        List<DmpOrderItemSplitEntity> itemEntityList = dmpOrderItemSplitService.listByIds(Arrays.asList("1679163638713159686"));
        List<DmpOrderItemSplitEntity> itemEntityList1 = dmpOrderItemSplitService.splitOrderItem(itemEntityList, PlatformEnum.MABANG.getDesc());
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

    @Test
    public void testMongoService() {
        // 根据状态查询未下载数据
        PlatformAmazonFbaShipmentDTO orderMongoDTO = PlatformAmazonFbaShipmentDTO.getByDownloadStatus(0);
        List<PlatformAmazonFbaShipmentDTO> entityList = mongoService.findMongoData(orderMongoDTO, 1, 1, MongoTableNameContant.THIRD_SYSTEM_AMAZON_FBA_SHIPMENT, PlatformAmazonFbaShipmentDTO.class);
        for (PlatformAmazonFbaShipmentDTO dto : entityList) {
            dto.setDownloadStatus(1);
            // 修改数据
            UniqueDto updateDto = UniqueDto.getUniqId(dto.getUniqueId());
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(dto), MapUtil.class);
            String category = PlatformCategoryEnum.THIRD_SYSTEM.getCode();
            String platform = PlatformDictEnum.AMAZON.getCode();
            String business = BusinessTypeEnum.FBA_SHIPMENT.getCode();
            Class<? extends PlatformAmazonFbaShipmentDTO> tClass = dto.getClass();
            String tableName = StrUtil.format("{}_{}_{}", category, platform, business);
            mongoService.updateMongoData(updateDto, mapUtil, tableName, tClass);
        }

    } 


}