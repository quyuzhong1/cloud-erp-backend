package com.erp.server.oms.rocketmq.customer;

import cn.hutool.json.JSONUtil;
import com.common.business.utils.RedisUtil;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.kingdee.KingdeeReturnOrderEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductSaleEntity;
import com.erp.server.oms.service.SoReturnService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_TO_OMS_SALES_TOPIC, selectorExpression = "sync_kingdee_return_order_tag", consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_RETURN_ORDER_TO_OMS)
public class ProductListingTimeCustomer implements RocketMQListener<List<KingdeeReturnOrderEntity>> {

    @Resource
    private SoReturnService soReturnService;
    @Resource
    private RedisUtil redisUtil;

    @Override
    public void onMessage(List<KingdeeReturnOrderEntity> list) {
        log.info("监听到金蝶退货单需要同步：entity={}", JSONUtil.toJsonStr(list));
    }
}
