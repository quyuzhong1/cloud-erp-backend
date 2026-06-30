package com.erp.server.tms.rocketmq;


import cn.hutool.core.collection.CollUtil;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.tms.entity.LogisticsBillDetailEntity;
import com.erp.server.tms.service.LogisticsBillDetailService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 异步更新物流更新时间（轨迹拉取游标推进）
 * 原 Track123 专用，现快递100 小包/海运拉取也复用此消费者：按 billDetailId 将 update_time 刷为 now，
 * 使本批单据在 ORDER BY update_time 的待拉取队列中轮到队尾，避免重复拉取与积压。
 */
@Service
@RocketMQMessageListener(topic = RocketMqTopic.TMS_123_LOGISTICS_TRACK,
        selectorExpression = "async_get_logistics_track",
        consumerGroup = RocketMqConsumerGroup.ASYNC_GET_LOGISTICS_TRACK_CONSUMER)
public class PlatformUpdateTrackTimeConsumerService implements RocketMQListener<List<String>> {
    @Resource
    private LogisticsBillDetailService logisticsBillDetailService;

    @Override
    public void onMessage(List<String> billDetailIds) {
        if (CollUtil.isEmpty(billDetailIds)){
            return;
        }
        //根据明细id更新轨迹更新时间
        logisticsBillDetailService.lambdaUpdate().set(LogisticsBillDetailEntity::getUpdateTime, LocalDateTime.now()).in(LogisticsBillDetailEntity::getId,billDetailIds).update();
    }

}
