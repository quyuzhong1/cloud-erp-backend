package com.erp.server.workflow.service.mq;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.workflow.entity.*;
import com.erp.model.workflow.enums.ThirdProcessDefinitionStatusEnum;
import com.erp.model.workflow.enums.ThirdProcessDefinitionTypeEnum;
import com.erp.server.workflow.context.CreateBillFactory;
import com.erp.server.workflow.handler.CreateBillHandler;
import com.erp.server.workflow.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = RocketMqTopic.DMP_PLATFORM_APPROVALS_TO_WORKFLOW_TOPIC,
        selectorExpression = "${spring.cloud.nacos.discovery.namespace}-dmp_platform_instances_to_workflow_tag",
        consumerGroup = RocketMqConsumerGroup.WORKFLOW_FS_INSTANCES_CONSUMER)
public class MQGetFsInstancesConsumerService implements RocketMQListener<JSONObject> {

    @Resource
    private ThirdProcessDefinitionService thirdProcessDefinitionService;

    @Resource
    private ApproveTaskInfoService approveTaskInfoService;

    @Resource
    private ThirdProcessInstanceService thirdProcessInstanceService;

    @Resource
    private CreateBillFactory createBillFactory;

    @Resource
    private CfgThirdProcessService cfgThirdProcessService;

    @Resource
    CfgProcessFieldMapService cfgProcessFieldMapService;

    @Resource
    CfgProcessValueMapService  cfgProcessValueMapService;

    @Resource
    ThirdProcessManagementService thirdProcessManagementService;



    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(JSONObject jsonObject) {
        String approvalCode = jsonObject.getStr("approvalCode");

        // 1. 获取启用的流程定义
        List<ThirdProcessDefinitionEntity> activeDefs = thirdProcessDefinitionService.list(
                new LambdaQueryWrapper<ThirdProcessDefinitionEntity>()
                        .eq(ThirdProcessDefinitionEntity::getStatus, ThirdProcessDefinitionStatusEnum.ACTIVE.getCode())
                        .eq(ThirdProcessDefinitionEntity::getIsDeleted, false)
        );

        // 2. 保存或更新实例
        ThirdProcessInstanceEntity instanceEntity = buildInstanceEntity(jsonObject);
        thirdProcessInstanceService.saveOrUpdate(instanceEntity,
                new QueryWrapper<ThirdProcessInstanceEntity>().eq("instance_code", instanceEntity.getInstanceCode())
        );

        // 3. 查找当前 approvalCode 对应的流程定义类型
        Optional<ThirdProcessDefinitionEntity> match = activeDefs.stream()
                .filter(def -> approvalCode.equals(def.getApprovalCode()))
                .findFirst();

        if (match.isPresent()) {
            String type = match.get().getType();
            if (ThirdProcessDefinitionTypeEnum.PULL.getCode().equals(type)) {
                handleAddInstance(jsonObject);
            } else {
                handleUpdateStatus(jsonObject);
            }
        } else {
            // 未找到对应定义，是否记录日志或抛出异常？
            log.warn("未找到匹配的流程定义: approvalCode={}", approvalCode);
        }
    }

    private ThirdProcessInstanceEntity buildInstanceEntity(JSONObject jsonObject) {
        ThirdProcessInstanceEntity entity = new ThirdProcessInstanceEntity();
        entity.setApprovalCode(jsonObject.getStr("approvalCode"));
        entity.setStartTime(jsonObject.getStr("startTime"));
        entity.setEndTime(jsonObject.getStr("endTime"));
        entity.setSerialNumber(jsonObject.getStr("serialNumber"));
        entity.setStatus(jsonObject.getStr("status"));
        entity.setForm(jsonObject.getStr("form"));
        entity.setInstanceCode(jsonObject.getStr("instanceCode"));
        return entity;
    }

    private void handleUpdateStatus(JSONObject jsonObject) {
        //TODO 更新thirdTask
        thirdProcessManagementService.insert(jsonObject);
    }

    private void handleAddInstance(JSONObject jsonObject) {
        // 从 jsonObject 中获取 instanceCode
        CfgThirdProcessEntity thirdProcessEntity = cfgThirdProcessService.getOne(new LambdaQueryWrapper<CfgThirdProcessEntity>().eq(CfgThirdProcessEntity::getBussinessKey, jsonObject.getStr("bussinessKey")).eq(CfgThirdProcessEntity::getIsDeleted, false));

        List<CfgProcessFieldMapEntity> fieldMapList = cfgProcessFieldMapService.list(new LambdaQueryWrapper<CfgProcessFieldMapEntity>().eq(CfgProcessFieldMapEntity::getCfgId, thirdProcessEntity.getId()).eq(CfgProcessFieldMapEntity::getIsDeleted, false));

        List<String> fieldIdList = fieldMapList.stream().map(e -> e.getId()).collect(Collectors.toList());

        List<CfgProcessValueMapEntity> valueMapList = cfgProcessValueMapService.list(new LambdaQueryWrapper<CfgProcessValueMapEntity>().in(CfgProcessValueMapEntity::getFieldMapId, fieldIdList).eq(CfgProcessValueMapEntity::getIsDeleted, false));

        String instanceCode = jsonObject.getStr("instanceCode");
        // 根据 instanceCode 查询对应的记录
        ApproveTaskInfoEntity taskInfo = approveTaskInfoService.getOne(
                new LambdaQueryWrapper<ApproveTaskInfoEntity>()
                        .eq(ApproveTaskInfoEntity::getThirdInstanceId, instanceCode)
        );
        // 如果记录不存在，记录日志并返回
        if (taskInfo == null) {
            log.warn("未找到对应的三方生成查询记录: instanceCode={}", instanceCode);
            return;
        }
        // 完成新增数据事务提交之后,异步执行
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                add(jsonObject,  thirdProcessEntity, fieldMapList, valueMapList);
                log.info("飞书审批实例创建单据[{}]消息已投递,事务已提交");
            }
        });
    }

    //处理推送类型的消息
    public void add(JSONObject jsonObject, CfgThirdProcessEntity thirdProcessEntity,  List<CfgProcessFieldMapEntity> fieldMapList, List<CfgProcessValueMapEntity> valueMapList) {
        try {
            //使用bussniessKey查询出三方审批生成配置，根据oprateType处理instance
            CreateBillHandler createBillHandler = createBillFactory.getCreateBillHandler(thirdProcessEntity.getBussinessKey());
            createBillHandler.createBill(jsonObject, thirdProcessEntity, fieldMapList, valueMapList);
        } catch (Exception e) {
            throw new ServiceException("创建单据异常");
        }
    }
}