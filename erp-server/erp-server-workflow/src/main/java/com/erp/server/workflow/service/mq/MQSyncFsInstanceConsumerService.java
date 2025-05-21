package com.erp.server.workflow.service.mq;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.common.utils.StringUtils;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.constant.ThirdConstants;
import com.common.business.enums.ThirdpartyPlatformEnum;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.sys.vo.ThirdUnionDTO;
import com.erp.model.workflow.dto.CfgApproveSyncDTO;
import com.erp.model.workflow.dto.FsBotParamsDTO;
import com.erp.model.workflow.entity.*;
import com.erp.model.workflow.enums.CfgApproveSyncSyncPlatformEnum;
import com.erp.model.workflow.enums.FSApprovalStatusEnum;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.sdk.fs.enmu.UserIdTypeEnum;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.workflow.service.*;
import com.lark.oapi.service.approval.v4.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
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
    private CfgApproveSyncService configApproveSyncService;
    @Resource
    private CfgApproveSyncFieldMapService cfgApproveSyncFieldMapService;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private ProcessTaskManagementService processTaskManagementService;
    @Resource
    private ProcessTaskCcService processTaskCcService;
    @Resource
    private FsService fsService;
    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    private ProcessTaskManagementExtService processTaskManagementExtService;

    @Override
    public void onMessage(CfgApproveSyncDTO.SyncFsProcessToMqDTO dto) {
        log.info("MQSyncFsInstanceConsumerService 开始");
        CfgApproveSyncEntity cfgApproveSyncEntity = dto.getCfgApproveSyncEntity();
        //任务
        List<ProcessTaskManagementEntity> processTaskManagementEntities = processTaskManagementService.listTask(dto.getInstanceId());
        //抄送任务
        List<String> taskManagementIds = processTaskManagementEntities.stream().map(ProcessTaskManagementEntity::getId).collect(Collectors.toList());
        List<ProcessTaskCcEntity> processTaskCcEntities = processTaskCcService.listTackCc(taskManagementIds);
        //任务列表人员和抄送列表人员飞书信息
        Map<String, ThirdUnionDTO> thirdUnionMap = getThirdUnionDTOMap(processTaskManagementEntities, processTaskCcEntities);
        //推送消息 (快接审批)
        List<CfgApproveSyncFieldMapEntity> fieldMapEntities = cfgApproveSyncFieldMapService.listByMainIds(Arrays.asList(cfgApproveSyncEntity.getId())).stream()
                .filter(e -> e.getIsQuick().equals(Boolean.TRUE))
                .collect(Collectors.toList());
        fieldMapEntities.sort(Comparator.comparingInt(CfgApproveSyncFieldMapEntity::getSort));

        CreateExternalInstanceReq req = buildExternalInstanceReq(dto,processTaskManagementEntities,processTaskCcEntities,fieldMapEntities ,thirdUnionMap);
        System.out.println(JSONUtil.toJsonStr(req.getExternalInstance()));
        CreateExternalInstanceResp resp = fsService.createExternalInstance(req);
        //todo 判断是否成功，无论成功失败都记录推送记录，
        if (!resp.success()) {
            String msg = String.format("code:%s,msg:%s,reqId:%s", resp.getCode(), resp.getMsg(), resp.getRequestId());
            log.error("同步三方审批实例失败>>>>>{}",msg);
        }else {
            //发送消息
            //pc地址
            String pcLinkByEnv = cfgSettingService.getPcLinkByEnv();
            //参数map
            Map<String, Object> variablesMap = dto.getVariablesMap();

            int len = fieldMapEntities.size() > 5 ? 5 : fieldMapEntities.size();
            List<String> summaries = new ArrayList<>();
            for (CfgApproveSyncFieldMapEntity entry : fieldMapEntities.subList(0, len)) {
                StringBuffer sb = new StringBuffer();
                sb.append(entry.getFieldName());
                sb.append(":");
                String fieldSourceValueStr = getFieldSourceValueStr(entry.getFieldSource(), variablesMap);
                if(StringUtils.isNotBlank(fieldSourceValueStr)){
                    sb.append(fieldSourceValueStr);
                }else {
                    if(variablesMap.containsKey("detailList")){
                        List<Object> detailList =( List<Object> ) variablesMap.get("detailList");
                        if(CollUtil.isNotEmpty(detailList)){
                            StringBuffer dsb = new StringBuffer();
                            for (Object object : detailList) {
                                Map<String, Object> map = BeanUtil.beanToMap(object);
                                String str = getFieldSourceValueStr(entry.getFieldSource(), map);
                                if(StringUtils.isNotBlank(str)){
                                    dsb.append(str);
                                    dsb.append(";");
                                }
                            }
                            String fieldSourceDetailValueStr = dsb.toString();
                            if(StringUtils.isNotBlank(fieldSourceDetailValueStr)){
                                sb.append(fieldSourceDetailValueStr);
                            }
                        }
                    }
                }
                summaries.add(sb.toString());
            }

            List<FsBotParamsDTO.SendParamsDTO> sendParams = new ArrayList<>();
            for (ProcessTaskManagementEntity e : processTaskManagementEntities) {
                FsBotParamsDTO.SendParamsDTO params = new FsBotParamsDTO.SendParamsDTO();
                params.setTemplateId(ThirdConstants.TEMPLATE_ID_1008);
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

            if(CollUtil.isNotEmpty(sendParams)){
                for (FsBotParamsDTO.SendParamsDTO sendParam : sendParams) {
                    String messageId = fsService.sendApproveMessage(sendParam);
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
        log.info("MQSyncFsInstanceConsumerService 结束");
    }

    public CreateExternalInstanceReq buildExternalInstanceReq(CfgApproveSyncDTO.SyncFsProcessToMqDTO mqDto,
                                                              List<ProcessTaskManagementEntity> processTaskManagementEntities,
                                                              List<ProcessTaskCcEntity> processTaskCcEntities,
                                                              List<CfgApproveSyncFieldMapEntity> fieldMapEntities,
                                                              Map<String, ThirdUnionDTO> thirdUnionMap){
        //pc地址
        String pcLinkByEnv = cfgSettingService.getPcLinkByEnv();

        //获取（String类型）当前时间戳
        String currentTimeMillis = String.valueOf(System.currentTimeMillis());
        //erp审批同步配置表
        CfgApproveSyncEntity cfgApproveSyncEntity = mqDto.getCfgApproveSyncEntity();
        //process_task_management表id
        String processManagementId = mqDto.getProcessManagementId();
        //任务id
        String taskId = mqDto.getTaskId();
        //实例id
        String instanceId = mqDto.getInstanceId();
        //创建人id
        String createUserId = mqDto.getCreateUserId();
        //单据名称
        String businessName = mqDto.getBusinessName();
        //参数map
        Map<String, Object> variablesMap = mqDto.getVariablesMap();
        //国际化文案
        Map<String,String> values = new HashMap<>();
        //标题
        values.put("@i18n@title",cfgApproveSyncEntity.getTitle());
        values.put("@i18n@approve","同意");
        values.put("@i18n@reject","拒绝");

        //查询飞书的用户信息
        String thirdUserId = "";
        String thirdOpenUserId = "";
        String userName ="";
        List<ThirdUnionDTO> thirdUnionDTOS = sysUserFeign.getThirdUnionIdsByUserIds(ThirdpartyPlatformEnum.FS.getCode(), Arrays.asList(createUserId));
        if(CollUtil.isEmpty(thirdUnionDTOS)){
            //todo 推送记录  失败  创建人未绑定飞书
        }else{
            ThirdUnionDTO thirdUnionDTO = thirdUnionDTOS.get(0);
            if(StringUtils.isBlank(thirdUnionDTO.getThirdUserId()) && StringUtils.isBlank(thirdUnionDTO.getThirdOpenId())){
                //todo 推送记录  失败  创建人未绑定飞书
            }
            thirdUserId = thirdUnionDTO.getThirdUserId();
            thirdOpenUserId = thirdUnionDTO.getThirdOpenId();
            values.put("@i18n@userName", thirdUnionDTO.getUserName());
            userName = thirdUnionDTO.getUserName();
        }

        //任务标题
        String taskTitle = StrUtil.format("审核通知：【{}】提交的审核名称({})抄送给你",userName,businessName);
        values.put("@i18n@taskTitle",taskTitle);

        ExternalInstance externalInstance = ExternalInstance.newBuilder()
                .approvalCode(cfgApproveSyncEntity.getApprovalCode())
                .status(FSApprovalStatusEnum.PENDING.getCode())
                .instanceId(processManagementId)
                .links(ExternalInstanceLink.newBuilder()
                        .pcLink(pcLinkByEnv)
                        .mobileLink(pcLinkByEnv)
                        .build())
                .title("@i18n@title")
                .userId(thirdUserId)
                .userName("@i18n@userName")
                .openId(thirdOpenUserId)
                .startTime(currentTimeMillis)//审批发起时间
                .endTime("0") //审批实例结束时间。未结束的审批为 0，Unix 毫秒时间戳。
                .updateTime(currentTimeMillis)//审批实例最近更新时间
                .displayMethod("BROWSER")//列表页打开审批实例的方式。 BROWSER：跳转系统默认浏览器打开, SIDEBAR：飞书中侧边抽屉打开, NORMAL：飞书内嵌页面打开
                .updateMode("UPDATE")//更新方式。 REPLACE：全量替换, UPDATE：增量更新
                .build();

        //任务列表数组  最大长度：300
        if(CollUtil.isEmpty(processTaskManagementEntities)){
            //todo 推送记录  失败
        }else {
            if(processTaskManagementEntities.size() > 300){
                //todo 推送记录  失败
            }
            ExternalInstanceTaskNode[] taskList = processTaskManagementEntities.stream()
                    .filter(e -> isThirdUnionValid(e.getCurApproveId(), thirdUnionMap))
                    .map(e -> ExternalInstanceTaskNode.newBuilder()
                            .taskId(e.getTaskId())
                            .userId(thirdUnionMap.get(e.getCurApproveId()).getThirdUserId())
                            .openId(thirdUnionMap.get(e.getCurApproveId()).getThirdOpenId())
                            .title("@i18n@taskTitle")
                            .links(ExternalInstanceLink.newBuilder()
                                    .pcLink(pcLinkByEnv)
                                    .mobileLink(pcLinkByEnv)
                                    .build())
                            .status(FSApprovalStatusEnum.PENDING.getCode())
                            .createTime(currentTimeMillis)
                            .endTime("0")
                            .updateTime(currentTimeMillis)
                            .actionConfigs(getActionConfigs())
                            .displayMethod("BROWSER")
                            .excludeStatistics(false)
                            .build()
                    ).toArray(ExternalInstanceTaskNode[]::new);

            externalInstance.setTaskList(taskList);
        }


        //抄送列表数组 最大长度：200
        if(CollUtil.isEmpty(processTaskCcEntities)){
            //todo 推送记录  失败
        }else {
            if(processTaskCcEntities.size() > 200){
                //todo 推送记录  失败
            }
            String ccTitle = StrUtil.format("抄送通知：【{}】提交的审核名称({})抄送给你",userName,businessName);
            CcNode[] ccList = processTaskCcEntities.stream()
                    .filter(e -> isThirdUnionValid(e.getCcUserId(), thirdUnionMap))
                    .map(e ->
                            CcNode.newBuilder()
                                    .ccId(e.getId())
                                    .userId(thirdUnionMap.get(e.getCcUserId()).getThirdUserId())
                                    .openId(thirdUnionMap.get(e.getCcUserId()).getThirdOpenId())
                                    .title(ccTitle)
                                    .links(ExternalInstanceLink.newBuilder()
                                            .pcLink(pcLinkByEnv)
                                            .mobileLink(pcLinkByEnv)
                                            .build())
                                    .readStatus("UNREAD")
                                    .createTime(currentTimeMillis)
                                    .updateTime(currentTimeMillis)
                                    .displayMethod("BROWSER")
                                    .build()
                    ).toArray(CcNode[]::new);
            externalInstance.setCcList(ccList);
        }

        //推送消息 (快接审批)
        if(CollUtil.isNotEmpty(fieldMapEntities)){
            //用户提交审批时填写的表单数据,用于所有审批列表中展示。最多展示3个
            int len = fieldMapEntities.size() > 3 ? 3 : fieldMapEntities.size();
            ExternalInstanceForm[] externalInstanceForm = fieldMapEntities.subList(0, len).stream().map(entry -> ExternalInstanceForm.newBuilder()
                            .name("@i18n@"+entry.getId())
                            .value("@i18n@"+entry.getFieldSource())
                            .build())
                    .toArray(ExternalInstanceForm[]::new);

            for (CfgApproveSyncFieldMapEntity entry : fieldMapEntities.subList(0, len)) {
                values.put("@i18n@"+entry.getId(),entry.getFieldName());

                String fieldSourceValueStr = getFieldSourceValueStr(entry.getFieldSource(), variablesMap);
                if(StringUtils.isNotBlank(fieldSourceValueStr)){
                    values.put("@i18n@"+ entry.getFieldSource(),fieldSourceValueStr);
                }else {
                    if(variablesMap.containsKey("detailList")){
                        List<Object> detailList =( List<Object> ) variablesMap.get("detailList");
                        if(CollUtil.isNotEmpty(detailList)){
                            StringBuffer sb = new StringBuffer();
                            for (Object object : detailList) {
                                Map<String, Object> map = BeanUtil.beanToMap(object);
                                String str = getFieldSourceValueStr(entry.getFieldSource(), map);
                                if(StringUtils.isNotBlank(str)){
                                    sb.append(str);
                                    sb.append(";");
                                }
                            }
                            String fieldSourceDetailValueStr = sb.toString();
                            if(StringUtils.isNotBlank(fieldSourceDetailValueStr)){
                                values.put("@i18n@"+entry.getFieldSource(),fieldSourceDetailValueStr);
                            }
                        }
                    }
                }
            }
            externalInstance.setForm(externalInstanceForm);
        }

        //国际化文案数组
        I18nResource[] i18nResources = configApproveSyncService.mapToI18nResouceArray(values);
        externalInstance.setI18nResources(i18nResources);

        // 创建请求对象
        return CreateExternalInstanceReq.newBuilder()
                .externalInstance(externalInstance)
                .build();
    }

    private String getFieldSourceValueStr(String fieldSource, Map<String, Object> variablesMap) {
        Object fieldSourceValue = variablesMap.getOrDefault(fieldSource,null);
        if(Objects.nonNull(fieldSourceValue)){
            return getFieldSourceValueStr(fieldSourceValue);
        }
        return "";
    }

    private static String getFieldSourceValueStr(Object fieldSourceValue) {
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

    private static ActionConfig[] getActionConfigs() {
        return new ActionConfig[]{
                ActionConfig.newBuilder()
                        .actionType("APPROVE")
                        .actionName("@i18n@approve")
                        .isNeedReason(true)
                        .isReasonRequired(false)
                        .isNeedAttachment(false)
                        .build(),
                ActionConfig.newBuilder()
                        .actionType("REJECT")
                        .actionName("@i18n@reject")
                        .isNeedReason(true)
                        .isReasonRequired(true)
                        .isNeedAttachment(false)
                        .build()
        };
    }

    private Map<String, ThirdUnionDTO> getThirdUnionDTOMap(List<ProcessTaskManagementEntity> processTaskManagementEntities, List<ProcessTaskCcEntity> processTaskCcEntities) {
        List<String> allUserIds = Stream.concat(
                        processTaskManagementEntities.stream()
                                .map(ProcessTaskManagementEntity::getCurApproveId),
                        processTaskCcEntities.stream()
                                .map(ProcessTaskCcEntity::getCcUserId)
                )
                .collect(Collectors.toList());
        List<ThirdUnionDTO> thirdUnionDTOs = sysUserFeign.getThirdUnionIdsByUserIds(ThirdpartyPlatformEnum.FS.getCode(),allUserIds);
        Map<String, ThirdUnionDTO> thirdUnionMap = thirdUnionDTOs.stream().collect(Collectors.toMap(ThirdUnionDTO::getUserId, e -> e));
        return thirdUnionMap;
    }

    private boolean isThirdUnionValid(String userId, Map<String, ThirdUnionDTO> thirdUnionMap) {
        ThirdUnionDTO dto = thirdUnionMap.getOrDefault(userId,null);
        if (null == dto) {
            return false;
        }
        if (StringUtils.isBlank(dto.getThirdUserId()) && StringUtils.isBlank(dto.getThirdOpenId())) {
            return false;
        }
        return true;
    }
}
