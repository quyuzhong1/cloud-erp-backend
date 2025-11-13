package com.erp.server.workflow.handler;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.common.utils.StringUtils;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.core.enums.ApiError;
import com.common.core.utils.BeanMapper;
import com.erp.model.sys.vo.ThirdUnionDTO;
import com.erp.model.workflow.dto.CfgApproveSyncDTO;
import com.erp.model.workflow.dto.FsBotParamsDTO;
import com.erp.model.workflow.dto.ProcessTaskManagementExtDTO;
import com.erp.model.workflow.entity.*;
import com.erp.model.workflow.enums.*;
import com.erp.sdk.fs.enmu.FsActionStatusEnum;
import com.erp.sdk.fs.enmu.UserIdTypeEnum;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.workflow.service.ApproveSyncRecordService;
import com.erp.server.workflow.service.CfgApproveNoticeService;
import com.erp.server.workflow.service.CfgSettingService;
import com.erp.server.workflow.service.ProcessTaskManagementExtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author jack
 * @date 2025-05-21
 */
@Slf4j
@Component
public class CfgApproveSyncSendHandler {

    @Resource
    private ProcessTaskManagementExtService processTaskManagementExtService;
    @Resource
    private CfgApproveNoticeService cfgApproveNoticeService;
    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    private CfgApproveSyncBuildHandler cfgApproveSyncBuildHandler;
    @Resource
    private FsService fsService;
    @Resource
    private ApproveSyncRecordService approveSyncRecordService;

    public void updateNotice(CfgApproveSyncDTO.SyncFsProcessToMqDTO dto,
                                List<ProcessTaskManagementEntity> processTaskManagementEntities,
                                ApproveSyncRecordEntity syncRecordEntity) {
        //获取操作的taskId
        String curTaskId = dto.getCurTaskId();
        if(StringUtils.isBlank(curTaskId)){

        }else {
            //过滤出当前任务ID的审批记录 并且任务状态为通过或者拒绝
            List<ProcessTaskManagementEntity> approveTaskList = processTaskManagementEntities.stream()
                    .filter(item -> item.getTaskId().equals(curTaskId))
                    .collect(Collectors.toList());

            //审批状态
            String approveType = dto.getApproveType();
            if (Objects.equals(approveType, ApproveTypeEnum.PASS.getStatus())){//审核通过
                //更新审批结果通知
                commonUpdateNotice(approveTaskList,FsActionStatusEnum.APPROVED.getCode(),syncRecordEntity);
            } else if (Objects.equals(approveType, ApproveTypeEnum.REJECT.getStatus())) {//审核不通过
                //更新审批结果通知
                commonUpdateNotice(approveTaskList,FsActionStatusEnum.REJECTED.getCode(),syncRecordEntity);
            } else if (Objects.equals(approveType, ApproveTypeEnum.CANCEL.getStatus())) {//撤销
                //更新审批结果通知
                commonUpdateNotice(processTaskManagementEntities,FsActionStatusEnum.CANCELLED.getCode(),syncRecordEntity);
            } else if (Objects.equals(approveType, FsActionStatusEnum.FORWARDED.getCode())) {//转办
                //更新审批结果通知
                commonUpdateNotice(approveTaskList,FsActionStatusEnum.FORWARDED.getCode(),syncRecordEntity);
            } else if(Objects.equals(approveType, FsActionStatusEnum.PROCESSED.getCode())){//强制通过
                //更新审批结果通知
                commonUpdateNotice(approveTaskList,FsActionStatusEnum.PROCESSED.getCode(),syncRecordEntity);
            } else if(Objects.equals(approveType, FsActionStatusEnum.ROLLBACK.getCode())){//强制驳回
                //更新审批结果通知
                commonUpdateNotice(approveTaskList,FsActionStatusEnum.ROLLBACK.getCode(),syncRecordEntity);
            } else if(Objects.equals(approveType, FsActionStatusEnum.SUSPEND.getCode())){ //暂停
                //更新审批结果通知
                commonUpdateNotice(approveTaskList,FsActionStatusEnum.SUSPEND.getCode(),syncRecordEntity);
            }
        }



    }

    public void sendNotice(CfgApproveSyncDTO.SyncFsProcessToMqDTO dto,
                           List<CfgApproveSyncFieldMapEntity> fieldMapEntities,
                           Map<String, String> remoteValues,
                           ProcessManagementEntity processManagementEntity,
                           CfgApproveSyncEntity cfgApproveSyncEntity,
                           String createUserId,
                           List<String> approveIds,
                           List<String> ccIds,
                           Map<String, ThirdUnionDTO> thirdUnionMap,
                           List<ProcessTaskManagementEntity> processTaskManagementEntities,
                           ApproveSyncRecordEntity syncRecordEntity) {

        //pc地址
        String pcLinkByEnv = cfgSettingService.getPcLinkByEnv();
        List<String> summaries = cfgApproveSyncBuildHandler.getSummaries(fieldMapEntities, remoteValues);

        String processInstanceId = processTaskManagementEntities.get(0).getProcessInstanceId();
        //过滤出当前任务ID的审批记录 并且任务状态为进行中的 ,并且未推送过审批消息的
        processTaskManagementEntities = processTaskManagementExtService.listProcessTaskByTaskIds(processTaskManagementEntities.stream().map(ProcessTaskManagementEntity::getId).collect(Collectors.toList()),processInstanceId);

        //审批状态
        String approveType = dto.getApproveType();
        if(StringUtils.isBlank(approveType) || (Objects.equals(approveType, ApproveTypeEnum.PASS.getStatus()) && !Objects.equals(processManagementEntity.getProcessStatus(), ProcessStatusEnum.FINISH))){//创建流程
            if(CollUtil.isNotEmpty(processTaskManagementEntities)){
                //默认发送审核人
                sendApproveNotice(NoticeTemplateEnum.APPROVE,summaries, processTaskManagementEntities, thirdUnionMap, cfgApproveSyncEntity, pcLinkByEnv,syncRecordEntity);

                //发送抄送通知
                commonSendNotice(NoticeTemplateEnum.CC, cfgApproveSyncEntity, createUserId, approveIds, ccIds, thirdUnionMap, summaries, pcLinkByEnv,syncRecordEntity);
            }
        }

        if (Objects.equals(approveType, ApproveTypeEnum.PASS.getStatus())//审核通过并且流程已经完成
                && Objects.equals(processManagementEntity.getProcessStatus(), ProcessStatusEnum.FINISH)) {
            //发送审批结果通知
            commonSendNotice(NoticeTemplateEnum.APPROVE_RESULT_PASS,cfgApproveSyncEntity, createUserId, approveIds, ccIds, thirdUnionMap, summaries, pcLinkByEnv,syncRecordEntity);
        } else if (Objects.equals(approveType, ApproveTypeEnum.REJECT.getStatus())
                && Objects.equals(processManagementEntity.getProcessStatus(), ProcessStatusEnum.FINISH)) {//审核不通过并且流程已经完成
            //发送审批结果通知
            commonSendNotice(NoticeTemplateEnum.APPROVE_RESULT_REJECT,cfgApproveSyncEntity, createUserId, approveIds, ccIds, thirdUnionMap, summaries, pcLinkByEnv,syncRecordEntity);
        } else if (Objects.equals(approveType, ApproveTypeEnum.CANCEL.getStatus())) {//撤销
            //发送撤销通知
            commonSendNotice(NoticeTemplateEnum.RECALL,cfgApproveSyncEntity, createUserId, approveIds, ccIds, thirdUnionMap, summaries, pcLinkByEnv,syncRecordEntity);
        } else if (Objects.equals(approveType, FsActionStatusEnum.FORWARDED.getCode())) {//转办
            //默认发送审核人
            sendApproveNotice(NoticeTemplateEnum.APPROVE,summaries, processTaskManagementEntities, thirdUnionMap, cfgApproveSyncEntity, pcLinkByEnv,syncRecordEntity);
        } else if(Objects.equals(approveType, FsActionStatusEnum.PROCESSED.getCode())){//强制通过
            //发送审批结果通知
            commonSendNotice(NoticeTemplateEnum.APPROVE_RESULT_PASS,cfgApproveSyncEntity, createUserId, approveIds, ccIds, thirdUnionMap, summaries, pcLinkByEnv,syncRecordEntity);
        } else if(Objects.equals(approveType, FsActionStatusEnum.ROLLBACK.getCode())){//强制驳回
            //发送审批结果通知
            commonSendNotice(NoticeTemplateEnum.APPROVE_RESULT_REJECT,cfgApproveSyncEntity, createUserId, approveIds, ccIds, thirdUnionMap, summaries, pcLinkByEnv,syncRecordEntity);
        } else if(Objects.equals(approveType, FsActionStatusEnum.RESTORE.getCode())){    //恢复
            //默认发送审核人
            sendApproveNotice(NoticeTemplateEnum.APPROVE,summaries, processTaskManagementEntities, thirdUnionMap, cfgApproveSyncEntity, pcLinkByEnv,syncRecordEntity);
        }
    }

    //发送审批 Bot 消息（除了1008模板外）
    private void commonSendNotice(NoticeTemplateEnum noticeTemplateEnum ,
                                  CfgApproveSyncEntity cfgApproveSyncEntity,
                                  String createUserId,
                                  List<String> approveIds,
                                  List<String> ccIds,
                                  Map<String, ThirdUnionDTO> thirdUnionMap,
                                  List<String> summaries,
                                  String pcLinkByEnv,
                                  ApproveSyncRecordEntity syncRecordEntity) {
        CfgApproveNoticeEntity cfgApproveNoticeEntity = cfgApproveNoticeService.getByNoticeTypeAndMainId(cfgApproveSyncEntity.getId(), noticeTemplateEnum.getCode(), Boolean.TRUE);
        if (Objects.nonNull(cfgApproveNoticeEntity)) {
            List<String> sendUserIds = getSendUserIds(cfgApproveNoticeEntity, createUserId, approveIds, ccIds);
            sendNotice(noticeTemplateEnum.getName(), summaries, createUserId, sendUserIds, thirdUnionMap, cfgApproveSyncEntity, pcLinkByEnv,syncRecordEntity);
        }
    }

    //更新审批 Bot 消息
    private void commonUpdateNotice(List<ProcessTaskManagementEntity> processTaskManagementEntities,String status,ApproveSyncRecordEntity syncRecordEntity) {
        if(CollUtil.isNotEmpty(processTaskManagementEntities)){
            List<ProcessTaskManagementExtDTO.MessageDTO> messageDtos = processTaskManagementExtService.listMessageIdByTaskIds(processTaskManagementEntities.stream().map(ProcessTaskManagementEntity::getId).collect(Collectors.toList()));
            if(CollUtil.isNotEmpty(messageDtos)){
                List<ApproveSyncRecordEntity> list = new ArrayList<>();
                for (ProcessTaskManagementExtDTO.MessageDTO messageDTO : messageDtos) {
                    String messageId = messageDTO.getMessageId();
                    ApproveSyncRecordEntity newRecord =  new ApproveSyncRecordEntity();
                    BeanMapper.copyNonNull(syncRecordEntity,newRecord);
                    newRecord.setDataJson(syncRecordEntity.getDataJson());
                    newRecord.setReceiverId(messageDTO.getCurApproveId());
                    newRecord.setReceiverName(messageDTO.getCurApproveName());
                    Boolean b = fsService.updateApproveMessage(messageId, status);
                    if(Boolean.FALSE.equals(b)){
                        //记录失败
                        newRecord.setErrorReason(String.format("飞书消息更新失败messsageId：%s",messageId));

                        Map<String, Object> dataJson = new HashMap<>();
                        dataJson.put("messageId",messageId);
                        dataJson.put("status",status);
                        dataJson.put("approveSyncFailedType",ApproveSyncFailedTypeEnum.UPDATEAPPROVENOTICE.getCode());
                        newRecord.setDataJson(dataJson);
                        list.add(newRecord);
                    }else {
//                        newRecord.setErrorReason(String.format("飞书消息更新成功messsageId：%s",messsageId));
                        newRecord.setErrorReason("");
                        newRecord.setStatus(ApproveSyncRecordStatusEnum.SUCCESS.getCode());
                        newRecord.setMessageId(messageId);
                        list.add(newRecord);
                    }
                }
                if(CollUtil.isNotEmpty(list)){
                    //保存日志
                    approveSyncRecordService.insertBatch(list);
                }
            }
        }
    }


    //获取需要发送信息的人员集合（去重）
    private static List<String> getSendUserIds(CfgApproveNoticeEntity cfgApproveNoticeEntity, String createUserId, List<String> approveIds, List<String> ccIds) {
        List<String> sendUserIds = new ArrayList<>();
        //具体人员
        String specificPerson = cfgApproveNoticeEntity.getSpecificPerson();
        if (StringUtils.isNotBlank(specificPerson)) {
            String[] specificPersons = specificPerson.split(",");
            sendUserIds.addAll(Arrays.asList(specificPersons));
        }
        //角色类型
        String[] roleTypes = cfgApproveNoticeEntity.getRoleType().split(",");
        for (String roleType : roleTypes) {
            if (roleType.equals(CfgApproveNoticeRoleTypeEnum.APPLICANT.getCode())) {
                sendUserIds.add(createUserId);
            } else if (roleType.equals(CfgApproveNoticeRoleTypeEnum.APPROVER.getCode())) {
                sendUserIds.addAll(approveIds);
            } else if (roleType.equals(CfgApproveNoticeRoleTypeEnum.CC.getCode())) {
                sendUserIds.addAll(ccIds);
            }
        }
        sendUserIds = sendUserIds.stream().distinct().collect(Collectors.toList());
        return sendUserIds;
    }

    /**
     * 发送抄送通知
     * 发送撤销通知
     * @author jack
     * @date 2025-05-21
     */
    public void sendNotice(String tempId,
                           List<String> summaries,
                           String titleUserId,
                           List<String> sendUserIds ,
                           Map<String, ThirdUnionDTO> thirdUnionMap,
                           CfgApproveSyncEntity cfgApproveSyncEntity,
                           String pcLinkByEnv,
                           ApproveSyncRecordEntity syncRecordEntity){
        if(CollUtil.isNotEmpty(sendUserIds)){
            List<FsBotParamsDTO.SendParamsDTO> sendParams = new ArrayList<>();
            List<ApproveSyncRecordEntity> list = new ArrayList<>();
            Map<String , ApproveSyncRecordEntity> map = new HashMap<>();
            for (String userId : sendUserIds) {
                FsBotParamsDTO.SendParamsDTO params = new FsBotParamsDTO.SendParamsDTO();
                params.setTemplateId(tempId);
                params.setUserId(userId);
                params.setTitleUserId(titleUserId);
                params.setApprovalName(cfgApproveSyncEntity.getTitle());
                params.setTitleThirdUserId(thirdUnionMap.get(titleUserId).getThirdUserId());
                params.setTitleUserIdType(UserIdTypeEnum.USERID.getCode());
                params.setActionDetailUrl(pcLinkByEnv);
                params.setSummaries(summaries);

                ApproveSyncRecordEntity newRecord =  new ApproveSyncRecordEntity();
                BeanMapper.copyNonNull(syncRecordEntity,newRecord);
                newRecord.setDataJson(syncRecordEntity.getDataJson());
                newRecord.setReceiverId(userId);
                Map<String, Object> dataJson = BeanUtil.beanToMap(params);
                dataJson.put("approveSyncFailedType",ApproveSyncFailedTypeEnum.SENDNOTICE.getCode());
                newRecord.setDataJson(dataJson);
                if(thirdUnionMap.containsKey(userId)){
                    newRecord.setReceiverName(thirdUnionMap.get(userId).getUserName());
                    if(StringUtils.isBlank(thirdUnionMap.get(userId).getThirdUserId())){
                        newRecord.setErrorReason(ApiError.FS_USER_NOT_BIND.msg);
                        list.add(newRecord);
                    }else {
                        if(thirdUnionMap.containsKey(titleUserId) && Objects.nonNull(thirdUnionMap.get(titleUserId))){
                            params.setTitleThirdUserId(thirdUnionMap.get(titleUserId).getThirdUserId());
                        }
                        params.setThirdUserId(thirdUnionMap.get(userId).getThirdUserId());
                        map.put(params.getThirdUserId(),newRecord);
                        sendParams.add(params);
                    }
                }else{
                    newRecord.setReceiverName("");
                    newRecord.setErrorReason(ApiError.FS_USER_NOT_BIND.msg);
                    list.add(newRecord);
                }
            }

            if(CollUtil.isNotEmpty(sendParams)){
                for (FsBotParamsDTO.SendParamsDTO sendParam : sendParams) {
                    ApproveSyncRecordEntity newRecord = map.get(sendParam.getThirdUserId());
                    //构建请求体
                    Map<String, Object> bodyMap = fsService.buildCcBodyMap(sendParam);
                    //发送消息
                    String messageId = fsService.sendErpApproveSyncMessage(bodyMap);
                    if(StringUtils.isBlank(messageId)){//发送失败
                        newRecord.setErrorReason(String.format("飞书消息发送失败,消息请求体：%s",JSONUtil.toJsonStr(sendParam)));
                    }else {
//                        newRecord.setErrorReason(String.format("飞书消息发送成功messsageId：%s",messageId));
                        newRecord.setErrorReason("");
                        newRecord.setStatus(ApproveSyncRecordStatusEnum.SUCCESS.getCode());
                        newRecord.setMessageId(messageId);
                    }
                    list.add(newRecord);
                }
            }
            //保存日志
            approveSyncRecordService.insertBatch(list);
        }
    }

    /**
     * 发送审核通知
     * @author jack
     * @date 2025-05-21
     */
    public void sendApproveNotice(NoticeTemplateEnum noticeTemplateEnum,
                                  List<String> summaries,
                                  List<ProcessTaskManagementEntity> processTaskManagementEntities,
                                  Map<String, ThirdUnionDTO> thirdUnionMap,
                                  CfgApproveSyncEntity cfgApproveSyncEntity,
                                  String pcLinkByEnv,
                                  ApproveSyncRecordEntity syncRecordEntity) {


        if(CollUtil.isNotEmpty(processTaskManagementEntities)){
            List<ApproveSyncRecordEntity> list = new ArrayList<>();
            Map<String , ApproveSyncRecordEntity> map = new HashMap<>();
            Map<String, Object> cfgDataJson = cfgSettingService.getFsActionCallback();
            List<FsBotParamsDTO.SendParamsDTO> sendParams = new ArrayList<>();
            for (ProcessTaskManagementEntity e : processTaskManagementEntities) {
                FsBotParamsDTO.SendParamsDTO params = new FsBotParamsDTO.SendParamsDTO();
                params.setTemplateId(noticeTemplateEnum.getName());
                params.setUserId(e.getCurApproveId());
                params.setTitleUserId(e.getCreateUserId());
                params.setUuid(e.getId());
                params.setApprovalName(cfgApproveSyncEntity.getTitle());
                params.setTitleUserIdType(UserIdTypeEnum.USERID.getCode());
                params.setActionDetailUrl(pcLinkByEnv);
                params.setActionCallbackUrl(String.valueOf(cfgDataJson.get("actionCallbackUrl")));
                params.setActionCallbackToken(String.valueOf(cfgDataJson.get("actionCallbackToken")));
                params.setActionCallbackKey(String.valueOf(cfgDataJson.get("actionCallbackKey")));
                params.setActionContext("");
                params.setSummaries(summaries);

                ApproveSyncRecordEntity newRecord =  new ApproveSyncRecordEntity();
                BeanMapper.copyNonNull(syncRecordEntity,newRecord);
                newRecord.setDataJson(syncRecordEntity.getDataJson());
                newRecord.setReceiverId(e.getCurApproveId());
                Map<String, Object> dataJson = BeanUtil.beanToMap(params);
                dataJson.put("approveSyncFailedType",ApproveSyncFailedTypeEnum.SENDAPPROVENOTICE.getCode());
                newRecord.setDataJson(dataJson);

                if(thirdUnionMap.containsKey(e.getCurApproveId())){
                    ThirdUnionDTO thirdUnionDTO = thirdUnionMap.get(e.getCurApproveId());
                    newRecord.setReceiverName(thirdUnionDTO.getUserName());
                    if(StringUtils.isBlank(thirdUnionDTO.getThirdUserId())){
                        newRecord.setErrorReason(ApiError.FS_USER_NOT_BIND.msg);
                        list.add(newRecord);
                    }else {
                        params.setThirdUserId(thirdUnionDTO.getThirdUserId());
                        if(thirdUnionMap.containsKey(e.getCreateUserId()) && Objects.nonNull(thirdUnionMap.get(e.getCreateUserId()))){
                            params.setTitleThirdUserId(thirdUnionMap.get(e.getCreateUserId()).getThirdUserId());
                        }
                        sendParams.add(params);
                        map.put(params.getThirdUserId(),newRecord);
                    }
                }else{
                    newRecord.setReceiverName("");
                    newRecord.setErrorReason(ApiError.FS_USER_NOT_BIND.msg);
                    list.add(newRecord);
                }
            }

            if(CollUtil.isNotEmpty(sendParams)){
                for (FsBotParamsDTO.SendParamsDTO sendParam : sendParams) {
                    ApproveSyncRecordEntity newRecord = map.get(sendParam.getThirdUserId());
                    //构建请求体
                    Map<String, Object> bodyMap = fsService.buildApproveBodyMap(sendParam);
                    //发送消息
                    String messageId = fsService.sendErpApproveSyncMessage(bodyMap);
                    if(StringUtils.isBlank(messageId)){//发送失败
                        //todo 记录错误信息
                        newRecord.setErrorReason(String.format("发送失败,消息请求体：%s",JSONUtil.toJsonStr(sendParam)));
                    }else{
//                        newRecord.setErrorReason(String.format("messageId：%s",messageId));
                        newRecord.setErrorReason("");
                        newRecord.setStatus(ApproveSyncRecordStatusEnum.SUCCESS.getCode());
                        newRecord.setMessageId(messageId);

                        //保持messageId 用于后续更新接口
                        ProcessTaskManagementExtEntity entity = new ProcessTaskManagementExtEntity();
                        entity.setProcessTaskManagementId(sendParam.getUuid());
                        entity.setMessageId(messageId);
                        entity.setSoucePlatform(CfgApproveSyncSyncPlatformEnum.FEISHU.getCode());
                        processTaskManagementExtService.save(entity);
                    }
                    list.add(newRecord);
                }
            }
            //保存日志
            approveSyncRecordService.insertBatch(list);
        }
    }

}
