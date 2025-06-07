package com.erp.server.workflow.service.mq;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.common.business.vo.LoginUser;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.workflow.dto.ApproveTaskInfoDTO;
import com.erp.model.workflow.entity.*;
import com.erp.model.workflow.enums.ApproveTaskStatusEnum;
import com.erp.model.workflow.enums.CfgQueryOptionBussinessKeyEnum;
import com.erp.model.workflow.enums.ThirdProcessDefinitionStatusEnum;
import com.erp.model.workflow.enums.ThirdProcessDefinitionTypeEnum;
import com.erp.server.workflow.context.ProcessFormFactory;
import com.erp.server.workflow.context.UpdateBillStatusFactory;
import com.erp.server.workflow.handler.ProcessFormHandler;
import com.erp.server.workflow.handler.UpdateBillStatusHandler;
import com.erp.server.workflow.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
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
    private UpdateBillStatusFactory updateBillStatusFactory;

    @Resource
    private CfgThirdProcessService cfgThirdProcessService;

    @Resource
    CfgProcessFieldMapService cfgProcessFieldMapService;

    @Resource
    CfgProcessValueMapService  cfgProcessValueMapService;

    @Resource
    ProcessFormFactory processFormFactory;

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
        CfgThirdProcessEntity thirdProcessEntity = cfgThirdProcessService.getOne(new LambdaQueryWrapper<CfgThirdProcessEntity>().eq(CfgThirdProcessEntity::getBussinessKey, jsonObject.getStr("bussinessKey")).eq(CfgThirdProcessEntity::getIsDeleted, false));

        List<CfgProcessFieldMapEntity> fieldMapList = cfgProcessFieldMapService.list(new LambdaQueryWrapper<CfgProcessFieldMapEntity>().eq(CfgProcessFieldMapEntity::getCfgId, thirdProcessEntity.getId()).eq(CfgProcessFieldMapEntity::getIsDeleted, false));

        List<String> fieldIdList = fieldMapList.stream().map(e -> e.getId()).collect(Collectors.toList());

        List<CfgProcessValueMapEntity> valueMapList = cfgProcessValueMapService.list(new LambdaQueryWrapper<CfgProcessValueMapEntity>().in(CfgProcessValueMapEntity::getFieldMapId, fieldIdList).eq(CfgProcessValueMapEntity::getIsDeleted, false));

        String instanceCode = jsonObject.getStr("instanceCode");

        // TODO创建三方生成查询记录(处理记录)
        ApproveTaskInfoEntity taskInfo = approveTaskInfoService.getOne(
                new LambdaQueryWrapper<ApproveTaskInfoEntity>()
                        .eq(ApproveTaskInfoEntity::getThirdInstanceId, instanceCode)
        );
        if (ObjectUtil.isEmpty(taskInfo)) {
            // 如果记录不存在，创建新的任务信息
            ProcessFormHandler handler = processFormFactory.getConstructBillHandler(thirdProcessEntity.getSourcePlatform());
            Map<String, Object> map = handler.constructBill(jsonObject.getJSONArray("form"), fieldMapList, valueMapList);

            UpdateBillStatusHandler billStatusHandler = updateBillStatusFactory.getUpdateBillStatusHandler(thirdProcessEntity.getBussinessKey());
            billStatusHandler.operateType(jsonObject, thirdProcessEntity, fieldMapList, valueMapList);
        }
        // 完成新增数据事务提交之后,异步执行
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                update(jsonObject, thirdProcessEntity, fieldMapList, valueMapList);
                log.info("飞书审批实例更新单据状态[{}]消息已投递,事务已提交");
            }
        });
    }

    //  处理拉取类型的消息
    public void update(JSONObject jsonObject, CfgThirdProcessEntity thirdProcessEntity,  List<CfgProcessFieldMapEntity> fieldMapList, List<CfgProcessValueMapEntity> valueMapList) {
        //使用bussniessKey查询出三方审批生成配置，根据oprateType处理instance
        UpdateBillStatusHandler updateBillStatusHandler = updateBillStatusFactory.getUpdateBillStatusHandler(thirdProcessEntity.getBussinessKey());
        updateBillStatusHandler.operateType(jsonObject, thirdProcessEntity, fieldMapList, valueMapList);
    }

    private void handleAddInstance(JSONObject jsonObject) {
        // 从 jsonObject 中获取 instanceCode
        String instanceCode = jsonObject.getStr("instanceCode");
        // 根据 instanceCode 查询对应的记录
        ApproveTaskInfoEntity taskInfo = approveTaskInfoService.getOne(
                new LambdaQueryWrapper<ApproveTaskInfoEntity>()
                        .eq(ApproveTaskInfoEntity::getThirdInstanceId, instanceCode)
        );
        // 如果记录不存在，记录日志并返回
        if (taskInfo == null) {
            log.warn("未找到对应的审批任务记录: instanceCode={}", instanceCode);
            return;
        }
        // 完成新增数据事务提交之后,异步执行
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                add(jsonObject, taskInfo);
                log.info("飞书审批实例更新单据状态[{}]消息已投递,事务已提交");
            }
        });
    }

    //处理推送类型的消息
    public void add(JSONObject jsonObject, ApproveTaskInfoEntity taskInfo) {
        String status = jsonObject.getStr("status");
        try {
            //更新状态

        } catch (Exception e) {
            throw new ServiceException("单据更新状态异常");
        }
        log.info("飞书审批实例更新单据状态[{}]成功", taskInfo.getThirdInstanceId());
    }
}

//1、单据启动时的数据处理startDTO，表头和明细转map

//2、cfg_query_option中选项类型的数据处理，数据隔离 TODO

//3、飞书推送的审批实例的状态更新

//4、三方审批生成的处理审批实例的业务代码

//5、三方生成查询记录的生成，以及处理逻辑 TODO

//6、所有点串联