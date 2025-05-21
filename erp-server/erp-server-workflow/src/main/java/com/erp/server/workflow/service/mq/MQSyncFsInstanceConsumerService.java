package com.erp.server.workflow.service.mq;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.common.utils.StringUtils;
import com.common.business.constant.ThirdConstants;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.ThirdpartyPlatformEnum;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.sys.vo.ThirdUnionDTO;
import com.erp.model.workflow.dto.CfgApproveSyncDTO;
import com.erp.model.workflow.dto.FsBotParamsDTO;
import com.erp.model.workflow.entity.*;
import com.erp.model.workflow.enums.CfgApproveNoticeNoticeTypeEnum;
import com.erp.model.workflow.enums.CfgApproveNoticeRoleTypeEnum;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.workflow.handler.CfgApproveSyncBuildHandler;
import com.erp.server.workflow.handler.CfgApproveSyncSendHandler;
import com.erp.server.workflow.service.*;
import com.lark.oapi.service.approval.v4.model.*;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = RocketMqTopic.WORKFLOW_SYNC_FS_INSTANCE_TOPIC,
        selectorExpression = "workflow_sync_fs_instance_tag",
        consumerGroup = RocketMqConsumerGroup.WORKFLOW_SYNC_FS_INSTANCE_CONSUMER)
public class MQSyncFsInstanceConsumerService implements RocketMQListener<CfgApproveSyncDTO.SyncFsProcessToMqDTO> {

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
    private CfgApproveNoticeService cfgApproveNoticeService;
    @Resource
    private CfgApproveSyncBuildHandler cfgApproveSyncBuildHandler;
    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    private ProcessManagementService processManagementService;
    @Override
    public void onMessage(CfgApproveSyncDTO.SyncFsProcessToMqDTO dto) {
        log.info("MQSyncFsInstanceConsumerService 开始");
//        CfgApproveSyncEntity cfgApproveSyncEntity = dto.getCfgApproveSyncEntity();
//        //主流程
//        ProcessManagementEntity processManagementEntity = processManagementService.getById(dto.getProcessManagementId());
//        if(Objects.isNull(processManagementEntity)){
//            //todo 推送记录  失败  流程不存在
//        }
//        //任务
//        List<ProcessTaskManagementEntity> processTaskManagementEntities = processTaskManagementService.listTask(dto.getInstanceId());
//        //抄送任务
//        List<String> taskManagementIds = processTaskManagementEntities.stream().map(ProcessTaskManagementEntity::getId).collect(Collectors.toList());
//        List<ProcessTaskCcEntity> processTaskCcEntities = processTaskCcService.listTackCc(taskManagementIds);
//        //任务列表人员和抄送列表人员飞书信息
//        List<String> allUserIds = new ArrayList<>();
//        //申请人
//        String createUserId = processManagementEntity.getCreateUserId();
//        //审核人
//        List<String> approveIds = processTaskManagementEntities.stream().map(ProcessTaskManagementEntity::getCurApproveId).filter(StringUtil::isNotBlank).collect(Collectors.toList());
//        //抄送人
//        List<String> ccIds = processTaskCcEntities.stream().map(ProcessTaskCcEntity::getCcUserId).filter(StringUtil::isNotBlank).collect(Collectors.toList());
//        // 合并成一个集合（包含去重后的用户ID）
//        allUserIds.add(createUserId);
//        allUserIds.addAll(approveIds);
//        allUserIds.addAll(ccIds);
//        Map<String, ThirdUnionDTO> thirdUnionMap = cfgApproveSyncBuildHandler.getThirdUnionDTOMap(allUserIds);
//        //推送消息 (快接审批)
//        List<CfgApproveSyncFieldMapEntity> fieldMapEntities = cfgApproveSyncFieldMapService.listByMainIds(Arrays.asList(cfgApproveSyncEntity.getId())).stream()
//                .filter(e -> e.getIsQuick().equals(Boolean.TRUE))
//                .collect(Collectors.toList());
//        fieldMapEntities.sort(Comparator.comparingInt(CfgApproveSyncFieldMapEntity::getSort));
//
//        //三方审批同步
//        CreateExternalInstanceReq req = cfgApproveSyncBuildHandler.buildExternalInstanceReq(dto,processManagementEntity, processTaskManagementEntities, processTaskCcEntities, fieldMapEntities, thirdUnionMap);
//        log.info("MQSyncFsInstanceConsumerService request参数:" + JSONUtil.toJsonStr(req.getExternalInstance()));
//        CreateExternalInstanceResp resp = fsService.createExternalInstance(req);
//
//        //todo 判断是否成功，无论成功失败都记录推送记录，
//        if (!resp.success()) {
//            String msg = String.format("code:%s,msg:%s,reqId:%s", resp.getCode(), resp.getMsg(), resp.getRequestId());
//            log.error("同步三方审批实例失败>>>>>{}", msg);
//        } else {
//
//
//            //pc地址
//            String pcLinkByEnv = cfgSettingService.getPcLinkByEnv();
//            //参数map
//            Map<String, Object> variablesMap = dto.getVariablesMap();
//            List<String> summaries = cfgApproveSyncBuildHandler.getSummaries(fieldMapEntities, variablesMap);
//
//            //审批状态
//            String approveType = dto.getApproveType();
//            if (Objects.equals(approveType, ApproveTypeEnum.PASS.getStatus())) {//审核通过
//                //发送审批结果通知
//                CfgApproveNoticeEntity cfgApproveNoticeEntity = cfgApproveNoticeService.getByNoticeTypeAndMainId(cfgApproveSyncEntity.getId(), CfgApproveNoticeNoticeTypeEnum.APPROVERESULT.getCode(), Boolean.TRUE);
//                if (Objects.nonNull(cfgApproveNoticeEntity)) {
//
//                }
//            } else if (Objects.equals(approveType, ApproveTypeEnum.REJECT.getStatus())) {//审核不通过
//                //发送审批结果通知
//                CfgApproveNoticeEntity cfgApproveNoticeEntity = cfgApproveNoticeService.getByNoticeTypeAndMainId(cfgApproveSyncEntity.getId(), CfgApproveNoticeNoticeTypeEnum.APPROVERESULT.getCode(), Boolean.TRUE);
//                if (Objects.nonNull(cfgApproveNoticeEntity)) {
//
//                }
//            } else if (Objects.equals(approveType, ApproveTypeEnum.CANCEL.getStatus())) {//撤销
//                //发送撤销通知
//                CfgApproveNoticeEntity cfgApproveNoticeEntity = cfgApproveNoticeService.getByNoticeTypeAndMainId(cfgApproveSyncEntity.getId(), CfgApproveNoticeNoticeTypeEnum.RECALL.getCode(), Boolean.TRUE);
//                if (Objects.nonNull(cfgApproveNoticeEntity)) {
//
//                }
//            } else {//创建流程
//                //发送审核通知
//                CfgApproveNoticeEntity approveNoticeEntity = cfgApproveNoticeService.getByNoticeTypeAndMainId(cfgApproveSyncEntity.getId(), CfgApproveNoticeNoticeTypeEnum.APPROVE.getCode(), Boolean.TRUE);
//                if (Objects.nonNull(approveNoticeEntity)) {
//                    //默认发送审核人
//                    cfgApproveSyncSendHandler.sendApproveNotice(summaries, processTaskManagementEntities, thirdUnionMap, cfgApproveSyncEntity, pcLinkByEnv);
//                }
//                //发送抄送通知
//                CfgApproveNoticeEntity ccNoticeEntity = cfgApproveNoticeService.getByNoticeTypeAndMainId(cfgApproveSyncEntity.getId(), CfgApproveNoticeNoticeTypeEnum.CC.getCode(), Boolean.TRUE);
//                if (Objects.nonNull(ccNoticeEntity)) {
//                    //具体人员
//                    String specificPerson = ccNoticeEntity.getSpecificPerson();
//                    if (StringUtils.isNotBlank(specificPerson)) {
//                        String[] specificPersons = specificPerson.split(",");
//                    }
//                    //
//                    String[] roleTypes = ccNoticeEntity.getRoleType().split(",");
//                    for (String roleType : roleTypes) {
//                        if (roleType.equals(CfgApproveNoticeRoleTypeEnum.APPLICANT.getCode())) {
//
//                        } else if (roleType.equals(CfgApproveNoticeRoleTypeEnum.CC.getCode())) {
//
//                        } else if (roleType.equals(CfgApproveNoticeRoleTypeEnum.APPROVER.getCode())) {
//
//                        }
//                    }
//
//                    cfgApproveSyncSendHandler.sendCcNotice(summaries, processTaskCcEntities, thirdUnionMap, cfgApproveSyncEntity, pcLinkByEnv);
//                }
//            }
//            log.info("MQSyncFsInstanceConsumerService 结束");
//        }
    }
}
