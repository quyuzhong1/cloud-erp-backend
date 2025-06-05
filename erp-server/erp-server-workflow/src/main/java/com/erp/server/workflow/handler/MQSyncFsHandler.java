package com.erp.server.workflow.handler;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.common.utils.StringUtils;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.core.utils.BeanMapper;
import com.erp.model.sys.vo.ThirdUnionDTO;
import com.erp.model.workflow.dto.CfgApproveSyncDTO;
import com.erp.model.workflow.dto.FsBotParamsDTO;
import com.erp.model.workflow.entity.*;
import com.erp.model.workflow.enums.*;
import com.erp.sdk.fs.enmu.FsActionStatusEnum;
import com.erp.sdk.fs.enmu.UserIdTypeEnum;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.workflow.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.lark.oapi.service.approval.v4.model.CheckExternalInstanceReq;
import com.lark.oapi.service.approval.v4.model.CheckExternalInstanceResp;
import com.lark.oapi.service.approval.v4.model.CreateExternalInstanceReq;
import com.lark.oapi.service.approval.v4.model.CreateExternalInstanceResp;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author jack
 * @date 2025-05-21
 */
@Slf4j
@Component
public class MQSyncFsHandler {

    @Resource
    private CfgApproveSyncFieldMapService cfgApproveSyncFieldMapService;
    @Resource
    private ProcessTaskManagementService processTaskManagementService;
    @Resource
    private ProcessTaskCcService processTaskCcService;
    @Resource
    private FsService fsService;
    @Resource
    private CfgApproveSyncSendHandler cfgApproveSyncSendHandler;
    @Resource
    private CfgApproveSyncBuildHandler cfgApproveSyncBuildHandler;
    @Resource
    private ProcessManagementService processManagementService;
    @Resource
    private ApproveSyncRecordService approveSyncRecordService;

    public boolean handler(CfgApproveSyncDTO.SyncFsProcessToMqDTO dto) {
        CfgApproveSyncEntity cfgApproveSyncEntity = dto.getCfgApproveSyncEntity();

        ApproveSyncRecordEntity syncRecordEntity = buildSyncRecord(dto);
        String errorReason ="";
        //主流程
        ProcessManagementEntity processManagementEntity = processManagementService.getById(dto.getProcessManagementId());
        if(Objects.isNull(processManagementEntity)){
            //主流程不存在
            errorReason = String.format("【%s】流程不存在", dto.getBusinessCode());
            syncRecordEntity.setErrorReason(errorReason);
            approveSyncRecordService.save(syncRecordEntity);
            return true;
        }

        //构建三方审批同步实例请求体
        //审批状态
        String approveType = dto.getApproveType();
        dto.setFSApprovalStatusEnum(FSApprovalStatusEnum.PENDING);//默认审批中
        if (Objects.equals(approveType, ApproveTypeEnum.PASS.getStatus())
                && Objects.equals(processManagementEntity.getProcessStatus(), ProcessStatusEnum.FINISH)) {
            //审核通过并且流程已经完成
            dto.setFSApprovalStatusEnum(FSApprovalStatusEnum.APPROVED);
            syncRecordEntity.setNoticeNode(CfgApproveNoticeNoticeTypeEnum.APPROVERESULT.getCode());
        } else if (Objects.equals(approveType, ApproveTypeEnum.REJECT.getStatus())
                && Objects.equals(processManagementEntity.getProcessStatus(), ProcessStatusEnum.FINISH)) {
            //审核不通过并且流程已经完成
            dto.setFSApprovalStatusEnum(FSApprovalStatusEnum.REJECTED);
            syncRecordEntity.setNoticeNode(CfgApproveNoticeNoticeTypeEnum.APPROVERESULT.getCode());
        } else if (Objects.equals(approveType, ApproveTypeEnum.CANCEL.getStatus())) {
            //撤销
            dto.setFSApprovalStatusEnum(FSApprovalStatusEnum.CANCELED);
            syncRecordEntity.setNoticeNode(CfgApproveNoticeNoticeTypeEnum.RECALL.getCode());
        } else if(Objects.equals(approveType, FsActionStatusEnum.FORWARDED.getCode())){//转交

        } else if(Objects.equals(approveType, FsActionStatusEnum.PROCESSED.getCode())){
            //强制通过
            dto.setFSApprovalStatusEnum(FSApprovalStatusEnum.APPROVED);
            syncRecordEntity.setNoticeNode(CfgApproveNoticeNoticeTypeEnum.APPROVERESULT.getCode());
        } else if(Objects.equals(approveType, FsActionStatusEnum.ROLLBACK.getCode())){
            //强制驳回
            dto.setFSApprovalStatusEnum(FSApprovalStatusEnum.REJECTED);
            syncRecordEntity.setNoticeNode(CfgApproveNoticeNoticeTypeEnum.APPROVERESULT.getCode());
        } else if(Objects.equals(approveType, FsActionStatusEnum.SUSPEND.getCode())){
            //暂停
        } else if(Objects.equals(approveType, FsActionStatusEnum.RESTORE.getCode())){
            //恢复
        }

        //任务
        List<ProcessTaskManagementEntity> processTaskManagementEntities = processTaskManagementService.listTask(dto.getInstanceId());
        //抄送任务
        List<String> taskManagementIds = processTaskManagementEntities.stream().map(ProcessTaskManagementEntity::getId).collect(Collectors.toList());
        List<ProcessTaskCcEntity> processTaskCcEntities = processTaskCcService.listTackCc(taskManagementIds);
        //任务列表人员和抄送列表人员飞书信息
        List<String> allUserIds = new ArrayList<>();
        //申请人
        String createUserId = processManagementEntity.getCreateUserId();
        //审核人
        List<String> approveIds = processTaskManagementEntities.stream().map(ProcessTaskManagementEntity::getCurApproveId).filter(StringUtil::isNotBlank).collect(Collectors.toList());
        //抄送人
        List<String> ccIds = processTaskCcEntities.stream().map(ProcessTaskCcEntity::getCcUserId).filter(StringUtil::isNotBlank).collect(Collectors.toList());
        // 合并成一个集合（包含去重后的用户ID）
        allUserIds.add(createUserId);
        allUserIds.addAll(approveIds);
        allUserIds.addAll(ccIds);
        allUserIds = allUserIds.stream().distinct().collect(Collectors.toList());
        Map<String, ThirdUnionDTO> thirdUnionMap = cfgApproveSyncBuildHandler.getThirdUnionDTOMap(allUserIds);

        //推送消息 (快捷审批)
        List<CfgApproveSyncFieldMapEntity> fieldMapEntities = cfgApproveSyncFieldMapService.listByMainIds(Arrays.asList(cfgApproveSyncEntity.getId())).stream()
                .filter(e -> e.getIsQuick().equals(Boolean.TRUE))
                .collect(Collectors.toList());
        fieldMapEntities.sort(Comparator.comparingInt(CfgApproveSyncFieldMapEntity::getSort));

        CreateExternalInstanceReq req = cfgApproveSyncBuildHandler.buildExternalInstanceReq(dto,processManagementEntity, processTaskManagementEntities, processTaskCcEntities, fieldMapEntities, thirdUnionMap,syncRecordEntity);
        if(Objects.isNull(req)){
            return true;
        }
        log.info("MQSyncFsInstanceConsumerService 同步三方审批实例请求参数: {}" ,  new Gson().toJson(req.getExternalInstance()));
        //三方审批同步
        CreateExternalInstanceResp resp = fsService.createExternalInstance(req);
        log.info("MQSyncFsInstanceConsumerService 同步三方审批实例响应参数: {}" ,  new Gson().toJson(resp));

        //todo 判断是否成功，无论成功失败都记录推送记录，
        if (!resp.success()) {
            String msg = String.format("同步三方审批实例失败:code:%s,msg:%s,reqId:%s", resp.getCode(), resp.getMsg(), resp.getRequestId());
            log.error("{}", msg);
            syncRecordEntity.setErrorReason(msg);
            approveSyncRecordService.save(syncRecordEntity);
        } else {
            //获取操作的taskId
            String curTaskId = dto.getCurTaskId();
            //更新消息
            cfgApproveSyncSendHandler.updateNotice(dto,curTaskId,processTaskManagementEntities,syncRecordEntity);

            //消息推送
            cfgApproveSyncSendHandler.sendNotice(dto, fieldMapEntities, processManagementEntity, cfgApproveSyncEntity, createUserId, approveIds, ccIds, thirdUnionMap, processTaskManagementEntities,syncRecordEntity);

            //校验三方审批实例
            CheckExternalInstanceReq checkExternalInstanceReq = cfgApproveSyncBuildHandler.buildExternalInstanceReq(processManagementEntity, processTaskManagementEntities);
            log.info("MQSyncFsInstanceConsumerService 校验三方审批实例请求参数: {}" ,  new Gson().toJson(checkExternalInstanceReq));

            CheckExternalInstanceResp checkExternalInstanceResp = fsService.checkExternalInstance(checkExternalInstanceReq);
            log.info("MQSyncFsInstanceConsumerService 校验三方审批实例响应参数: {}" ,  new Gson().toJson(checkExternalInstanceResp));
        }
        return false;
    }

    //构建一个失败的记录模板
    private static ApproveSyncRecordEntity buildSyncRecord(CfgApproveSyncDTO.SyncFsProcessToMqDTO dto) {
        CfgApproveSyncEntity cfgApproveSyncEntity = dto.getCfgApproveSyncEntity();
        ApproveSyncRecordEntity approveSyncRecordEntity = new ApproveSyncRecordEntity();
        approveSyncRecordEntity.setCfgApproveSyncId(cfgApproveSyncEntity.getId());
        approveSyncRecordEntity.setNoticeType(ApproveSyncRecordNoticeTypeEnum.APPROVALPUSH.getCode());
        approveSyncRecordEntity.setBusinessType(cfgApproveSyncEntity.getBusinessType());
        approveSyncRecordEntity.setBusinessCode(dto.getBusinessCode());
        approveSyncRecordEntity.setNoticeMethod(CfgApproveSyncSyncPlatformEnum.FEISHU.getCode());
        approveSyncRecordEntity.setSendTime(LocalDateTime.now());
        approveSyncRecordEntity.setTitle(cfgApproveSyncEntity.getTitle());
        approveSyncRecordEntity.setStatus(ApproveSyncRecordStatusEnum.FAILED.getCode());
        approveSyncRecordEntity.setNoticeNode(CfgApproveNoticeNoticeTypeEnum.APPROVE.getCode());
        return approveSyncRecordEntity;
    }

}
