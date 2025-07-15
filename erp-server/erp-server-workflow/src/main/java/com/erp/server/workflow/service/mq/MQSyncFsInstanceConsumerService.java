package com.erp.server.workflow.service.mq;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.common.utils.StringUtils;
import com.common.business.constant.ThirdConstants;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.ThirdpartyPlatformEnum;
import com.common.core.utils.BeanMapper;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.sys.vo.ThirdUnionDTO;
import com.erp.model.workflow.dto.CfgApproveSyncDTO;
import com.erp.model.workflow.dto.FsBotParamsDTO;
import com.erp.model.workflow.entity.*;
import com.erp.model.workflow.enums.*;
import com.erp.sdk.fs.enmu.FsActionStatusEnum;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.workflow.handler.CfgApproveSyncBuildHandler;
import com.erp.server.workflow.handler.CfgApproveSyncSendHandler;
import com.erp.server.workflow.handler.MQSyncFsHandler;
import com.erp.server.workflow.service.*;
import com.google.gson.Gson;
import com.lark.oapi.service.approval.v4.model.*;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private MQSyncFsHandler mqSyncFsHandler;

    @Override
    @Transactional
    public void onMessage(CfgApproveSyncDTO.SyncFsProcessToMqDTO dto) {
        log.info("MQSyncFsInstanceConsumerService 开始");
        mqSyncFsHandler.handler(dto);
        log.info("MQSyncFsInstanceConsumerService 结束");
    }

}
