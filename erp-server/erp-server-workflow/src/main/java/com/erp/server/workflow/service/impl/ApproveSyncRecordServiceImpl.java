package com.erp.server.workflow.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.sys.vo.ThirdUnionDTO;
import com.erp.model.wms.entity.SampleRecipientEntity;
import com.erp.model.workflow.dto.CfgApproveSyncDTO;
import com.erp.model.workflow.dto.FsBotParamsDTO;
import com.erp.model.workflow.dto.WorkflowMqConsumerRecordDTO;
import com.erp.model.workflow.entity.*;
import com.erp.model.workflow.enums.*;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.workflow.handler.CfgApproveSyncBuildHandler;
import com.erp.server.workflow.handler.MQSyncFsHandler;
import com.erp.server.workflow.mapper.ApproveSyncRecordMapper;
import com.erp.server.workflow.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.google.gson.Gson;
import jodd.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.workflow.dto.ApproveSyncRecordDTO;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PROCESS_APPROVE_SYNC_RECORD;

/**
 * <p>
 * ERP审批同步-通知配置 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-05-12
 */
@Slf4j
@Service
public class ApproveSyncRecordServiceImpl extends SuperServiceImpl<ApproveSyncRecordMapper, ApproveSyncRecordEntity> implements ApproveSyncRecordService {


    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private CfgApproveSyncBuildHandler cfgApproveSyncBuildHandler;
    @Resource
    private ProcessManagementService processManagementService;
    @Resource
    private ProcessTaskManagementService processTaskManagementService;
    @Resource
    private FsService fsService;

    @Resource
    private MQSyncFsHandler mqSyncFsHandler;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private ProcessTaskCcService processTaskCcService;
    @Resource
    private CfgApproveSyncService cfgApproveSyncService;
    @Resource
    private MQProducerService mqProducerService;
    @Resource
    private MqConsumerRecordService workflowMqConsumerRecordService;

    @Override
    public List<ApproveSyncRecordDTO.TabListDTO> tabList(PermissionsDTO param) {
        ApproveSyncRecordDTO.PagingParamDTO searchParam = new ApproveSyncRecordDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<ApproveSyncRecordDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        List<ApproveSyncRecordDTO.TabListDTO> result = new ArrayList<>();
        ApproveSyncRecordDTO.TabListDTO enable = list.stream().filter(e -> e.getTabFlag().equals(ApproveSyncRecordStatusEnum.SUCCESS.getCode())).findFirst().orElse(null);
        ApproveSyncRecordDTO.TabListDTO disable = list.stream().filter(e -> e.getTabFlag().equals(ApproveSyncRecordStatusEnum.FAILED.getCode())).findFirst().orElse(null);
        result.add(new ApproveSyncRecordDTO.TabListDTO("all", "全部", 0));
        result.add(new ApproveSyncRecordDTO.TabListDTO(ApproveSyncRecordStatusEnum.SUCCESS.getCode(), ApproveSyncRecordStatusEnum.SUCCESS.getName(), null == enable ? 0 : enable.getCount()));
        result.add(new ApproveSyncRecordDTO.TabListDTO(ApproveSyncRecordStatusEnum.FAILED.getCode(), ApproveSyncRecordStatusEnum.FAILED.getName(), null == disable ? 0 : disable.getCount()));
        return result;
    }

    @Override
    public PagingVO<ApproveSyncRecordDTO.ListDTO> paging(PagingDTO<ApproveSyncRecordDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<ApproveSyncRecordDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    private void fillList(List<ApproveSyncRecordDTO.ListDTO> records) {
        for (ApproveSyncRecordDTO.ListDTO record : records) {
            //单据类型
            String businessType = record.getBusinessType();
            record.setBusinessTypeName(SourceTypeEnum.getName(businessType));

            record.setNoticeTypeName(ApproveSyncRecordNoticeTypeEnum.getName(record.getNoticeType()));

            record.setNoticeMethodName(CfgApproveSyncSyncPlatformEnum.getName(record.getNoticeMethod()));

            record.setStatusName(ApproveSyncRecordStatusEnum.getName(record.getStatus()));

            record.setNoticeNodeName(CfgApproveNoticeNoticeTypeEnum.getName(record.getNoticeNode()));
        }
    }

    @Override
    public void exportList(ApproveSyncRecordDTO.PagingParamDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("三方推送记录导出", EXPORT_PROCESS_APPROVE_SYNC_RECORD.getCode(), param);
    }

    @Override
    public BatchResultDTO repush(String id) {
        ApproveSyncRecordEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到三方推送记录数据"));
        Map<String, Object> dataJson = entity.getDataJson();
        if (CollUtil.isEmpty(dataJson)) {
            return BatchResultDTO.fail(entity.getId(), entity.getId(), "流程未启动");
        }

        String approveSyncFailedType = String.valueOf(dataJson.get("approveSyncFailedType"));
        if (Objects.isNull(dataJson.get("approveSyncFailedType")) || StringUtils.isBlank(approveSyncFailedType)) {
            return BatchResultDTO.fail(entity.getId(), entity.getId(), "重推类型不存在");
        }
        Gson gson = new Gson();
        //创建实例失败
        if (Objects.equals(approveSyncFailedType, ApproveSyncFailedTypeEnum.CREATEINSTANCE.getCode())) {
            try {
                CfgApproveSyncDTO.SyncFsProcessToMqDTO dto = gson.fromJson(gson.toJson(dataJson), CfgApproveSyncDTO.SyncFsProcessToMqDTO.class);
                if (!processManagementService.checkTaskByProcessInstanceId(dto.getInstanceId())) {
                    return BatchResultDTO.fail(entity.getId(), entity.getId(), "重推节点不能小于流程当前节点");
                }

                log.info("repush 开始");
                mqSyncFsHandler.handler(dto);
                log.info("repush 结束");
            } catch (Exception e) {
                return BatchResultDTO.fail(entity.getId(), entity.getId(), "创建实例失败");
            }
        }
        //发送消息失败
        if (Objects.equals(approveSyncFailedType, ApproveSyncFailedTypeEnum.SENDNOTICE.getCode())) {
            BatchResultDTO failed = getRepushNotice(approveSyncFailedType, gson, dataJson, entity);
            if (failed != null) return failed;
        }
        //发送审批消息失败
        if (Objects.equals(approveSyncFailedType, ApproveSyncFailedTypeEnum.SENDAPPROVENOTICE.getCode())) {
            BatchResultDTO failed = getRepushNotice(approveSyncFailedType, gson, dataJson, entity);
            if (failed != null) return failed;
        }
        //更新审批消息失败
        if (Objects.equals(approveSyncFailedType, ApproveSyncFailedTypeEnum.UPDATEAPPROVENOTICE.getCode())) {
            String messageId = String.valueOf(dataJson.get("messageId"));
            String status = String.valueOf(dataJson.get("status"));
            if(StringUtils.isBlank(messageId) || StringUtils.isBlank(status)){
                return BatchResultDTO.fail(entity.getId(), entity.getId(), "更新审批消息失败");
            }
            Boolean b = fsService.updateApproveMessage(messageId, status);
            if (Boolean.TRUE.equals(b)) {
                lambdaUpdate()
                        .eq(ApproveSyncRecordEntity::getId,entity.getId())
                        .set(ApproveSyncRecordEntity::getMessageId,messageId)
                        .set(ApproveSyncRecordEntity::getStatus,ApproveSyncRecordStatusEnum.SUCCESS.getCode())
                        .set(ApproveSyncRecordEntity::getErrorReason,"")
                        .set(ApproveSyncRecordEntity::getSendTime, LocalDateTime.now())
                        .update();
            }else {
                return BatchResultDTO.fail(entity.getId(), entity.getId(), "更新审批消息失败");
            }
        }

        return BatchResultDTO.success(entity.getId(), entity.getId(), "重推成功");
    }



    private BatchResultDTO getRepushNotice(String  approveSyncFailedType , Gson gson, Map<String, Object> dataJson, ApproveSyncRecordEntity entity) {
        FsBotParamsDTO.SendParamsDTO params = gson.fromJson(gson.toJson(dataJson), FsBotParamsDTO.SendParamsDTO.class);
        String userId = params.getUserId();
        String titleUserId = params.getTitleUserId();
        List<String> allUserIds = new ArrayList<>();
        if(StringUtils.isNotBlank(userId)){
            allUserIds.add( userId);
        }
        if(StringUtils.isNotBlank(titleUserId)){
            allUserIds.add( titleUserId);
        }
        Map<String, ThirdUnionDTO> thirdUnionMap = cfgApproveSyncBuildHandler.getThirdUnionDTOMap(allUserIds);
        if(CollUtil.isEmpty(thirdUnionMap)) {
            return BatchResultDTO.fail(entity.getId(), entity.getId(), ApiError.FS_USER_NOT_BIND.msg);
        }

        if(thirdUnionMap.containsKey(titleUserId) && Objects.nonNull(thirdUnionMap.get(titleUserId))){
            params.setThirdUserId(thirdUnionMap.get(titleUserId).getThirdUserId());
        }
        if(thirdUnionMap.containsKey(userId) && StringUtils.isNotBlank(thirdUnionMap.get(userId).getThirdUserId())){
            params.setThirdUserId(thirdUnionMap.get(userId).getThirdUserId());

            //构建请求体
            Map<String, Object>  bodyMap = null;
            if(Objects.equals(approveSyncFailedType, ApproveSyncFailedTypeEnum.SENDAPPROVENOTICE.getCode())){
                bodyMap = fsService.buildApproveBodyMap(params);
            }else if(Objects.equals(approveSyncFailedType, ApproveSyncFailedTypeEnum.SENDNOTICE.getCode())){
                bodyMap = fsService.buildCcBodyMap(params);
            }
            if(Objects.nonNull(bodyMap)){
                //发送消息
                String messageId = fsService.sendErpApproveSyncMessage(bodyMap);
                if(StringUtils.isNotBlank(messageId)){//发送失败
                    lambdaUpdate()
                            .eq(ApproveSyncRecordEntity::getId,entity.getId())
                            .set(ApproveSyncRecordEntity::getMessageId,messageId)
                            .set(ApproveSyncRecordEntity::getStatus,ApproveSyncRecordStatusEnum.SUCCESS.getCode())
                            .set(ApproveSyncRecordEntity::getErrorReason,"")
                            .set(ApproveSyncRecordEntity::getSendTime, LocalDateTime.now())
                            .update();
                }
            }else {
                return BatchResultDTO.fail(entity.getId(), entity.getId(), "重推失败");
            }
        }else {
            return BatchResultDTO.fail(entity.getId(), entity.getId(), ApiError.FS_USER_NOT_BIND.msg);
        }
        return null;
    }

    @Override
    public void insertBatch(List<ApproveSyncRecordEntity> list) {
        if(CollUtil.isNotEmpty(list)){
            List<String> userIds = list.stream().map(ApproveSyncRecordEntity::getReceiverId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
            Map<String, String> map = sysUserFeign.getUserListByUserIds(userIds).stream().collect(Collectors.toMap(FindUserDTO::getUserId, FindUserDTO::getUserName, (o1, o2) -> o1));
            for (ApproveSyncRecordEntity approveSyncRecordEntity : list) {
                approveSyncRecordEntity.setReceiverName(map.getOrDefault(approveSyncRecordEntity.getReceiverId(),""));
                if(Objects.isNull(approveSyncRecordEntity.getMessageId())){
                    approveSyncRecordEntity.setMessageId("");
                }
            }
            baseMapper.insertBatch(list);
        }
    }

    @Override
    public void externalInstance(ApproveSyncRecordDTO.externalInstanceParamDTO dto) {
//        if(Objects.isNull(dto) || CollUtil.isEmpty(dto.getProcessManagementId())){
//            return;
//        }
//
//        List<ProcessManagementEntity> list = processManagementService.lambdaQuery().in(ProcessManagementEntity::getId, dto.getProcessManagementId()).list();
//        if(CollUtil.isNotEmpty(list)){
//            List<String> processInstanceIds = list.stream().map(ProcessManagementEntity::getProcessInstanceId).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
//            if(CollUtil.isNotEmpty(processInstanceIds)){
//                Map<String, List<ProcessTaskManagementEntity>> taskMap = processTaskManagementService.lambdaQuery()
//                        .in(ProcessTaskManagementEntity::getProcessInstanceId, processInstanceIds)
//                        .list().stream().collect(Collectors.groupingBy(ProcessTaskManagementEntity::getProcessInstanceId));
//
//                for (ProcessManagementEntity processManagementEntity : list) {
//                    if(taskMap.containsKey(processManagementEntity.getProcessInstanceId())){
//                        //任务列表
//                        List<ProcessTaskManagementEntity> processTaskManagementEntities = taskMap.get(processManagementEntity.getProcessInstanceId());
//
//                        //抄送任务
//                        List<String> taskManagementIds = processTaskManagementEntities.stream().map(ProcessTaskManagementEntity::getId).collect(Collectors.toList());
//                        List<ProcessTaskCcEntity> processTaskCcEntities = processTaskCcService.listTackCc(taskManagementIds);
//
//                        //任务列表人员和抄送列表人员飞书信息
//                        List<String> allUserIds = new ArrayList<>();
//                        //申请人
//                        String createUserId = processManagementEntity.getCreateUserId();
//                        //审核人
//                        List<String> approveIds = processTaskManagementEntities.stream().map(ProcessTaskManagementEntity::getCurApproveId).filter(StringUtil::isNotBlank).collect(Collectors.toList());
//                        //抄送人
//                        List<String> ccIds = processTaskCcEntities.stream().map(ProcessTaskCcEntity::getCcUserId).filter(StringUtil::isNotBlank).collect(Collectors.toList());
//                        // 合并成一个集合（包含去重后的用户ID）
//                        allUserIds.add(createUserId);
//                        allUserIds.addAll(approveIds);
//                        allUserIds.addAll(ccIds);
//                        allUserIds = allUserIds.stream().distinct().collect(Collectors.toList());
//                        Map<String, ThirdUnionDTO> thirdUnionMap = cfgApproveSyncBuildHandler.getThirdUnionDTOMap(allUserIds);
//
//
//
//
//                    }
//                }
//            }
//        }
    }

    @Override
    public void externalInstance2(ApproveSyncRecordDTO.externalInstanceParamDTO dto) {
        List<String> status = Arrays.asList("approve", "reject");

        List<SampleRecipientEntity> list = FeignQuery.create(SampleRecipientEntity.class).in(SampleRecipientEntity::getId, dto.getIds()).list();
        List<ProcessManagementEntity> processManagementEntities = processManagementService.lambdaQuery().eq(ProcessManagementEntity::getIsDeleted,false).in(ProcessManagementEntity::getBusinessId, dto.getIds()).list();

        for (SampleRecipientEntity entity : list) {
            Map<String, Object> variables = BeanUtil.beanToMap(entity);

            ProcessManagementEntity managementTask = processManagementEntities.stream().filter(e -> e.getBusinessId().equals(entity.getId())).findFirst().orElse(null);
            if(Objects.nonNull(managementTask)){

                List<ProcessTaskManagementEntity> taskList = processTaskManagementService.lambdaQuery()
                        .eq(ProcessTaskManagementEntity::getProcessInstanceId, managementTask.getProcessInstanceId())
                        .eq(ProcessTaskManagementEntity::getIsDeleted, false)
                        .in(ProcessTaskManagementEntity::getTaskStatus, status)
                        .orderByDesc(ProcessTaskManagementEntity::getCreateTime)
                        .list();


                //判断该单据类型是否有ERP审批同步定义
                CfgApproveSyncDTO.SyncFsProcessToMqDTO mqDto = new CfgApproveSyncDTO.SyncFsProcessToMqDTO();
                mqDto.setProcessManagementId(managementTask.getId());
                mqDto.setBusinessName(managementTask.getBusinessName());
                mqDto.setBusinessCode(managementTask.getBusinessCode());
                mqDto.setInstanceId(managementTask.getProcessInstanceId());
                mqDto.setCurTaskId(taskList.get(0).getId());
                mqDto.setOperator(managementTask.getCreateUserId());
                mqDto.setCreateUserId(managementTask.getCreateUserId());
                mqDto.setVariablesMap(variables);
                mqDto.setBusinessKey(managementTask.getBusinessKey());
                mqDto.setApproveType(managementTask.getApproveStatus().getStatus());
                syncFsExternalInstance(mqDto);
            }
        }
    }

    /**
     * 飞书三方审批实例同步
     * @author jack
     * @date 2025-05-21
     */
    private void syncFsExternalInstance(CfgApproveSyncDTO.SyncFsProcessToMqDTO mqDto) {
        //判断该单据类型是否有ERP审批同步定义
        List<CfgApproveSyncEntity> cfgApproveSyncEntities = cfgApproveSyncService.getByBusinessType(Arrays.asList(mqDto.getBusinessKey()))
                .stream()
                .filter(e -> e.getEnableStatus().equals(Boolean.TRUE))
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(cfgApproveSyncEntities)) {
            CfgApproveSyncEntity cfgApproveSyncEntity = cfgApproveSyncEntities.get(0);
            mqDto.setCfgApproveSyncEntity(cfgApproveSyncEntity);

            //保存mq消费记录
            if (addMqConsumerRecord(mqDto)) return;

            mqProducerService.syncClassMsgWithDelayLevel(RocketMqTopic.WORKFLOW_SYNC_FS_INSTANCE_TOPIC, RocketMqTagEnum.WORKFLOW_SYNC_FS_INSTANCE_TAG.getName(),mqDto , mqDto.getProcessManagementId(),1);
        }
    }

    private boolean addMqConsumerRecord(CfgApproveSyncDTO.SyncFsProcessToMqDTO mqDto) {
        log.info("addMqConsumerRecord开始 保存MQ消费记录, businessKey={}, processManagementId={}, curTaskId={}", mqDto.getBusinessKey(), mqDto.getProcessManagementId(), mqDto.getCurTaskId());
        //保存mq消费记录
        Map<String, Object> convertedMap = BeanUtil.beanToMap(mqDto);

        // 构建DTO
        WorkflowMqConsumerRecordDTO.MqDTO dto = new WorkflowMqConsumerRecordDTO.MqDTO();
        dto.setDataJson(convertedMap);
        dto.setBusinessKey(mqDto.getBusinessKey());
        dto.setTopic(RocketMqTopic.WORKFLOW_SYNC_FS_INSTANCE_TOPIC);
        dto.setConsumerGroup(RocketMqConsumerGroup.WORKFLOW_SYNC_FS_INSTANCE_CONSUMER);
        dto.setTag(RocketMqTagEnum.WORKFLOW_SYNC_FS_INSTANCE_TAG.getName());
        String id = workflowMqConsumerRecordService.addMqRecord(dto);
        if (StringUtils.isBlank(id)) {
            log.error("addMqConsumerRecord 保存MQ消费记录失败, businessKey={}, processManagementId={}, curTaskId={}", mqDto.getBusinessKey(), mqDto.getProcessManagementId(), mqDto.getCurTaskId());
            return true;
        }
        log.info("addMqConsumerRecord结束 保存MQ消费记录");
        return false;
    }
}
