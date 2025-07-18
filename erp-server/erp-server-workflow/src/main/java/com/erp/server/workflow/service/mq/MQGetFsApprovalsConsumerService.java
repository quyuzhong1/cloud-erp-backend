package com.erp.server.workflow.service.mq;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import com.erp.model.workflow.entity.ThirdProcessDefinitionEntity;
import com.erp.server.workflow.service.ThirdProcessDefinitionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 飞书获取单个审批状态
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_FS_APPROVALS_TO_WORKFLOW_TOPIC,
        selectorExpression = RocketMqNewTag.DMP_FS_APPROVALS_TO_WORKFLOW_TAG,
        consumerGroup = RocketMqNewConsumerGroup.DMP_FS_APPROVALS_TO_WORKFLOW_GROUP,
        consumeMode = ConsumeMode.ORDERLY)
public class MQGetFsApprovalsConsumerService extends AbstractNewPlatformConsumerHandler {
    @Resource
    private ThirdProcessDefinitionService thirdProcessDefinitionService;

    @Override
    public String getBizName() {
        return "飞书获取单个审批状态";
    }

    @Override
    public void handle(String data) {
        JSONObject jsonObject = JSONUtil.parseObj(data);
        String status = jsonObject.getStr("status");
        String form = jsonObject.getStr("form");
        String approval_code = jsonObject.getStr("ulanzi_approval_code");
        log.info("status: {}, form: {}", status, form);
        ThirdProcessDefinitionEntity thirdProcessDefinitionEntity = new ThirdProcessDefinitionEntity();
        thirdProcessDefinitionEntity.setStatus(status.toLowerCase());
        thirdProcessDefinitionEntity.setFormJson(form);
        thirdProcessDefinitionEntity.setApprovalCode(approval_code);
        //更新thirdProcessDefinitionEntity，以APPROVALCODE为条件
        thirdProcessDefinitionService.update(thirdProcessDefinitionEntity, new QueryWrapper<ThirdProcessDefinitionEntity>().eq("approval_code", approval_code));
    }
}
