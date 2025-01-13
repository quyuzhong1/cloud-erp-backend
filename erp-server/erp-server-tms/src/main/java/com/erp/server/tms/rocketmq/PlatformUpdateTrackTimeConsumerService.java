package com.erp.server.tms.rocketmq;


import cn.hutool.core.collection.CollUtil;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.dto.SoB2cLabelDTO;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.entity.LogisticsBillDetailEntity;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.tms.service.LogisticsBillDetailService;
import com.erp.server.tms.service.LogisticsBillService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 异步更新物流更新时间
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
        logisticsBillDetailService.lambdaUpdate().set(LogisticsBillDetailEntity::getTrackTime, LocalDateTime.now()).in(LogisticsBillDetailEntity::getId,billDetailIds).update();
    }

}
