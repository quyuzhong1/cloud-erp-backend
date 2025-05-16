package com.erp.server.workflow.service.mq;

import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.dmp.entity.DmpPushMsgEntity;
import com.erp.model.workflow.dto.CfgApproveSyncDTO;
import com.erp.server.workflow.service.CfgApproveSyncService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 *
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = RocketMqTopic.WORKFLOW_SYNC_FS_INSTANCE_TOPIC,
        selectorExpression = "workflow_sync_fs_instance_tag",
        consumerGroup = RocketMqConsumerGroup.WORKFLOW_SYNC_FS_INSTANCE_CONSUMER)
public class MQSyncFsInstanceConsumerService implements RocketMQListener<CfgApproveSyncDTO.SyncFsProcessToMqDTO> {

    @Resource
    private CfgApproveSyncService configApproveSyncService;

    @Override
    public void onMessage(CfgApproveSyncDTO.SyncFsProcessToMqDTO dto) {
        log.info("MQSyncFsInstanceConsumerService onMessage: {}", dto);



    }
}
