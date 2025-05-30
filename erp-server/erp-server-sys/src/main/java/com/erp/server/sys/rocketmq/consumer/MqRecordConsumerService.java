package com.erp.server.sys.rocketmq.consumer;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.constant.ThirdConstants;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.entity.BaseEntity;
import com.common.core.entity.ConditionElement;
import com.common.core.exception.ServiceException;
import com.common.core.server.rule.SpElServer;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.StrUtils;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.plm.dto.ProductShowDTO;
import com.erp.model.plm.entity.NoticeMessageEntity;
import com.erp.model.plm.entity.PlmCfgSettingEntity;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.entity.TaskFollowerEntity;
import com.erp.model.plm.enums.NoticeItemPeopleEnum;
import com.erp.model.sys.dto.CfgThirdNoticeDTO;
import com.erp.model.sys.dto.MqConsumerRecordDTO;
import com.erp.model.sys.dto.ThirdNoticePushRecordDTO;
import com.erp.model.sys.entity.CfgApproveSyncFieldMapEntity;
import com.erp.model.sys.entity.CfgRuleConditionEntity;
import com.erp.model.sys.entity.CfgThirdNoticeEntity;
import com.erp.model.sys.entity.MqConsumerRecordEntity;
import com.erp.model.sys.enums.CfgThirdNoticeMethodEnum;
import com.erp.model.sys.vo.FsBatchSendMessageDTO;
import com.erp.model.sys.vo.ThirdUnionDTO;
import com.erp.model.wms.dto.pickingstrategy.CfgRuleConditionDTO;
import com.erp.model.wms.entity.CfgRulePickingEntity;
import com.erp.model.workflow.dto.AuditorHandleDTO;
import com.erp.model.workflow.dto.CfgQueryOptionDTO;
import com.erp.model.workflow.dto.ProcessTaskManagementDTO;
import com.erp.model.workflow.entity.CfgQueryOptionEntity;
import com.erp.model.workflow.enums.CfgApproveSyncSyncPlatformEnum;
import com.erp.model.workflow.enums.CfgQueryOptionFieldBelongsTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.ProcessTaskManagementFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.rpc.workflow.feign.CfgQueryOptionFeign;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.sys.service.CfgApproveSyncFieldMapService;
import com.erp.server.sys.service.CfgRuleConditionService;
import com.erp.server.sys.service.CfgThirdNoticeService;
import com.erp.server.sys.service.MqConsumerRecordService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = RocketMqTopic.RECEIVE_DDL_TO_MQ_SYS_TOPIC,
        selectorExpression = "sys_receive_ddl_to_mq_tag",
        consumerGroup = RocketMqConsumerGroup.SYS_RECEIVE_DDL_TO_MQ_CONSUMER)
public class MqRecordConsumerService implements RocketMQListener<MqConsumerRecordDTO.MqDTO> {
    /*
    //------plm------
    新建产品  com.erp.server.plm.service.impl.NoticeMessageServiceImpl.newProductNotice  productInfo
    产品信息变更 com.erp.server.plm.service.impl.ProductDetailServiceImpl.handleProductChangeNotification productDetail
    模具审核 com.erp.server.plm.service.impl.MouldInfoServiceImpl.sendApproveNotice  mouldinfo
    模具提交 com.erp.server.plm.service.impl.MouldInfoServiceImpl.submit
    模具创建 com.erp.server.plm.service.impl.MouldInfoServiceImpl.add
    返还确认 com.erp.server.plm.service.impl.MouldInfoServiceImpl.returnConfirm
    返还达量 com.erp.server.plm.service.impl.MouldRefCalcQtyServiceImpl.calcRefundQty MouldRefCalcQty

    //------wms------
    质检通知 -新品 com.erp.server.wms.service.impl.QcResultServiceImpl.sendQcResultMsg
    质检通知 -老品 com.erp.server.wms.service.impl.QcResultServiceImpl.sendQcResultMsg
    首次质检 -产品尺寸变更 com.erp.server.wms.service.impl.QcResultServiceImpl.sendQcBackFillPackaging
    质检通知 com.erp.server.wms.schedule.CfgSettingJob.fsQcNotice
    仓位补货通知 com.erp.server.wms.schedule.CfgSettingJob.fsWlrNotice
    要货申请待处理 com.erp.server.wms.service.impl.RequisitionApplicationServiceImpl.sendRequisitionMsg FS_REQUISITION_WAITHANDLE_NOTICE
    要货申请处理中 com.erp.server.wms.service.impl.RequisitionApplicationServiceImpl.sendRequisitionMsg FS_REQUISITION_HANDLEING_NOTICE
    要货申请已装箱 com.erp.server.wms.service.impl.RequisitionApplicationServiceImpl.sendRequisitionMsg FS_REQUISITION_PACKING_NOTICE
    要货申请已完成 com.erp.server.wms.service.impl.RequisitionApplicationServiceImpl.sendRequisitionMsg FS_REQUISITION_NOTICE
    头程发货单待处理 com.erp.server.wms.service.impl.FirstMileDeliveryServiceImpl.submit
    要货申请变更单提交 com.erp.server.wms.service.impl.RequisitionApplicationServiceImpl.sendRequisitionMsg FS_REQUISITION_CHANGE_SUBMIT_NOTICE
    要货申请变更单审核 com.erp.server.wms.service.impl.RequisitionApplicationServiceImpl.sendRequisitionMsg FS_REQUISITION_CHANGE_APPROVE_NOTICE
    装箱完成通知 com.erp.server.wms.service.impl.PackingTaskServiceImpl.sendNoticeMsg

    //------mrp------
    确认发货建议 com.erp.server.mrp.schedule.CfgNoticeJob.sendMrpNotice
    生成发货建议 com.erp.server.mrp.schedule.CfgNoticeJob.sendMrpNotice

    //------tms------
    在途异常 com.erp.server.tms.schedule.FmLogisticWarnJob.sendFmLogisticWarnJob
    渠道更换 com.erp.server.tms.service.impl.TmsFirstMileLogisticServiceImpl.updateChannel  / com.erp.server.tms.service.impl.TmsFirstMileLogisticServiceImpl.batchUpdateChannel
    备案通知 com.erp.server.tms.service.impl.ProductRegistrationServiceImpl.sendMsgWhenNotRegistration
    组包预报生成【新增】
    物流单下单成功【新增】
*/


    @Resource
    private MqConsumerRecordService mqConsumerRecordService;

    @Resource
    private CfgQueryOptionFeign cfgQueryOptionFeign;

    @Resource
    private CfgThirdNoticeService cfgThirdNoticeService;

    @Resource
    private CfgApproveSyncFieldMapService cfgApproveSyncFieldMapService;

    @Resource
    private CfgRuleConditionService cfgRuleConditionService;

    @Resource
    private MQProducerService mqProducerService;

    @Resource
    private ProcessTaskManagementFeign processTaskManagementFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private SpElServer spElServer;

    @Resource
    private FsService fsService;

    private String namespace = SpringUtil.getProperty("spring.cloud.nacos.discovery.namespace");

    @Override
    public void onMessage(MqConsumerRecordDTO.MqDTO dto) {
        log.info("MqRecordConsumerService 开始");
        //接收中台发送的ddl变更
        //参数不能为空
        if(StringUtils.isNotBlank(dto.getDb()) && StringUtils.isNotBlank(dto.getTable()) && StringUtils.isNotBlank(dto.getIud()) && Objects.nonNull(dto.getDataJson())){
            //保存mq消费记录
            // 创建 Gson 实例
            Gson gson = new Gson();
            // 将 Map 转换为 JSON 字符串
            MqConsumerRecordEntity mqConsumerRecord = new MqConsumerRecordEntity();
            mqConsumerRecord.setTopic(RocketMqTopic.SEND_THIRD_NOTICE_SYS_TOPIC.replace("${spring.cloud.nacos.discovery.namespace}", namespace));
            mqConsumerRecord.setTag(RocketMqTagEnum.SYS_SEND_THIRD_NOTICE_TAG.name());
            mqConsumerRecord.setConsumerGroup(RocketMqConsumerGroup.SYS_SEND_THIRD_NOTICE_CONSUMER.replace("${spring.cloud.nacos.discovery.namespace}", namespace));
            mqConsumerRecord.setDataJson(gson.toJson(dto.getDataJson()));
            boolean flag = mqConsumerRecordService.save(mqConsumerRecord);
            if(flag){
                String id = mqConsumerRecord.getId();
                //根据参数判断一下通知的单据类型
                CfgQueryOptionDTO.MqParamsDTO mqParamsDTO = new CfgQueryOptionDTO.MqParamsDTO();
                mqParamsDTO.setTableName(dto.getTable());
                mqParamsDTO.setSysClassify(dto.getDb().replace("erp-", ""));
                List<CfgQueryOptionEntity> cfgQueryOptionList = cfgQueryOptionFeign.listByMqParams(mqParamsDTO);
                if(CollUtil.isEmpty(cfgQueryOptionList)){
                    //todo
                    return ;
                }
                CfgQueryOptionEntity cfgQueryOptionEntity = cfgQueryOptionList.get(0);
                //单据类型
                String bussinessKey = cfgQueryOptionEntity.getBussinessKey();

                //获取三方通知配置的信息--单条--即时通知
                List<CfgThirdNoticeEntity> cfgThirdNoticeList = cfgThirdNoticeService.lambdaQuery()
                        .eq(CfgThirdNoticeEntity::getBusinessType, bussinessKey)
                        .eq(CfgThirdNoticeEntity::getMethod, CfgThirdNoticeMethodEnum.SINGLE.getCode())
                        .eq(CfgThirdNoticeEntity::getNoticeStatus, Boolean.TRUE)
                        .list();
                if(CollUtil.isEmpty(cfgThirdNoticeList)){
                    return ;
                }

                List<String> cfgThirdNoticeIdList = cfgThirdNoticeList.stream().map(CfgThirdNoticeEntity::getId).collect(Collectors.toList());
                //三方通知配置--推送消息
                List<CfgApproveSyncFieldMapEntity> fieldMapList = cfgApproveSyncFieldMapService.lambdaQuery().in(CfgApproveSyncFieldMapEntity::getMainId, cfgThirdNoticeIdList).list();
                Map<String, List<CfgApproveSyncFieldMapEntity>> fieldMap = fieldMapList.stream().collect(Collectors.groupingBy(CfgApproveSyncFieldMapEntity::getMainId));
                //三方通知配置--规则条件
                List<CfgRuleConditionEntity> ruleConditionList = cfgRuleConditionService.lambdaQuery().in(CfgRuleConditionEntity::getRuleId, cfgThirdNoticeIdList).list();
                Map<String, List<CfgRuleConditionEntity>> ruleConditionMap = ruleConditionList.stream().collect(Collectors.groupingBy(CfgRuleConditionEntity::getRuleId));

                Map<String, Object> dataJson = dto.getDataJson();
                for (CfgThirdNoticeEntity noticeEntity : cfgThirdNoticeList) {
                    String roleType = noticeEntity.getRoleType();
                    String specificPerson = noticeEntity.getSpecificPerson();
                    if(StringUtils.isBlank(roleType) && StringUtils.isBlank(specificPerson)){
                        continue;
                    }
                    String createUserId = String.valueOf(dataJson.getOrDefault("createUserId", ""));
                    String businessId = String.valueOf(dataJson.getOrDefault("id", ""));
                    List<String> userIdList = getSetNotice(roleType, specificPerson, businessId, createUserId);
                    if(CollUtil.isEmpty(userIdList)){
                        continue;
                    }

                    //根据通知方式查找人员 目前只有飞书
                    List<ThirdUnionDTO> unionList = new ArrayList<>();
                    String noticeMethod = noticeEntity.getNoticeMethod();
                    if(StringUtils.isNotBlank(noticeMethod)){
                        List<String> noticeMethodList = Arrays.asList(noticeMethod.split(","));
                        for (String str : noticeMethodList) {
                            //获取飞书的unionid 与用户关系
                            if(CfgApproveSyncSyncPlatformEnum.FEISHU.getCode().equals(str)){
                                unionList = sysUserFeign.getThirdByUserIds(ThirdConstants.FS_PLATFORM,userIdList);
                            }
                        }
                    }

                    if(CollUtil.isEmpty(unionList)){
                        continue;
                    }

                    //校验规则条件
                    List<CfgRuleConditionEntity> cfgRuleConditionEntities = ruleConditionMap.get(noticeEntity.getId());
                    if(CollUtil.isNotEmpty(cfgRuleConditionEntities)){
                        //封装条件参数
                        Map<String, Object> map = new HashMap<>();
                        for (CfgRuleConditionEntity cfgRuleConditionEntity : cfgRuleConditionEntities) {
                            map.put(cfgRuleConditionEntity.getField(), dataJson.get(cfgRuleConditionEntity.getField()));
                        }
                        // 获取所有符合条件的规则
                        List<CfgRuleConditionEntity> conditionList = cfgRuleConditionEntities.stream()
                                .sorted(Comparator.comparing(CfgRuleConditionEntity::getIndex))
                                .collect(Collectors.toList());
                        List<ConditionElement> conditionElementList = BeanMapper.copyList(conditionList, ConditionElement.class);
                        //获取到表达式,判断表达式是否匹配
                        Boolean match = spElServer.matchExpressionByConditionList(conditionElementList, map);
                        if(match){
                            //组装推送消息请求体
                            List<CfgApproveSyncFieldMapEntity> fieldList = fieldMap.get(noticeEntity.getId()).stream()
                                    .sorted(Comparator.comparing(CfgApproveSyncFieldMapEntity::getSort))
                                    .collect(Collectors.toList());

                            List<String> fieldId = fieldList.stream().map(CfgApproveSyncFieldMapEntity::getFieldId).collect(Collectors.toList());
                            Map<String, CfgQueryOptionEntity> cfgQueryOptionMap = cfgQueryOptionList.stream().collect(Collectors.toMap(CfgQueryOptionEntity::getId, e -> e));

                            CfgQueryOptionEntity cfgQueryOptionDetail = cfgQueryOptionList.stream().filter( e ->fieldId.contains(e.getId()) && e.getFieldBelongsType().equals(CfgQueryOptionFieldBelongsTypeEnum.DETAIL.getCode())).findFirst().orElse(null);

                            List<BaseEntity> detailList = new ArrayList<>();
                            if(Objects.nonNull(cfgQueryOptionDetail)){//存在明细表，则需要查出来

                                CfgQueryOptionEntity mainIdEntity = cfgQueryOptionList.stream().filter(e -> StringUtils.isNotBlank(e.getParentId())).findFirst().orElse(null);
                                if(Objects.isNull(mainIdEntity)){
                                    break;
                                }
                                String classpath = cfgQueryOptionDetail.getClasspath();
                                classpath = classpath.replace("class ", "");
                                Class<BaseEntity> clazz = null;
                                try {
                                    clazz = (Class<BaseEntity>) Class.forName(classpath);
                                } catch (ClassNotFoundException e) {
                                    throw new ServiceException(classpath + "实体不存在");
                                }
                                String conditionField = StrUtils.underlineByhump(mainIdEntity.getConditionField());
                                detailList = FeignQuery.create(clazz)
                                        .eq(conditionField, businessId)
                                        .list();

                                if(CollUtil.isEmpty(detailList)){
                                    break;
                                }
                            }
                            FsBatchSendMessageDTO sendMessage = new FsBatchSendMessageDTO();
                            List<String> unionIds = unionList.stream().map(ThirdUnionDTO::getThirdUnionId).distinct().collect(Collectors.toList());
                            sendMessage.setUnionIds(unionIds);
                            //通知标题
                            String title = noticeEntity.getTitle();
                            //通知主题
                            StringBuffer sb = new StringBuffer();
                            String noticeType = noticeEntity.getNoticeType();
                            sb.append("通知类型：");
                            sb.append(noticeType);
                            sb.append("\n");
                            for (CfgApproveSyncFieldMapEntity entity : fieldList) {
                                sb.append(entity.getFieldName());
                                sb.append("：");

                                CfgQueryOptionEntity opt = cfgQueryOptionMap.getOrDefault(entity.getFieldId(),null);
                                if(Objects.isNull(opt)){
                                    sb.append(dataJson.getOrDefault(entity.getFieldSource(), ""));
                                }else {
                                    String fieldBelongsType = opt.getFieldBelongsType();
                                    //属于明细表
                                    if(fieldBelongsType.equals(CfgQueryOptionFieldBelongsTypeEnum.DETAIL.getCode())){
                                        StringBuffer detailSb = new StringBuffer();
                                        for (BaseEntity baseEntity : detailList) {
                                            Map<String, Object> detailMap = gson.fromJson(gson.toJson(baseEntity), new TypeToken<Map<String, Object>>(){}.getType());
                                            detailSb.append(detailMap.getOrDefault(entity.getFieldSource(),""));
                                            detailSb.append("；");
                                        }
                                        sb.append(detailSb.toString());
                                    }else {
                                        sb.append(dataJson.getOrDefault(entity.getFieldSource(), ""));
                                    }
                                }
                                sb.append("\n");
                            }
                            String content =sb.toString();

                            //跳转URL
                            String url =noticeEntity.getUrl();

                            Map<String,Object> contentMap = fsService.getCardMessageMap(title, content,url);
                            sendMessage.setContentMap(contentMap);

                            mqProducerService.asyncClassMsgByDelayLevel(RocketMqTopic.SEND_THIRD_NOTICE_SYS_TOPIC, RocketMqTagEnum.SYS_SEND_THIRD_NOTICE_TAG.getName(),sendMessage , IdUtil.simpleUUID(),1);
                        }
                    }
                 }
            }
        }
        log.info("MqRecordConsumerService 结束");
    }


    /**
     * 获取系统设置的通知人员
     * @param
     * @return java.util.List<java.lang.String>
     * @author jack
     * @date 2025-05-30
     */
    private List<String> getSetNotice(String roleType,
                                      String specificPerson,
                                      String businessId,
                                      String createUserId) {
        List<String> resultList = new ArrayList<>();

        //具体人员
        if(StringUtils.isNotBlank(specificPerson)){
            List<String> otherPeopleIds = Arrays.asList(specificPerson.split(","));
            resultList.addAll(otherPeopleIds);
        }

        if(StringUtils.isNotBlank(roleType)){
            List<String> itemPeopleList = Arrays.asList(roleType.split(","));
            //创建人
            if (itemPeopleList.contains(NoticeItemPeopleEnum.CREATOR.getFlag()) && StringUtils.isNotBlank(createUserId)){
                resultList.add(createUserId);
            }
            //这个是审核人
            if (itemPeopleList.contains(NoticeItemPeopleEnum.AUDITOR.getFlag())) {
                List<ProcessTaskManagementDTO.ApproveHistoryDTO> approveHistoryList = processTaskManagementFeign.listApproveHistory(businessId);
                List<String> collect = approveHistoryList.stream().filter(v -> v.getApproveStatus().equals(ApproveStatusEnum.APPROVE_ING.getStatus())).map(ProcessTaskManagementDTO.ApproveHistoryDTO::getCurApproveId).collect(Collectors.toList());
                resultList.addAll(collect);
            }
        }
        return resultList;
    }
    /**
     * 获取系统设置的通知人员
     * @param
     * @return java.util.List<java.lang.String>
     * @author jack
     * @date 2025-05-30
     */
    private List<String> getSetProductNotice(String roleType,
                                      String specificPerson,
                                      String projectChargeId ,
                                      String productChargeId,
                                      List<String> taskIdList,
                                      String businessId,
                                      String createUserId) {
        List<String> resultList = new ArrayList<>();

        //具体人员
        if(StringUtils.isNotBlank(specificPerson)){
            List<String> otherPeopleIds = Arrays.asList(specificPerson.split(","));
            resultList.addAll(otherPeopleIds);
        }

        if(StringUtils.isNotBlank(roleType)){
            List<String> itemPeopleList = Arrays.asList(roleType.split(","));

            //这个是项目经理
            if (itemPeopleList.contains(NoticeItemPeopleEnum.ITEM_MANAGER.getFlag()) && StringUtils.isNotBlank(projectChargeId)) {
                //项目负责人
                List<String> projectChargeIdList = Arrays.asList(projectChargeId.split(","));
                resultList.addAll(projectChargeIdList);
            }

            //这个是产品经理
            if (itemPeopleList.contains(NoticeItemPeopleEnum.PRODUCT_MANAGER.getFlag()) && StringUtils.isNotBlank(productChargeId)) {
                List<String> productChargeIdList = Arrays.asList(productChargeId.split(","));
                resultList.addAll(productChargeIdList);
            }

            //这个是关注人
            if (itemPeopleList.contains(NoticeItemPeopleEnum.FOLLOWER.getFlag()) && CollectionUtils.isNotEmpty(taskIdList)) {
                List<TaskFollowerEntity> taskConcernEntities = plmTaskFeign.listTaskFollowerByTaskIds(taskIdList);
                if (CollectionUtils.isNotEmpty(taskConcernEntities)) {
                    List<String> userIdList = taskConcernEntities.stream().map(TaskFollowerEntity::getUserId).distinct().collect(Collectors.toList());
                    resultList.addAll(userIdList);
                }
            }

            //这个是审核人
            if (itemPeopleList.contains(NoticeItemPeopleEnum.AUDITOR.getFlag()) && CollectionUtils.isNotEmpty(taskIdList)) {
                List<ProjectTaskEntity> taskEntityList = plmTaskFeign.listProjectTaskByTaskIds(taskIdList);
                if (CollectionUtils.isNotEmpty(taskEntityList)) {
                    for (ProjectTaskEntity taskEntity : taskEntityList) {
                        List<AuditorHandleDTO> historyTaskByProcessId = workflowFeign.getHistoryTaskByProcessId(taskEntity.getProcessId());
                        List<String> userIdList = historyTaskByProcessId.stream().map(AuditorHandleDTO::getHandleUserId).distinct().collect(Collectors.toList());
                        resultList.addAll(userIdList);
                    }
                }
            }


            //店铺负责人
            if (itemPeopleList.contains(NoticeItemPeopleEnum.SHOP_CHARGE.getFlag())){

            }
        }
        return resultList;
    }
}
