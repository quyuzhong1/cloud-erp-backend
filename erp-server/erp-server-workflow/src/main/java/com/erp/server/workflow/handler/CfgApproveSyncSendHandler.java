package com.erp.server.workflow.handler;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.nacos.common.utils.StringUtils;
import com.common.business.enums.ApproveTypeEnum;
import com.erp.model.sys.vo.ThirdUnionDTO;
import com.erp.model.workflow.dto.CfgApproveSyncDTO;
import com.erp.model.workflow.dto.FsBotParamsDTO;
import com.erp.model.workflow.entity.*;
import com.erp.model.workflow.enums.*;
import com.erp.sdk.fs.enmu.FsActionStatusEnum;
import com.erp.sdk.fs.enmu.UserIdTypeEnum;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.workflow.service.CfgApproveNoticeService;
import com.erp.server.workflow.service.CfgSettingService;
import com.erp.server.workflow.service.ProcessTaskManagementExtService;
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



    public void updateNotice(CfgApproveSyncDTO.SyncFsProcessToMqDTO dto,
                           List<CfgApproveSyncFieldMapEntity> fieldMapEntities,
                           ProcessManagementEntity processManagementEntity,
                           CfgApproveSyncEntity cfgApproveSyncEntity,
                           String createUserId,
                           List<String> approveIds,
                           List<String> ccIds,
                           Map<String, ThirdUnionDTO> thirdUnionMap,
                           List<ProcessTaskManagementEntity> processTaskManagementEntities) {
        //pc地址
        String pcLinkByEnv = cfgSettingService.getPcLinkByEnv();
        //参数map
        Map<String, Object> variablesMap = dto.getVariablesMap();
        List<String> summaries = cfgApproveSyncBuildHandler.getSummaries(fieldMapEntities, variablesMap);

        //审批状态
        String approveType = dto.getApproveType();
        if (Objects.equals(approveType, ApproveTypeEnum.PASS.getStatus())){//审核通过
            //更新审批结果通知
            commonUpdateNotice(processTaskManagementEntities,FsActionStatusEnum.APPROVED.getCode());
        } else if (Objects.equals(approveType, ApproveTypeEnum.REJECT.getStatus())) {//审核不通过
            //更新审批结果通知
            commonUpdateNotice(processTaskManagementEntities,FsActionStatusEnum.REJECTED.getCode());
        } else if (Objects.equals(approveType, ApproveTypeEnum.CANCEL.getStatus())) {//撤销
            //更新审批结果通知
            commonUpdateNotice(processTaskManagementEntities,FsActionStatusEnum.CANCELLED.getCode());
        }
    }

    public void sendNotice(CfgApproveSyncDTO.SyncFsProcessToMqDTO dto,
                           List<CfgApproveSyncFieldMapEntity> fieldMapEntities,
                           ProcessManagementEntity processManagementEntity,
                           CfgApproveSyncEntity cfgApproveSyncEntity,
                           String createUserId,
                           List<String> approveIds,
                           List<String> ccIds,
                           Map<String, ThirdUnionDTO> thirdUnionMap,
                           List<ProcessTaskManagementEntity> processTaskManagementEntities) {
        //pc地址
        String pcLinkByEnv = cfgSettingService.getPcLinkByEnv();
        //参数map
        Map<String, Object> variablesMap = dto.getVariablesMap();
        List<String> summaries = cfgApproveSyncBuildHandler.getSummaries(fieldMapEntities, variablesMap);

        //审批状态
        String approveType = dto.getApproveType();
        if(StringUtils.isBlank(approveType)){//创建流程
            //发送审核通知
            CfgApproveNoticeEntity approveNoticeEntity = cfgApproveNoticeService.getByNoticeTypeAndMainId(cfgApproveSyncEntity.getId(), CfgApproveNoticeNoticeTypeEnum.APPROVE.getCode(), Boolean.TRUE);
            if (Objects.nonNull(approveNoticeEntity)) {
                //默认发送审核人
                sendApproveNotice(NoticeTemplateEnum.APPROVE,summaries, processTaskManagementEntities, thirdUnionMap, cfgApproveSyncEntity, pcLinkByEnv);
            }
            //发送抄送通知
            commonSendNotice(NoticeTemplateEnum.CC,cfgApproveSyncEntity, createUserId, approveIds, ccIds, thirdUnionMap, summaries, pcLinkByEnv);
        }if (Objects.equals(approveType, ApproveTypeEnum.PASS.getStatus())//审核通过并且流程已经完成
                && Objects.equals(processManagementEntity.getProcessStatus(), ProcessStatusEnum.FINISH)) {
            //发送审批结果通知
            commonSendNotice(NoticeTemplateEnum.APPROVE_RESULT_PASS,cfgApproveSyncEntity, createUserId, approveIds, ccIds, thirdUnionMap, summaries, pcLinkByEnv);
        } else if (Objects.equals(approveType, ApproveTypeEnum.REJECT.getStatus())
                && Objects.equals(processManagementEntity.getProcessStatus(), ProcessStatusEnum.FINISH)) {//审核不通过并且流程已经完成
            //发送审批结果通知
            commonSendNotice(NoticeTemplateEnum.APPROVE_RESULT_REJECT,cfgApproveSyncEntity, createUserId, approveIds, ccIds, thirdUnionMap, summaries, pcLinkByEnv);
        } else if (Objects.equals(approveType, ApproveTypeEnum.CANCEL.getStatus())) {//撤销
            //发送撤销通知
            commonSendNotice(NoticeTemplateEnum.RECALL,cfgApproveSyncEntity, createUserId, approveIds, ccIds, thirdUnionMap, summaries, pcLinkByEnv);
        }
    }

    //发送审批 Bot 消息（除了1008模板外）
    private void commonSendNotice(NoticeTemplateEnum noticeTemplateEnum , CfgApproveSyncEntity cfgApproveSyncEntity, String createUserId, List<String> approveIds, List<String> ccIds, Map<String, ThirdUnionDTO> thirdUnionMap, List<String> summaries, String pcLinkByEnv) {
        CfgApproveNoticeEntity cfgApproveNoticeEntity = cfgApproveNoticeService.getByNoticeTypeAndMainId(cfgApproveSyncEntity.getId(), noticeTemplateEnum.getCode(), Boolean.TRUE);
        if (Objects.nonNull(cfgApproveNoticeEntity)) {
            List<String> sendUserIds = getSendUserIds(cfgApproveNoticeEntity, createUserId, approveIds, ccIds);
            sendCcNotice(noticeTemplateEnum.getName(), summaries, createUserId, sendUserIds, thirdUnionMap, cfgApproveSyncEntity, pcLinkByEnv);
        }
    }

    //更新审批 Bot 消息
    private void commonUpdateNotice(List<ProcessTaskManagementEntity> processTaskManagementEntities,String status) {
        List<String> messsageIds = processTaskManagementExtService.listByProcessTaskManagementIds(processTaskManagementEntities.stream().map(ProcessTaskManagementEntity::getId).collect(Collectors.toList()));
        if(CollUtil.isNotEmpty(messsageIds)){
            for (String messsageId : messsageIds) {
                Boolean b = fsService.updateApproveMessage(messsageId, status);
                if(!b){
                    //todo , 记录失败

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
    public void sendCcNotice(String tempId,List<String> summaries,String titleUserId,List<String> sendUserIds , Map<String, ThirdUnionDTO> thirdUnionMap, CfgApproveSyncEntity cfgApproveSyncEntity,String pcLinkByEnv){
        if(CollUtil.isNotEmpty(sendUserIds)){
            List<FsBotParamsDTO.SendParamsDTO> sendParams = new ArrayList<>();
            for (String userId : sendUserIds) {
                if(thirdUnionMap.containsKey(userId)){
                    FsBotParamsDTO.SendParamsDTO params = new FsBotParamsDTO.SendParamsDTO();
                    params.setTemplateId(tempId);
                    params.setUserId(thirdUnionMap.get(userId).getThirdUserId());
//                    params.setUuid(e.getId());
                    params.setApprovalName(cfgApproveSyncEntity.getTitle());
                    params.setTitleUserId(thirdUnionMap.get(titleUserId).getThirdUserId());
                    params.setTitleUserIdType(UserIdTypeEnum.USERID.getCode());
                    params.setActionDetailUrl(pcLinkByEnv);
                    params.setActionCallbackUrl("");
                    params.setActionCallbackToken("");
                    params.setActionCallbackKey("");
                    params.setActionContext("");
                    params.setSummaries(summaries);
                    sendParams.add(params);
                }
            }

            if(CollUtil.isNotEmpty(sendParams)){
                for (FsBotParamsDTO.SendParamsDTO sendParam : sendParams) {
                    //构建请求体
                    Map<String, Object> bodyMap = fsService.buildCcBodyMap(sendParam);
                    //发送消息
                    String messageId = fsService.sendErpApproveSyncMessage(bodyMap);
                    if(StringUtils.isBlank(messageId)){//发送失败
                        //todo 记录错误信息
                    }
                }
            }
        }
    }

    /**
     * 发送审核通知
     * @author jack
     * @date 2025-05-21
     */
    public void sendApproveNotice(NoticeTemplateEnum noticeTemplateEnum, List<String> summaries, List<ProcessTaskManagementEntity> processTaskManagementEntities, Map<String, ThirdUnionDTO> thirdUnionMap, CfgApproveSyncEntity cfgApproveSyncEntity,String pcLinkByEnv) {
        if(CollUtil.isNotEmpty(processTaskManagementEntities)){
            List<FsBotParamsDTO.SendParamsDTO> sendParams = new ArrayList<>();
            for (ProcessTaskManagementEntity e : processTaskManagementEntities) {
                if(thirdUnionMap.containsKey(e.getCreateUserId())){
                    FsBotParamsDTO.SendParamsDTO params = new FsBotParamsDTO.SendParamsDTO();
                    params.setTemplateId(noticeTemplateEnum.getName());
                    params.setUserId(thirdUnionMap.get(e.getCurApproveId()).getThirdUserId());
                    params.setUuid(e.getId());
                    params.setApprovalName(cfgApproveSyncEntity.getTitle());
                    params.setTitleUserId(thirdUnionMap.get(e.getCreateUserId()).getThirdUserId());
                    params.setTitleUserIdType(UserIdTypeEnum.USERID.getCode());
                    params.setActionDetailUrl(pcLinkByEnv);
                    params.setActionCallbackUrl("");
                    params.setActionCallbackToken("");
                    params.setActionCallbackKey("");
                    params.setActionContext("");
                    params.setSummaries(summaries);
                    sendParams.add(params);
                }
            }

            if(CollUtil.isNotEmpty(sendParams)){
                for (FsBotParamsDTO.SendParamsDTO sendParam : sendParams) {
                    //构建请求体
                    Map<String, Object> bodyMap = fsService.buildApproveBodyMap(sendParam);
                    //发送消息
                    String messageId = fsService.sendErpApproveSyncMessage(bodyMap);
                    if(StringUtils.isBlank(messageId)){//发送失败
                        //todo 记录错误信息
                    }else{
                        //保持messageId 用于后续更新接口
                        ProcessTaskManagementExtEntity entity = new ProcessTaskManagementExtEntity();
                        entity.setProcessTaskManagementId(sendParam.getUuid());
                        entity.setMessageId(messageId);
                        entity.setSoucePlatform(CfgApproveSyncSyncPlatformEnum.FEISHU.getCode());
                        processTaskManagementExtService.save(entity);
                    }
                }
            }
        }
    }

}
