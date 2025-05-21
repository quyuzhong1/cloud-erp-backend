package com.erp.server.workflow.handler;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.nacos.common.utils.StringUtils;
import com.common.business.enums.ThirdpartyPlatformEnum;
import com.erp.model.sys.vo.ThirdUnionDTO;
import com.erp.model.workflow.dto.CfgApproveSyncDTO;
import com.erp.model.workflow.entity.*;
import com.erp.model.workflow.enums.FSApprovalStatusEnum;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.workflow.service.CfgApproveSyncService;
import com.erp.server.workflow.service.CfgSettingService;
import com.erp.server.workflow.service.ProcessManagementService;
import com.lark.oapi.service.approval.v4.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private ProcessManagementService processManagementService;

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
                                                              Map<String, ThirdUnionDTO> thirdUnionMap){
        //pc地址
        String pcLinkByEnv = cfgSettingService.getPcLinkByEnv();
        //erp审批同步配置表
        CfgApproveSyncEntity cfgApproveSyncEntity = mqDto.getCfgApproveSyncEntity();
        //process_task_management表id
        String processManagementId = mqDto.getProcessManagementId();
        //任务id
        String taskId = mqDto.getTaskId();
        //实例id
        String instanceId = mqDto.getInstanceId();
        //创建人id
        String createUserId = mqDto.getOperator();
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
        List<ThirdUnionDTO> thirdUnionDTOS = sysUserFeign.getThirdByUserIds(ThirdpartyPlatformEnum.FS.getCode(), Arrays.asList(createUserId));
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

        //获取创建时间以及更新时间
        LocalDateTime createTime = processManagementEntity.getCreateTime();
        LocalDateTime updateTime = processManagementEntity.getUpdateTime();
        // 转换为毫秒时间戳
        String createTimeMillis = String.valueOf(createTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        String updateTimeMillis = String.valueOf(updateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());

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
                .startTime(createTimeMillis)//审批发起时间
                .endTime("0") //审批实例结束时间。未结束的审批为 0，Unix 毫秒时间戳。
                .updateTime(updateTimeMillis)//审批实例最近更新时间
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
                            .createTime(createTimeMillis)
                            .endTime("0")
                            .updateTime(updateTimeMillis)
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



    public List<String> getSummaries(List<CfgApproveSyncFieldMapEntity> fieldMapEntities, Map<String, Object> variablesMap) {
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
        return summaries;
    }

}
