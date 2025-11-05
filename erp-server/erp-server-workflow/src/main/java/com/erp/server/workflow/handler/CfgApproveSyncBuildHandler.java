package com.erp.server.workflow.handler;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.nacos.common.utils.StringUtils;
import com.common.business.dto.FindUserDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ThirdpartyPlatformEnum;
import com.erp.model.sys.vo.ThirdUnionDTO;
import com.erp.model.workflow.dto.CfgApproveSyncDTO;
import com.erp.model.workflow.entity.*;
import com.erp.model.workflow.enums.FSApprovalStatusEnum;
import com.erp.model.workflow.enums.FSTaskApprovalStatusEnum;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.workflow.service.*;
import com.lark.oapi.service.approval.v4.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 构建ERP审批同步的请求体
 * @author jack
 * @date 2025-05-21
 */
@Slf4j
@Component
public class CfgApproveSyncBuildHandler {

    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    private CfgApproveSyncService configApproveSyncService;
    @Resource
    private CfgQueryOptionExtService cfgQueryOptionExtService;
    @Resource
    private ApproveSyncRecordService approveSyncRecordService;


    // 定义映射关系
    private static final Map<ApproveStatusEnum, String> STATUS_MAPPING = new HashMap<>();

    static {
        STATUS_MAPPING.put(ApproveStatusEnum.APPROVE_ING, FSTaskApprovalStatusEnum.PENDING.getCode());
        STATUS_MAPPING.put(ApproveStatusEnum.APPROVE, FSTaskApprovalStatusEnum.APPROVED.getCode());
        STATUS_MAPPING.put(ApproveStatusEnum.REJECT, FSTaskApprovalStatusEnum.REJECTED.getCode());
    }


    /**
     * 构建三方审批同步
     * @author jack
     * @date 2025-05-21
     */
    public CreateExternalInstanceReq buildExternalInstanceReq(CfgApproveSyncDTO.SyncFsProcessToMqDTO mqDto,
                                                              ProcessManagementEntity processManagementEntity,
                                                              List<ProcessTaskManagementEntity> processTaskManagementEntities,
                                                              List<ProcessTaskCcEntity> processTaskCcEntities,
                                                              List<CfgApproveSyncFieldMapEntity> fieldMapEntities,
                                                              Map<String, String> remoteValues,
                                                              Map<String, ThirdUnionDTO> thirdUnionMap,
                                                              ApproveSyncRecordEntity syncRecordEntity){
        String errorReason= "";
        //pc地址
        String pcLinkByEnv = cfgSettingService.getPcLinkByEnv();
        //erp审批同步配置表
        CfgApproveSyncEntity cfgApproveSyncEntity = mqDto.getCfgApproveSyncEntity();
        //process_task_management表id
        String processManagementId = mqDto.getProcessManagementId();
        //实例id
        String instanceId = mqDto.getInstanceId();
        //创建人id
        String createUserId = mqDto.getOperator();
        //单据名称
        String businessName = mqDto.getBusinessName();
        //当前流程操作动作
        String approveType = mqDto.getApproveType();
        //国际化文案
        Map<String,String> values = new HashMap<>();
        //标题
        values.put("@i18n@title",cfgApproveSyncEntity.getTitle());

        syncRecordEntity.setReceiverId(createUserId);
        FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(createUserId);
        if(Objects.nonNull(findUserDTO)){
            syncRecordEntity.setReceiverName(findUserDTO.getUserName());
        }

        //查询飞书的用户信息
        String thirdOpenUserId = "";
        String userName;
        ThirdUnionDTO thirdUnionDTO = thirdUnionMap.getOrDefault(createUserId, null);
        if(Objects.isNull(thirdUnionDTO)){
            userName = "";
            errorReason= "创建人未绑定飞书";
            syncRecordEntity.setErrorReason( errorReason);
            approveSyncRecordService.insertBatch(Arrays.asList(syncRecordEntity));
            return null;
        }else{
            syncRecordEntity.setReceiverId(createUserId);
            syncRecordEntity.setReceiverName(thirdUnionDTO.getUserName());
            if(StringUtils.isBlank(thirdUnionDTO.getThirdUserId()) && StringUtils.isBlank(thirdUnionDTO.getThirdOpenId())){
                errorReason= "创建人未绑定飞书";
                syncRecordEntity.setErrorReason( errorReason);
                approveSyncRecordService.insertBatch(Arrays.asList(syncRecordEntity));
                return null;
            }
            thirdOpenUserId = thirdUnionDTO.getThirdOpenId();
            values.put("@i18n@userName", thirdUnionDTO.getUserName());
            userName = thirdUnionDTO.getUserName();
        }

        //获取创建时间以及更新时间
        LocalDateTime createTime = processManagementEntity.getCreateTime();
        LocalDateTime updateTime = processManagementEntity.getUpdateTime();
        // 转换为毫秒时间戳
        String createTimeMillis = String.valueOf(createTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        String updateTimeMillis = String.valueOf(updateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        String endTimeMillis;
        if(!mqDto.getFSApprovalStatusEnum().equals(FSApprovalStatusEnum.PENDING)){
            endTimeMillis = updateTimeMillis;
        } else {
            endTimeMillis = "0";
        }
        ExternalInstance externalInstance = ExternalInstance.newBuilder()
                .approvalCode(cfgApproveSyncEntity.getApprovalCode())
                .status(mqDto.getFSApprovalStatusEnum().getCode())
                .instanceId(processManagementId)
                .links(ExternalInstanceLink.newBuilder()
                        .pcLink(pcLinkByEnv)
                        .mobileLink(pcLinkByEnv)
                        .build())
                .title("@i18n@title")
//                .userId(thirdUserId)
                .userName("@i18n@userName")
                .openId(thirdOpenUserId)
                .startTime(createTimeMillis)//审批发起时间
                .endTime(endTimeMillis) //审批实例结束时间。未结束的审批为 0，Unix 毫秒时间戳。
                .updateTime(updateTimeMillis)//审批实例最近更新时间
                .displayMethod("BROWSER")//列表页打开审批实例的方式。 BROWSER：跳转系统默认浏览器打开, SIDEBAR：飞书中侧边抽屉打开, NORMAL：飞书内嵌页面打开
                .updateMode("UPDATE")//更新方式。 REPLACE：全量替换, UPDATE：增量更新
                .build();

        //任务列表数组  最大长度：300
        if(CollUtil.isEmpty(processTaskManagementEntities)){
            errorReason= "审批任务不能为空";
            syncRecordEntity.setErrorReason( errorReason);
            approveSyncRecordService.insertBatch(Arrays.asList(syncRecordEntity));
            return null;
        }else {
            if(processTaskManagementEntities.size() > 300){
                errorReason= "飞书平台任务列表数不能超过300";
                syncRecordEntity.setErrorReason( errorReason);
                approveSyncRecordService.insertBatch(Arrays.asList(syncRecordEntity));
                return null;
            }

            AtomicReference<Integer> num = new AtomicReference<>(0);
            ExternalInstanceTaskNode[] taskList = processTaskManagementEntities.stream()
                    .filter(e -> isThirdUnionValid(e.getCurApproveId(), thirdUnionMap))
                    .map(e -> {
                                num.updateAndGet(v -> v + 1);
                                Integer i = num.get();
                                String taskTitle = "@i18n@taskTitle" + i;
                                //撤销操作
                                values.put(taskTitle, cfgApproveSyncEntity.getTitle());

                        // 使用映射获取状态码
                        ApproveStatusEnum taskStatus = e.getTaskStatus();
                        String status = STATUS_MAPPING.getOrDefault(taskStatus, "");

                        return ExternalInstanceTaskNode.newBuilder()
                                        .taskId(e.getId())
                                        .openId(thirdUnionMap.get(e.getCurApproveId()).getThirdOpenId())
                                        .title(taskTitle)
                                        .links(ExternalInstanceLink.newBuilder()
                                                .pcLink(pcLinkByEnv)
                                                .mobileLink(pcLinkByEnv)
                                                .build())
                                        .status(status)
                                        .createTime(createTimeMillis)
                                        .endTime(endTimeMillis)
                                        .updateTime(updateTimeMillis)
                                        .actionConfigs(getActionConfigs())
                                        .displayMethod("BROWSER")
                                        .excludeStatistics(false)
                                        .build();
                            }
                    ).toArray(ExternalInstanceTaskNode[]::new);

            externalInstance.setTaskList(taskList);
        }


        //抄送列表数组 最大长度：200
        if(CollUtil.isNotEmpty(processTaskCcEntities)){
            if(processTaskCcEntities.size() > 200){
                errorReason= "飞书平台抄送列表数不能超过200";
                syncRecordEntity.setErrorReason( errorReason);
                approveSyncRecordService.insertBatch(Arrays.asList(syncRecordEntity));
                return null;
            }
            CcNode[] ccList = processTaskCcEntities.stream()
                    .filter(e -> isThirdUnionValid(e.getCcUserId(), thirdUnionMap))
                    .map(e ->
                            CcNode.newBuilder()
                                    .ccId(e.getId())
                                    .openId(thirdUnionMap.get(e.getCcUserId()).getThirdOpenId())
                                    .title(cfgApproveSyncEntity.getTitle())
                                    .links(ExternalInstanceLink.newBuilder()
                                            .pcLink(pcLinkByEnv)
                                            .mobileLink(pcLinkByEnv)
                                            .build())
                                    .readStatus("UNREAD")
                                    .createTime(createTimeMillis)
                                    .updateTime(updateTimeMillis)
                                    .displayMethod("BROWSER")
                                    .build()
                    ).toArray(CcNode[]::new);
            externalInstance.setCcList(ccList);
        }

        //推送消息 (快接审批)
        if(CollUtil.isNotEmpty(fieldMapEntities)){
            //用户提交审批时填写的表单数据,用于所有审批列表中展示。最多展示3个
            int len = fieldMapEntities.size() > 3 ? 3 : fieldMapEntities.size();
            //需要进行值映射
            if(Objects.nonNull(remoteValues) && remoteValues.size() > 0){
                AtomicReference<Integer> num = new AtomicReference<>(0);
                ExternalInstanceForm[] externalInstanceForm = fieldMapEntities.subList(0, len).stream().map(entry -> {
                    num.updateAndGet(v -> v + 1);
                    return ExternalInstanceForm.newBuilder()
                            .name("@i18n@name" + num.get())
                            .value("@i18n@val" + num.get())
                            .build();
                }).toArray(ExternalInstanceForm[]::new);
                num.set(0);
                for (CfgApproveSyncFieldMapEntity entry : fieldMapEntities.subList(0, len)) {
                    num.updateAndGet(v -> v + 1);
                    values.put("@i18n@name"+num.get(),entry.getFieldName());
                    values.put("@i18n@val"+num.get(),remoteValues.getOrDefault(entry.getFieldId(),""));
                }
                externalInstance.setForm(externalInstanceForm);
            }
        }

        //国际化文案数组
        I18nResource[] i18nResources = configApproveSyncService.mapToI18nResouceArray(values);
        externalInstance.setI18nResources(i18nResources);

        // 创建请求对象
        return CreateExternalInstanceReq.newBuilder()
                .externalInstance(externalInstance)
                .build();
    }

    public String getFieldSourceValueStr(String fieldSource, Map<String, Object> variablesMap) {
        Object fieldSourceValue = variablesMap.getOrDefault(fieldSource,null);
        if(Objects.nonNull(fieldSourceValue)){
            return getFieldSourceValueStr(fieldSourceValue);
        }
        return "";
    }

    public  String getFieldSourceValueStr(Object fieldSourceValue) {
        String fieldSourceValueStr = "";
        if (fieldSourceValue != null) {
            if (fieldSourceValue instanceof String) {
                fieldSourceValueStr = (String) fieldSourceValue;
            } else if (fieldSourceValue instanceof Integer) {
                fieldSourceValueStr = String.valueOf(fieldSourceValue);
            } else if (fieldSourceValue instanceof Date) {
                // 假设日期格式为 yyyy-MM-dd HH:mm:ss
                fieldSourceValueStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date) fieldSourceValue);
            } else if (fieldSourceValue instanceof java.time.LocalDate) {
                fieldSourceValueStr = ((java.time.LocalDate) fieldSourceValue).toString();
            } else if (fieldSourceValue instanceof java.time.LocalDateTime) {
                fieldSourceValueStr = ((java.time.LocalDateTime) fieldSourceValue).toString();
            } else if (fieldSourceValue instanceof Boolean) {
                fieldSourceValueStr = String.valueOf(fieldSourceValue);
            } else if (fieldSourceValue instanceof Double || fieldSourceValue instanceof Float || fieldSourceValue instanceof Long) {
                fieldSourceValueStr = String.valueOf(fieldSourceValue);
            } else {
                // 其他类型，尝试直接调用 toString()
                fieldSourceValueStr = fieldSourceValue.toString();
            }
        }
        return fieldSourceValueStr;
    }

    public  ActionConfig[] getActionConfigs() {
        return new ActionConfig[]{
                ActionConfig.newBuilder()
                        .actionType("APPROVE")
                        .isNeedReason(false)
                        .isReasonRequired(false)
                        .isNeedAttachment(false)
                        .build(),
                ActionConfig.newBuilder()
                        .actionType("REJECT")
                        .isNeedReason(true)
                        .isReasonRequired(true)
                        .isNeedAttachment(false)
                        .build()
        };
    }

    public Map<String, ThirdUnionDTO> getThirdUnionDTOMap(List<String> allUserIds) {
        Map<String, ThirdUnionDTO> thirdUnionMap = new HashMap<>();
        if(CollUtil.isNotEmpty(allUserIds)){
            List<ThirdUnionDTO> thirdUnionDTOs = sysUserFeign.getThirdByUserIds(ThirdpartyPlatformEnum.FS.getCode(),allUserIds);
            thirdUnionMap = thirdUnionDTOs.stream().collect(Collectors.toMap(ThirdUnionDTO::getUserId, e -> e));
        }
        return thirdUnionMap;
    }

    public boolean isThirdUnionValid(String userId, Map<String, ThirdUnionDTO> thirdUnionMap) {
        ThirdUnionDTO dto = thirdUnionMap.getOrDefault(userId,null);
        if (null == dto) {
            return false;
        }
        if (StringUtils.isBlank(dto.getThirdUserId()) && StringUtils.isBlank(dto.getThirdOpenId())) {
            return false;
        }
        return true;
    }



    public List<String> getSummaries(List<CfgApproveSyncFieldMapEntity> fieldMapEntities, Map<String, String> remoteValues) {
        int len = fieldMapEntities.size() > 5 ? 5 : fieldMapEntities.size();
        List<String> summaries = new ArrayList<>();
        fieldMapEntities.sort(Comparator.comparingInt(CfgApproveSyncFieldMapEntity::getSort));
        for (CfgApproveSyncFieldMapEntity entry : fieldMapEntities.subList(0, len)) {
            StringBuffer sb = new StringBuffer();
            sb.append(entry.getFieldName());
            sb.append(":");
            sb.append(remoteValues.getOrDefault(entry.getFieldId(),""));
            summaries.add(sb.toString());
        }
        return summaries;
    }


    /**
     * 构建三方审批同步
     * @author jack
     * @date 2025-05-21
     */
    public CheckExternalInstanceReq buildExternalInstanceReq(ProcessManagementEntity processManagementEntity,List<ProcessTaskManagementEntity> processTaskManagementEntities){
        LocalDateTime updateTime = processManagementEntity.getUpdateTime();
        String updateTimeMillis = String.valueOf(updateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());

        ExteranlInstanceCheck exteranlInstanceCheck = ExteranlInstanceCheck.newBuilder()
                .instanceId(processManagementEntity.getId())
                .updateTime(updateTimeMillis)
                .build();

        if(CollUtil.isNotEmpty(processTaskManagementEntities)){
            ExternalInstanceTask[] tasks = processTaskManagementEntities.stream().map(e -> {
                String taskUpdateTimeMillis = String.valueOf(e.getUpdateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                return ExternalInstanceTask.newBuilder()
                        .taskId(e.getId())
                        .updateTime(taskUpdateTimeMillis)
                        .build();
            }).toArray(ExternalInstanceTask[]::new);
            exteranlInstanceCheck.setTasks(tasks);
        }

        return CheckExternalInstanceReq.newBuilder()
                .checkExternalInstanceReqBody(CheckExternalInstanceReqBody.newBuilder()
                        .instances( new ExteranlInstanceCheck[]{exteranlInstanceCheck})
                        .build())
                .build();
    }

}
