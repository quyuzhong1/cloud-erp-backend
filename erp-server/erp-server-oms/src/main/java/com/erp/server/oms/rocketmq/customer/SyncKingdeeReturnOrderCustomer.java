package com.erp.server.oms.rocketmq.customer;

import cn.hutool.json.JSONUtil;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.dmp.kingdee.KingdeeReturnOrderEntity;
import com.erp.server.oms.rocketmq.sync.oms.SyncSoReturnService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_TO_OMS_SALES_TOPIC, selectorExpression = "sync_kingdee_return_order_tag", consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_RETURN_ORDER_TO_OMS)
public class SyncKingdeeReturnOrderCustomer implements RocketMQListener<List<KingdeeReturnOrderEntity>> {

    @Resource
    private SyncSoReturnService syncSoReturnService;

    @Override
    public void onMessage(List<KingdeeReturnOrderEntity> list) {
        log.info("监听到金蝶退货单需要同步：entity={}", JSONUtil.toJsonStr(list));
        syncSoReturnService.syncKingdeeReturnOrderToSoReturn(list);
    }
}
