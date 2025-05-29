package com.erp.server.workflow.service.mq;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.workflow.entity.*;
import com.erp.server.workflow.service.*;
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
@RocketMQMessageListener(topic = RocketMqTopic.DMP_PLATFORM_APPROVALS_TO_WORKFLOW_TOPIC,
        selectorExpression = "dmp_platform_approvals_to_workflow_tag",
        consumerGroup = RocketMqConsumerGroup.WORKFLOW_FS_APPROVALS_CONSUMER)
public class MQGetFsApprovalsConsumerService implements RocketMQListener<JSONObject> {
    @Resource
    private ThirdProcessDefinitionService thirdProcessDefinitionService;

    @Override
    public void onMessage(JSONObject jsonObject) {
        String status = jsonObject.getStr("status");
        String form = jsonObject.getStr("form");
        String approval_code = jsonObject.getStr("ulanzi_approval_code");
        log.info("status: {}, form: {}", status, form);
        ThirdProcessDefinitionEntity thirdProcessDefinitionEntity = new ThirdProcessDefinitionEntity();
        thirdProcessDefinitionEntity.setStatus(status);
        thirdProcessDefinitionEntity.setFormJson(form);
        thirdProcessDefinitionEntity.setApprovalCode(approval_code);
        //更新thirdProcessDefinitionEntity，以APPROVALCODE为条件
        thirdProcessDefinitionService.update(thirdProcessDefinitionEntity, new QueryWrapper<ThirdProcessDefinitionEntity>().eq("approval_code", approval_code));
    }
}
