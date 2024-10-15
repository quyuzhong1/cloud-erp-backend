package com.erp.server.tms.rocketmq;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.core.controller.vo.ApiResult;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import com.erp.model.tms.entity.LogisticsTrackEntity;
import com.erp.server.tms.convert.TrackDataConverter;
import com.erp.server.tms.service.LogisticsBillDetailService;
import com.erp.server.tms.service.LogisticsTrackService;
import com.sdk.tms.track123.dto.PlatformTrackDTO;
import com.sdk.tms.track123.model.response.TrackDetail;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_TRACK123_TO_TMS_TOPIC,
        selectorExpression = RocketMqNewTag.DMP_TRACK123_TO_TMS_TAG,
        consumerGroup = RocketMqNewConsumerGroup.DMP_TRACK123_TO_TMS_GROUP,
        consumeMode = ConsumeMode.ORDERLY)
public class NewDmpPlatformTrackConsumerService extends AbstractNewPlatformConsumerHandler {
    @Resource
    private PlatformTrackConsumerService platformTrackConsumerService;

    @Override
    public String getBizName() {
        return "物流轨迹信息";
    }

    @Override
    public void handle(String data) {
        platformTrackConsumerService.handle(data);
    }
}
