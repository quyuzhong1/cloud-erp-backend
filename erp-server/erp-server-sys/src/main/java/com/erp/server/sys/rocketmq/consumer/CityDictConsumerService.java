package com.erp.server.sys.rocketmq.consumer;

import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 下载第三方城市字典消费类
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.PLATFORM_PULL_DATA_TOPIC,
        selectorExpression = "third_system_city_dict_tag",
        consumerGroup = "${spring.cloud.nacos.discovery.namespace}-platform_pull_products_consumer",
        consumeMode = ConsumeMode.ORDERLY)
public class CityDictConsumerService<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Override
    public void updateSyncTaskStatus(String syncTaskId, SyncStatusEnum code, String msg) {
        dmpTaskFeign.updateSyncInfo(new DmpSyncMqDTO.ParamDTO(syncTaskId, code.getCode(), msg));
    }

    @Override
    public void sendWarnMsg(String syncTaskId) {

    }

    @Override
    public ApiResult<?> handle(Object ext) {
        return null;
    }
}
