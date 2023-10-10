package com.erp.server.dmp.push.consumer;

import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.server.dmp.push.service.business.KingdeeSubcontractChangeConsumerService;
import com.erp.server.dmp.utils.KingdeeApiUtils;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author Will
 * @version 1.0
 * @date 2023/4/20 11:12
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, selectorExpression = "kingdee_subcontract_change_tag", consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_SUBCONTRACT_CHANGE,consumeMode = ConsumeMode.ORDERLY)
public class KingdeeSubcontractChangeConsumer implements RocketMQListener<Map<String, Object>> {

    @Resource
    private KingdeeSubcontractChangeConsumerService kingdeeSubcontractChangeConsumerService;

    public static void main(String[] args) {

        Map<String, Object> resultMap = new LinkedHashMap<>();
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.SUB_REQCHANGE.getCode());
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FBillNo = '%s'", "SUBCH000003"));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FId,FBillNo,FBillType.FNUMBER,FChangeType";
        List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 100, 1,0);
        System.out.println(queryList);


    }

    @Override
    public void onMessage(Map<String, Object> map) {
        try {
            kingdeeSubcontractChangeConsumerService.executeConsumer(map);
        }catch (Exception e){
            log.error("KingdeeSubcontractChangeConsumer>>>onMessage>>>map ={}>>>e={}", map, e);
        }


    }
}
