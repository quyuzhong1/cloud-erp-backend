package com.erp.server.workflow.service.mq;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.nacos.common.utils.StringUtils;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.enums.ThirdpartyPlatformEnum;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.dmp.entity.DmpPushMsgEntity;
import com.erp.model.plm.entity.PlmCfgSettingEntity;
import com.erp.model.sys.vo.ThirdUnionDTO;
import com.erp.model.workflow.dto.CfgApproveSyncDTO;
import com.erp.model.workflow.entity.*;
import com.erp.model.workflow.enums.FSApprovalStatusEnum;
import com.erp.rpc.sys.feign.SysUserFeign;
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

    @Override
    public void onMessage(CfgApproveSyncDTO.SyncFsProcessToMqDTO dto) {
        log.info("MQSyncFsInstanceConsumerService 开始");
        CreateExternalInstanceReq req = buildExternalInstanceReq(dto);
//
//        CreateExternalInstanceResp resp = fsService.createExternalInstance(req);
//        //todo 判断是否成功，五无论成功失败都记录推送记录，
//        if (!resp.success()) {
//            String msg = String.format("code:%s,msg:%s,reqId:%s", resp.getCode(), resp.getMsg(), resp.getRequestId());
//            log.error("同步三方审批实例失败>>>>>{}",msg);
//        }

        log.info("MQSyncFsInstanceConsumerService 结束");
    }

    public CreateExternalInstanceReq buildExternalInstanceReq(CfgApproveSyncDTO.SyncFsProcessToMqDTO mqDto){
        //pc地址
        String pcLinkByEnv = getPcLinkByEnv();

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
            if(StringUtils.isBlank(thirdUnionDTO.getThirdUserId()) && StringUtils.isBlank(thirdUnionDTO.getThirdOpenUserId())){
                //todo 推送记录  失败  创建人未绑定飞书
            }
            thirdUserId = thirdUnionDTO.getThirdUserId();
            thirdOpenUserId = thirdUnionDTO.getThirdOpenUserId();
            values.put("@i18n@userName", thirdUnionDTO.getUserName());
            userName = thirdUnionDTO.getUserName();
        }

        //任务标题
        String taskTitle = StrUtil.format("审核通知：【{}】提交的【审核名称({})抄送给你",userName,businessName);
        values.put("@i18n@taskTitle",taskTitle);

        //任务
        List<ProcessTaskManagementEntity> processTaskManagementEntities = processTaskManagementService.listTask(processManagementId);
        //抄送任务
        List<String> taskManagementIds = processTaskManagementEntities.stream().map(ProcessTaskManagementEntity::getId).collect(Collectors.toList());
        List<ProcessTaskCcEntity> processTaskCcEntities = processTaskCcService.listTackCc(taskManagementIds);
        //任务列表人员和抄送列表人员飞书信息
        Map<String, ThirdUnionDTO> thirdUnionMap = getThirdUnionDTOMap(processTaskManagementEntities, processTaskCcEntities);

        //任务列表数组  最大长度：300
        ExternalInstanceTaskNode[] taskList = processTaskManagementEntities.stream()
                .filter(e -> isThirdUnionValid(e.getCurApproveId(), thirdUnionMap))
                .map(e -> ExternalInstanceTaskNode.newBuilder()
                        .taskId(e.getTaskId())
                        .userId(thirdUnionMap.get(e.getCurApproveId()).getThirdUserId())
                        .openId(thirdUnionMap.get(e.getCurApproveId()).getThirdOpenUserId())
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

        //抄送列表数组 最大长度：200
        String ccTitle = StrUtil.format("抄送通知：【{}】提交的【审核名称({})抄送给你",userName,businessName);
        CcNode[] ccList = processTaskCcEntities.stream()
                .filter(e -> isThirdUnionValid(e.getCcUserId(), thirdUnionMap))
                .map(e ->
                        CcNode.newBuilder()
                                .ccId(e.getId())
                                .userId(thirdUnionMap.get(e.getCcUserId()).getThirdUserId())
                                .openId(thirdUnionMap.get(e.getCcUserId()).getThirdOpenUserId())
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

        //国际化文案数组
        I18nResource[] i18nResources = configApproveSyncService.mapToI18nResouceArray(values);

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
                .taskList(taskList)
                .ccList(ccList)
                .i18nResources(i18nResources)
                .build();


        //推送消息
        List<CfgApproveSyncFieldMapEntity> fieldMapEntities = cfgApproveSyncFieldMapService.listByMainIds(Arrays.asList(cfgApproveSyncEntity.getId()));
        fieldMapEntities.sort(Comparator.comparingInt(CfgApproveSyncFieldMapEntity::getSort));
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

                Object fieldSourceValue = variablesMap.getOrDefault(entry.getFieldSource(),null);
                if(Objects.nonNull(fieldSourceValue)){
                    String fieldSourceValueStr = getFieldSourceValueStr(fieldSourceValue);
                    values.put("@i18n@"+entry.getFieldSource(),fieldSourceValueStr);
                }
            }

            externalInstance.setForm(externalInstanceForm);
        }

        // 创建请求对象
        return CreateExternalInstanceReq.newBuilder()
                .externalInstance(externalInstance)
                .build();
    }

    //根据环境配置返回不同的PC链接
    private String getPcLinkByEnv() {
        //初始化消息发送的URL
        String url ="";
        //根据不同的环境选择对应的URL
        CfgSettingEntity cfgSetting =  cfgSettingService.lambdaQuery().eq(CfgSettingEntity::getKey, "envUrl").one();
        if(null != cfgSetting){
            Map<String, Object> dataJson = cfgSetting.getDataJson();
            boolean uat = BusinessCommonConstants.hasProfile("uat");
            boolean dev = BusinessCommonConstants.hasProfile("dev");
            boolean test = BusinessCommonConstants.hasProfile("test");
            boolean prod = BusinessCommonConstants.hasProfile("prod");
            if(uat){
                url = String.valueOf(dataJson.get("uat"));
            }else  if(dev||test){
                url = String.valueOf(dataJson.get("test"));
            }else if(prod){
                url = String.valueOf(dataJson.get("prod"));
            }
        }
        return url;
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
        if (StringUtils.isBlank(dto.getThirdUserId()) && StringUtils.isBlank(dto.getThirdOpenUserId())) {
            return false;
        }
        return true;
    }
}
