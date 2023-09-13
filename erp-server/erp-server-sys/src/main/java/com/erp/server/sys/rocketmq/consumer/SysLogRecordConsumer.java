package com.erp.server.sys.rocketmq.consumer;

import cn.hutool.json.JSONUtil;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.sys.dto.SysLogMqDTO;
import com.erp.server.sys.service.SysLogRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_ERP_LOG_TO_SYS_TOPIC, selectorExpression = "sync_erp_log_to_sys_tag", consumerGroup = RocketMqConsumerGroup.SYNC_ERP_LOG_TO_SYS)
public class SysLogRecordConsumer implements RocketMQListener<SysLogMqDTO> {

    @Resource
    private SysLogRecordService sysLogRecordService;

    @Override
    public void onMessage(SysLogMqDTO mqDTO) {
        log.info("监听到操作日志需要同步：entity={}", JSONUtil.toJsonStr(mqDTO));
        sysLogRecordService.consumerAndAdd(mqDTO);
    }

}

