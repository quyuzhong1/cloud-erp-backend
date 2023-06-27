package com.erp.server.wms.rocketmq.customer;

import cn.hutool.json.JSONUtil;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.dmp.kingdee.KingdeeDeliveryDetailEntity;
import com.erp.server.wms.rocketmq.sync.SyncB2CSoOutstockService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * B2C 销售出库 金蝶同步到WMS
 *
 * @author Lambda
 * @Classname KingdeeB2CSoOutstockConsumer
 * @Description TODO
 * @Date 2023-06-27 10:21
 * @Created by yl
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_TO_WMS_TOPIC, selectorExpression = "sync_kingdee_so_outatock_tag", consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_SO_OUTSTOCK_TO_WMS)
public class KingdeeB2CSoOutstockConsumer implements RocketMQListener<KingdeeDeliveryDetailEntity> {

    @Resource
    private SyncB2CSoOutstockService syncB2CSoOutstockService;

    @Override
    public void onMessage(KingdeeDeliveryDetailEntity entity) {
        log.info("监听到金蝶销售出库单要同步：entity={}", JSONUtil.toJsonStr(entity));
        syncB2CSoOutstockService.syncKingdeeSoOutstock(entity);
    }
}
