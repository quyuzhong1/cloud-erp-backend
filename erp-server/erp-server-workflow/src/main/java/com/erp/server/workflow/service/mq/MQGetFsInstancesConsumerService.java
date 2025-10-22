package com.erp.server.workflow.service.mq;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.ApproveDTO;
import com.common.business.enums.ApproveTypeEnum;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import com.erp.model.sys.entity.SysUserThirdEntity;
import com.erp.model.workflow.dto.EndProcessDTO;
import com.erp.model.workflow.entity.*;
import com.erp.model.workflow.enums.*;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.workflow.context.CreateBillFactory;
import com.erp.server.workflow.handler.CreateBillHandler;
import com.erp.server.workflow.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.common.business.enums.ApproveTypeEnum.PASS;
import static com.common.business.enums.ApproveTypeEnum.REJECT;

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

    @Resource
    ProcessManagementService processManagementService;

    @Resource
    SysUserFeign sysUserFeign;


    @Override
    public String getBizName() {
        return "飞书获取单个审批实例详情";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
                handleAddInstance(jsonObject);
            } else {
                handleUpdateStatus(jsonObject,match.get().getSourcePlatform());
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

    private void handleUpdateStatus(JSONObject jsonObject, String sourcePlatform) {
        //更新或新增thirdProcessMan and taskMan
        thirdProcessManagementService.addOrUpdate(jsonObject,sourcePlatform);

        //回调
        FSApprovalStatusEnum statusEnum = FSApprovalStatusEnum.getByCode(jsonObject.getStr(FsRequestBodyAttributesEnum.STATUS.getCode()));
        if (statusEnum != FSApprovalStatusEnum.PENDING) {
            ApproveTaskInfoEntity one = approveTaskInfoService.getOne(
                    new LambdaQueryWrapper<ApproveTaskInfoEntity>()
                            .eq(ApproveTaskInfoEntity::getThirdInstanceId, jsonObject.getStr(FsRequestBodyAttributesEnum.INSTANCECODE.getCode()))
                            .orderByDesc(ApproveTaskInfoEntity::getCreateTime)
            );
            handleCallbackLogic(jsonObject, statusEnum, one);
        }
    }

    private void handleAddInstance(JSONObject jsonObject) {
        // 从 jsonObject 中获取 instanceCode
        CfgThirdProcessEntity thirdProcessEntity = cfgThirdProcessService.getOne(new LambdaQueryWrapper<CfgThirdProcessEntity>().eq(CfgThirdProcessEntity::getThirdProcessDefinitionCode, jsonObject.getStr(FsRequestBodyAttributesEnum.APPROVALCODE.getCode())).eq(CfgThirdProcessEntity::getIsDeleted, false));

        if (ObjectUtil.isEmpty(thirdProcessEntity)) {
            throw new ServiceException("未找到对应的三方审批生成配置");
        }
        List<CfgProcessFieldMapEntity> fieldMapList = cfgProcessFieldMapService.list(new LambdaQueryWrapper<CfgProcessFieldMapEntity>().eq(CfgProcessFieldMapEntity::getCfgId, thirdProcessEntity.getId()).eq(CfgProcessFieldMapEntity::getIsDeleted, false));

        List<String> fieldIdList = fieldMapList.stream().map(BaseEntity::getId).collect(Collectors.toList());

        List<CfgProcessValueMapEntity> valueMapList = cfgProcessValueMapService.list(new LambdaQueryWrapper<CfgProcessValueMapEntity>().in(CfgProcessValueMapEntity::getFieldMapId, fieldIdList).eq(CfgProcessValueMapEntity::getIsDeleted, false));

        // 新增数据
        //使用bussniessKey查询出三方审批生成配置，根据oprateType处理instance
        CreateBillHandler createBillHandler = createBillFactory.getCreateBillHandler(thirdProcessEntity.getBussinessKey());
        createBillHandler.createBill(jsonObject, thirdProcessEntity, fieldMapList, valueMapList);
    }

    /**
     * 根据流程状态处理最终的回调逻辑。
     */
    private void handleCallbackLogic(JSONObject jsonObject, FSApprovalStatusEnum statusEnum, ApproveTaskInfoEntity one) {
        JSONArray taskList = jsonObject.getJSONArray(FsRequestBodyAttributesEnum.TASKLIST.getCode());
        JSONObject lastTask = taskList.getJSONObject(taskList.size() - 1);
        String lastUserId = lastTask.getStr(FsRequestBodyAttributesEnum.USERID.getCode());
        Long endTime = lastTask.getLong(FsRequestBodyAttributesEnum.ENDTIME.getCode());
        LocalDateTime approveTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(endTime), ZoneId.systemDefault());

        switch (statusEnum) {
            case APPROVED:
                handleCallback(one, PASS.getStatus(), lastUserId, approveTime);
                break;
            case REJECTED:
                handleCallback(one, REJECT.getStatus(), lastUserId, approveTime);
                break;
            case CANCELED:
                ApproveDTO.CancelProcessDTO cancelProcessDTO = new ApproveDTO.CancelProcessDTO();
                cancelProcessDTO.setBusinessKey(one.getBussinessKey());
                cancelProcessDTO.setId(one.getBussinessId());
                processManagementService.cancelProcessFeign(cancelProcessDTO);
                break;
            case DELETED:
                ApproveDTO.DisApproveDTO disApproveDTO = new ApproveDTO.DisApproveDTO();
                disApproveDTO.setId(one.getBussinessId());
                disApproveDTO.setBusinessKey(one.getBussinessKey());
                processManagementService.disApproveFeign(disApproveDTO);
                break;
            default:
                break;
        }
    }

    /**
     * 原始的回调方法，保持不变。
     */
    public void handleCallback(ApproveTaskInfoEntity entity, String approveStatus, String userId, LocalDateTime approveTime) {
        EndProcessDTO processDTO = new EndProcessDTO();
        processDTO.setBusinessKey(entity.getBussinessKey());
        processDTO.setBusinessId(entity.getBussinessId());
        processDTO.setApproveStatus(ApproveTypeEnum.getByCode(approveStatus));
        // 来自第三方系统的用户ID可能需要转换为您系统内部的用户ID
        SysUserThirdEntity user = sysUserFeign.getUserByThird(ProcessSourcePlatformEnum.FS.getCode().toUpperCase(), userId);
        processDTO.setApproveUserId(user.getUserId());
        processDTO.setApproveTime(approveTime);
        processManagementService.callFeign(entity.getBussinessKey(), processDTO);
    }
}