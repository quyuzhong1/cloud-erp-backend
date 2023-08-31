package com.erp.server.dmp.push.consumer;


import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.server.dmp.push.service.business.KingdeeOtherOutstockConsumerService;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Will
 * @description: 其它出库单推送至金蝶
 * @date: 2023/5/24 18:17
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, selectorExpression = "kingdee_other_outstock_tag", consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_OTHER_OUTSTOCK, consumeMode = ConsumeMode.ORDERLY)
public class KingdeeOtherOutstockConsumer implements RocketMQListener<Map<String, Object>> {
    @Resource
    private KingdeeOtherOutstockConsumerService kingdeeOtherOutstockConsumerService;

    public static void main(String[] args) {

        Map<String, Object> resultMap = new LinkedHashMap<>();
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.STK_MISDELIVERY.getCode());
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FBillNo = '%s'", "QTCK23060500001"));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FID,FStockerId.FStaffNumber";
        List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 100, 1, 0);
        System.out.println(queryList);

    }

    @Override
    public void onMessage(Map<String, Object> map) {
        try {
            kingdeeOtherOutstockConsumerService.executeConsumer(map);
        } catch (Exception e) {
            log.error("KingdeeOtherOutstockConsumer>>>onMessage>>>map ={}>>>e={}", map, e);
        }

    }

}