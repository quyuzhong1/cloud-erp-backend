package com.erp.server.workflow.service.mq;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.ApproveDTO;
import com.common.business.enums.ApprovePlatformEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.factory.ApproveEndHandlerFactory;
import com.common.business.handler.AbstractApproveHandler;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
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
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.common.business.enums.ApproveTypeEnum.PASS;
import static com.common.business.enums.ApproveTypeEnum.REJECT;

/**
 * 飞书获取单个审批实例详情
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_FS_APPROVALS_TO_WORKFLOW_TOPIC,
        selectorExpression = RocketMqNewTag.DMP_FS_INSTANCES_TO_WORKFLOW_TAG,
        consumerGroup = RocketMqNewConsumerGroup.DMP_FS_INSTANCES_TO_WORKFLOW_GROUP,
        consumeMode = ConsumeMode.ORDERLY)
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

    @Resource
    private ApproveEndHandlerFactory approveEndHandlerFactory;

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

        List<CfgProcessFieldMapEntity> fieldMapList = cfgProcessFieldMapService.list(new LambdaQueryWrapper<CfgProcessFieldMapEntity>().eq(CfgProcessFieldMapEntity::getCfgId, thirdProcessEntity.getId()).eq(CfgProcessFieldMapEntity::getIsDeleted, false));

        List<String> fieldIdList = fieldMapList.stream().map(BaseEntity::getId).collect(Collectors.toList());

        List<CfgProcessValueMapEntity> valueMapList = cfgProcessValueMapService.list(new LambdaQueryWrapper<CfgProcessValueMapEntity>().in(CfgProcessValueMapEntity::getFieldMapId, fieldIdList).eq(CfgProcessValueMapEntity::getIsDeleted, false));

        // 新增数据
        add(jsonObject,  thirdProcessEntity, fieldMapList, valueMapList);
    }

    //处理推送类型的消息
    public void add(JSONObject jsonObject, CfgThirdProcessEntity thirdProcessEntity,  List<CfgProcessFieldMapEntity> fieldMapList, List<CfgProcessValueMapEntity> valueMapList) {
        try {
            //使用bussniessKey查询出三方审批生成配置，根据oprateType处理instance
            CreateBillHandler createBillHandler = createBillFactory.getCreateBillHandler(thirdProcessEntity.getBussinessKey());
            createBillHandler.createBill(jsonObject, thirdProcessEntity, fieldMapList, valueMapList);
        } catch (Exception e) {
            throw new ServiceException("创建单据异常，msg= {}", e.getMessage());
        }
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

        //审批意见
        JSONArray timeline = jsonObject.getJSONArray(FsRequestBodyAttributesEnum.TIMELINE.getCode());
        String comment = "";
        if (timeline != null && !timeline.isEmpty()) {
             comment = timeline.stream().filter(item -> !Objects.equals(((JSONObject) item).getStr(FsRequestBodyAttributesEnum.COMMENT.getCode()), ""))
                    .map(item -> ((JSONObject) item).getStr(FsRequestBodyAttributesEnum.COMMENT.getCode()))
                    .collect(Collectors.joining("; "));
        }
        switch (statusEnum) {
            case APPROVED:
                handleCallback(one, PASS.getStatus(), lastUserId, approveTime,comment);
                break;
            case REJECTED:
                handleCallback(one, REJECT.getStatus(), lastUserId, approveTime,comment);
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
        //评论
        JSONArray commentList = jsonObject.getJSONArray(FsRequestBodyAttributesEnum.COMMENTLIST.getCode());
        List<String> comments = null;
        if (commentList != null && !commentList.isEmpty()) {
            comments = commentList.stream()
                    .map(item -> ((JSONObject) item).getStr(FsRequestBodyAttributesEnum.COMMENT.getCode()))
                    .collect(Collectors.toList());
        }
        handleAddComments(comments,one);
    }

    /**
     * 添加评论日志
     * @author will
     * @date 2025/8/5 10:10
     * @param comments
     * @param entity
     * @return void
     */
    public void handleAddComments( List<String> comments, ApproveTaskInfoEntity entity) {
        if (CollUtil.isEmpty(comments)) {
            return;
        }
        String businessKey = entity.getBussinessKey();
        SourceTypeEnum sourceType = SourceTypeEnum.getByCode(businessKey);
        if (null == sourceType) {
            throw new ServiceException(ApiError.ERROR_NOT_FOUND_APPROVE_BUSINESSKEY,"添加评论",businessKey);
        }
        AbstractApproveHandler handler = approveEndHandlerFactory.getHandler(sourceType);
        ApproveDTO.AddCommentDTO addCommentDTO = new ApproveDTO.AddCommentDTO();
        addCommentDTO.setBusinessKey(businessKey);
        addCommentDTO.setId(entity.getBussinessId());
        addCommentDTO.setComments(comments);
        addCommentDTO.setApprovePlatformEnum(ApprovePlatformEnum.FEI_SHU);
        handler.addComment(addCommentDTO);
    }

    /**
     * 原始的回调方法，保持不变。
     */
    public void handleCallback(ApproveTaskInfoEntity entity, String approveStatus, String userId, LocalDateTime approveTime,String comment) {
        EndProcessDTO processDTO = new EndProcessDTO();
        processDTO.setBusinessKey(entity.getBussinessKey());
        processDTO.setBusinessId(entity.getBussinessId());
        processDTO.setApproveStatus(ApproveTypeEnum.getByCode(approveStatus));
        // 来自第三方系统的用户ID可能需要转换为您系统内部的用户ID
        SysUserThirdEntity user = sysUserFeign.getUserByThird(ProcessSourcePlatformEnum.FS.getCode().toUpperCase(), userId);
        processDTO.setApproveUserId(user.getUserId());
        processDTO.setApproveTime(approveTime);
        processDTO.setComment(comment);
        processDTO.setApprovePlatformEnum(ApprovePlatformEnum.FEI_SHU);
        processManagementService.callFeign(entity.getBussinessKey(), processDTO);
    }
}