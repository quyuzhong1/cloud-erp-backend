package com.erp.server.workflow.service.mq;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import com.erp.model.workflow.entity.ThirdProcessDefinitionEntity;
import com.erp.model.workflow.entity.ThirdProcessInstanceEntity;
import com.erp.model.workflow.enums.FsRequestBodyAttributesEnum;
import com.erp.model.workflow.enums.ThirdProcessDefinitionStatusEnum;
import com.erp.model.workflow.enums.ThirdProcessDefinitionTypeEnum;
import com.erp.server.workflow.service.FsInstancesService;
import com.erp.server.workflow.service.ThirdProcessDefinitionService;
import com.erp.server.workflow.service.ThirdProcessInstanceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

/**
 * 飞书获取单个审批实例详情
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_FS_INSTANCES_TO_WORKFLOW_TOPIC,
        selectorExpression = RocketMqNewTag.DMP_FS_INSTANCES_TO_WORKFLOW_TAG,
        consumerGroup = RocketMqNewConsumerGroup.DMP_FS_INSTANCES_TO_WORKFLOW_GROUP)
public class MQGetFsInstancesConsumerService  extends AbstractNewPlatformConsumerHandler {

    @Resource
    private ThirdProcessDefinitionService thirdProcessDefinitionService;

    @Resource
    private ThirdProcessInstanceService thirdProcessInstanceService;

    @Resource
    private FsInstancesService fsInstancesService;


    @Override
    public String getBizName() {
        return "飞书获取单个审批实例详情";
    }

    @Override
    public void handle(String data) {

        JSONObject jsonObject = JSONUtil.parseObj(data);
        String approvalCode = jsonObject.getStr(FsRequestBodyAttributesEnum.APPROVALCODE.getCode());

        // 1. 获取启用的流程定义
        List<ThirdProcessDefinitionEntity> activeDefs = thirdProcessDefinitionService.list(
                new LambdaQueryWrapper<ThirdProcessDefinitionEntity>()
                        .eq(ThirdProcessDefinitionEntity::getStatus, ThirdProcessDefinitionStatusEnum.ACTIVE.getCode())
                        .eq(ThirdProcessDefinitionEntity::getIsDeleted, false)
        );

        // 2. 保存或更新实例
        ThirdProcessInstanceEntity instanceEntity = buildInstanceEntity(jsonObject);
        thirdProcessInstanceService.saveOrUpdate(instanceEntity,
                new LambdaQueryWrapper<ThirdProcessInstanceEntity>().eq(ThirdProcessInstanceEntity::getInstanceCode, instanceEntity.getInstanceCode())
        );

        // 3. 查找当前 approvalCode 对应的流程定义类型
        Optional<ThirdProcessDefinitionEntity> match = activeDefs.stream()
                .filter(def -> approvalCode.equals(def.getApprovalCode()))
                .findFirst();

        if (match.isPresent()) {
            String type = match.get().getType();
            if (ThirdProcessDefinitionTypeEnum.PULL.getCode().equals(type)) {
                fsInstancesService.handleAddInstance(jsonObject);
            } else {
                fsInstancesService.handleUpdateStatus(jsonObject,match.get().getSourcePlatform());
            }
        } else {
            // 未找到对应定义，是否记录日志或抛出异常？
            log.warn("未找到匹配的流程定义: approvalCode={}", approvalCode);
        }
    }

    private ThirdProcessInstanceEntity buildInstanceEntity(JSONObject jsonObject) {
        ThirdProcessInstanceEntity entity = new ThirdProcessInstanceEntity();
        entity.setApprovalCode(jsonObject.getStr(FsRequestBodyAttributesEnum.APPROVALNAME.getCode()));
        entity.setStartTime(jsonObject.getStr(FsRequestBodyAttributesEnum.STARTTIME.getCode()));
        entity.setEndTime(jsonObject.getStr(FsRequestBodyAttributesEnum.ENDTIME.getCode()));
        entity.setSerialNumber(jsonObject.getStr(FsRequestBodyAttributesEnum.SERIALNUMBER.getCode()));
        entity.setStatus(jsonObject.getStr(FsRequestBodyAttributesEnum.STATUS.getCode()));
        entity.setForm(jsonObject.getStr(FsRequestBodyAttributesEnum.FORM.getCode()));
        entity.setInstanceCode(jsonObject.getStr(FsRequestBodyAttributesEnum.INSTANCECODE.getCode()));
        entity.setApprovalName(jsonObject.getStr(FsRequestBodyAttributesEnum.APPROVALNAME.getCode()));
        entity.setTaskList(jsonObject.getStr(FsRequestBodyAttributesEnum.TASKLIST.getCode()));
        entity.setThirdJson(jsonObject);
        return entity;
    }
}