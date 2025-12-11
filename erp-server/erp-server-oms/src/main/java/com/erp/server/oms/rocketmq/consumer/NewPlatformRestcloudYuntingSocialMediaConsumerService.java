package com.erp.server.oms.rocketmq.consumer;

import cn.hutool.json.JSONUtil;
import com.common.business.dto.YuntingSocialMediaDTO;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractRestCloudPlatformConsumerHandler;
import com.erp.server.oms.service.KolSocialMediaService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 云听社媒数据消费者
 * @author wuhaotian
 * @date 2025-12-10
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqNewTopic.RESTCLOUD_YUNTING_SOCIAL_MEDIA_TO_OMS_TOPIC,
        selectorExpression = RocketMqNewTag.RESTCLOUD_YUNTING_SOCIAL_MEDIA_TO_OMS_TAG,
        consumerGroup = RocketMqNewConsumerGroup.RESTCLOUD_YUNTING_SOCIAL_MEDIA_TO_OMS_GROUP,
        consumeMode = ConsumeMode.ORDERLY)
public class NewPlatformRestcloudYuntingSocialMediaConsumerService extends AbstractRestCloudPlatformConsumerHandler {

    @Resource
    private KolSocialMediaService kolSocialMediaService;

    @Override
    public String getBizName() {
        return "云听社媒数据";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handle(String data) {
        log.info("云听社媒数据消费开始，data={}", data);
        
        YuntingSocialMediaDTO dto = JSONUtil.toBean(data, YuntingSocialMediaDTO.class);
        if (Objects.isNull(dto)) {
            log.error("云听社媒数据消费失败，参数为空");
            return;
        }
        
        if (cn.hutool.core.util.StrUtil.isBlank(dto.getUnique())) {
            log.error("云听社媒数据消费失败，unique为空");
            return;
        }
        
        kolSocialMediaService.handleYuntingConsumer(dto);
        
        log.info("云听社媒数据消费成功，unique={}", dto.getUnique());
    }
}

