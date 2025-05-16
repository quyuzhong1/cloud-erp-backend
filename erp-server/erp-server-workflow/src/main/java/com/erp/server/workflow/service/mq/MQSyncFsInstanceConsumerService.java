package com.erp.server.workflow.service.mq;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.nacos.common.utils.StringUtils;
import com.common.business.enums.ThirdpartyPlatformEnum;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.dmp.entity.DmpPushMsgEntity;
import com.erp.model.sys.vo.ThirdUnionDTO;
import com.erp.model.workflow.dto.CfgApproveSyncDTO;
import com.erp.model.workflow.entity.CfgApproveSyncEntity;
import com.erp.model.workflow.entity.CfgApproveSyncFieldMapEntity;
import com.erp.model.workflow.entity.ProcessTaskManagementEntity;
import com.erp.model.workflow.enums.FSApprovalStatusEnum;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.workflow.service.CfgApproveSyncFieldMapService;
import com.erp.server.workflow.service.CfgApproveSyncService;
import com.erp.server.workflow.service.ProcessTaskCcService;
import com.erp.server.workflow.service.ProcessTaskManagementService;
import com.lark.oapi.service.approval.v4.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

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

    @Override
    public void onMessage(CfgApproveSyncDTO.SyncFsProcessToMqDTO dto) {
        log.info("MQSyncFsInstanceConsumerService 开始");
//        buildExternalInstanceReq(dto);
        log.info("MQSyncFsInstanceConsumerService 结束");
    }

    public CreateExternalInstanceReq buildExternalInstanceReq(CfgApproveSyncDTO.SyncFsProcessToMqDTO mqDto){

        CfgApproveSyncEntity cfgApproveSyncEntity = mqDto.getCfgApproveSyncEntity();

        String processManagementId = mqDto.getProcessManagementId();

        //
        String taskId = mqDto.getTaskId();

        String createUserId = mqDto.getCreateUserId();
        //国际化文案
        Map<String,String> values = new HashMap<>();
        //标题
        values.put("@i18n@title",cfgApproveSyncEntity.getTitle());

        //查询飞书的用户信息
        String thirdUserId = "";
        String thirdOpenUserId = "";
        List<ThirdUnionDTO> thirdUnionDTOS = sysUserFeign.getThirdUnionIdsByUserIds(ThirdpartyPlatformEnum.FS.getCode(), Arrays.asList(createUserId));
        if(CollUtil.isEmpty(thirdUnionDTOS)){
            //todo 推送记录 创建人为绑定飞书
        }else{
            ThirdUnionDTO thirdUnionDTO = thirdUnionDTOS.get(0);
            if(StringUtils.isBlank(thirdUnionDTO.getThirdUserId()) && StringUtils.isBlank(thirdUnionDTO.getThirdOpenUserId())){
                //todo 推送记录 创建人为绑定飞书
            }
            thirdUserId = thirdUnionDTO.getThirdUserId();
            thirdOpenUserId = thirdUnionDTO.getThirdOpenUserId();
            values.put("@i18n@userName", thirdUnionDTO.getUserName());
        }

        //任务列表数组
        List<ProcessTaskManagementEntity> processTaskManagementEntity = processTaskManagementService.listTask(processManagementId);

        //抄送列表数组
        I18nResource[] i18nResources = configApproveSyncService.mapToI18nResouceArray(values);

        //获取（String类型）当前时间戳
        String currentTimeMillis = String.valueOf(System.currentTimeMillis());
        ExternalInstance externalInstance = ExternalInstance.newBuilder()
                .approvalCode(cfgApproveSyncEntity.getApprovalCode())
                .status(FSApprovalStatusEnum.PENDING.getCode())
                .instanceId(processManagementId)
                .links(ExternalInstanceLink.newBuilder()
                        .pcLink("")
                        .mobileLink("")
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
                .taskList(new ExternalInstanceTaskNode[]{
                        ExternalInstanceTaskNode.newBuilder()
                                .taskId("112534")
                                .userId("a987sf9s")
                                .openId("ou_be73cbc0ee35eb6ca54e9e7cc14998c1")
                                .title("@i18n@4")
                                .links(ExternalInstanceLink.newBuilder()
                                        .pcLink("https://applink.feishu.cn/client/mini_program/open?mode=appCenter&appId=cli_9c90fc38e07a9101&path=pc/pages/detail?id=1234")
                                        .mobileLink("https://applink.feishu.cn/client/mini_program/open?appId=cli_9c90fc38e07a9101&path=pages/detail?id=1234")
                                        .build())
                                .status("PENDING")
                                .extra("{\"complete_reason\":\"approved\",\"xxx\":\"xxx\"}")
                                .createTime("1556468012678")
                                .endTime("1556468012678")
                                .updateTime("1556468012678")
                                .actionContext("123456")
                                .actionConfigs(new ActionConfig[]{
                                        ActionConfig.newBuilder()
                                                .actionType("APPROVE")
                                                .actionName("@i18n@5")
                                                .isNeedReason(false)
                                                .isReasonRequired(false)
                                                .isNeedAttachment(false)
                                                .build()
                                })
                                .displayMethod("BROWSER")
                                .excludeStatistics(false)
                                .nodeId("node")
                                .nodeName("i18n@name")
                                .build()
                })
                .ccList(new CcNode[]{
                        CcNode.newBuilder()
                                .ccId("123456")
                                .userId("12345")
                                .openId("ou_be73cbc0ee35eb6ca54e9e7cc14998c1")
                                .links(ExternalInstanceLink.newBuilder()
                                        .pcLink("https://applink.feishu.cn/client/mini_program/open?mode=appCenter&appId=cli_9c90fc38e07a9101&path=pc/pages/detail?id=1234")
                                        .mobileLink("https://applink.feishu.cn/client/mini_program/open?appId=cli_9c90fc38e07a9101&path=pages/detail?id=1234")
                                        .build())
                                .readStatus("READ")
                                .extra("{\"xxx\":\"xxx\"}")
                                .title("xxx")
                                .createTime("1556468012678")
                                .updateTime("1556468012678")
                                .displayMethod("BROWSER")
                                .build()
                })
                .i18nResources(i18nResources)
                .build();


        //推送消息
        List<CfgApproveSyncFieldMapEntity> fieldMapEntities = cfgApproveSyncFieldMapService.listByMainIds(Arrays.asList(cfgApproveSyncEntity.getId()));
        fieldMapEntities.sort(Comparator.comparingInt(CfgApproveSyncFieldMapEntity::getSort));
        if(CollUtil.isNotEmpty(fieldMapEntities)){
            //用户提交审批时填写的表单数据,用于所有审批列表中展示。最多展示3个
            int len = fieldMapEntities.size() > 3 ? 3 : fieldMapEntities.size();
            ExternalInstanceForm[] externalInstanceForm = fieldMapEntities.subList(0, len).stream().map(entry -> ExternalInstanceForm.newBuilder()
                            .name("@i18n@"+entry.getFieldName())
                            .value("@i18n@"+entry.getFieldSource())
                            .build())
                    .toArray(ExternalInstanceForm[]::new);
            externalInstance.setForm(externalInstanceForm);
        }

        // 创建请求对象
        return CreateExternalInstanceReq.newBuilder()
                .externalInstance(externalInstance)
                .build();
    }
}
