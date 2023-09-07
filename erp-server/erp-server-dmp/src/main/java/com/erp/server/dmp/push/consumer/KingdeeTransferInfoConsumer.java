package com.erp.server.dmp.push.consumer;


import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.ApiSendStatusEnum;
import com.erp.model.dmp.enums.KingdeeDocStatusEnum;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.server.dmp.push.service.business.KingdeeTransferInfoConsumerService;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @description: 直接调拨单推送至金蝶
 * @author Will
 * @date: 2023/5/24 18:17
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, selectorExpression = "kingdee_transfer_info_tag", consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_TRANSFER_INFO,consumeMode = ConsumeMode.ORDERLY)
public class KingdeeTransferInfoConsumer implements RocketMQListener<Map<String, Object>> {
    @Resource
    private KingdeeTransferInfoConsumerService kingdeeTransferInfoConsumerService;

    public static void main(String[] args) {

        Map<String, Object> resultMap = new LinkedHashMap<>();
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.STK_TRANSFERDIRECT.getCode());
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FBillNo = '%s'", "ZJDB23060500005"));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FID,FTransferDirect";
        List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 100, 1,0);
        System.out.println(queryList);

    }

    @Override
    public void onMessage(Map<String, Object> map) {
        try {
            kingdeeTransferInfoConsumerService.executeConsumer(map);
        }catch (Exception e){
            log.error("KingdeeTransferInfoConsumer>>>onMessage>>>map ={}>>>e={}", map, e);
        }

    }

}