package com.erp.server.dmp.push.consumer;


import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 对接金蝶入库单
 * @Author Luo_WG
 * @Date 2023/4/23 19:47
 **/
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, selectorExpression = "kingdee_purchase_stock_in_tag", consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_PURCHASE_STOCK_IN)
public class KingdeeStockInConsumer implements RocketMQListener<Map<String, Object>> {
    @Resource
    private KingdeeCommonService kingdeeCommonService;

    @Override
    public void onMessage(Map<String, Object> stringObjectMap) {
        //模块类型
        Integer type = ApiModuleTypeEnum.PURCHASE_RETURN_ORDER.getCode();
    }
}