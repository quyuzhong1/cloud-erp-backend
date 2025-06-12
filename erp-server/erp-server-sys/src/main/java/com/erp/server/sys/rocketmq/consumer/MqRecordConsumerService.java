package com.erp.server.sys.rocketmq.consumer;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.enums.ThirdpartyPlatformEnum;
import com.common.business.validator.ValidList;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.entity.BaseEntity;
import com.common.core.entity.ConditionElement;
import com.common.core.exception.ServiceException;
import com.common.core.server.rule.SpElServer;
import com.common.core.utils.BeanMapper;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.sys.dto.MqConsumerRecordDTO;
import com.erp.model.sys.entity.*;
import com.erp.model.sys.enums.*;
import com.erp.model.sys.vo.SendThirdNoticeConsumerDTO;
import com.erp.model.sys.vo.ThirdUnionDTO;
import com.erp.model.wms.dto.WarehouseLocationDTO;
import com.erp.model.workflow.dto.CfgQueryOptionDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.entity.CfgQueryOptionEntity;
import com.erp.model.workflow.enums.CfgApproveSyncSyncPlatformEnum;
import com.erp.model.workflow.enums.CfgQueryOptionFieldBelongsTypeEnum;
import com.erp.rpc.sys.feign.SysPostFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.rpc.workflow.feign.CfgQueryOptionFeign;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.sys.service.*;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
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
public class MqRecordConsumerService implements RocketMQListener<String> {
    /*
    //------plm------
    新建产品  com.erp.server.plm.service.impl.NoticeMessageServiceImpl.newProductNotice  productInfo  product_info
    产品信息变更 com.erp.server.plm.service.impl.ProductDetailServiceImpl.handleProductChangeNotification product_detail
    模具审核 com.erp.server.plm.service.impl.MouldInfoServiceImpl.sendApproveNotice  mould_info
    模具提交 com.erp.server.plm.service.impl.MouldInfoServiceImpl.submit
    模具创建 com.erp.server.plm.service.impl.MouldInfoServiceImpl.add
    返还确认 com.erp.server.plm.service.impl.MouldInfoServiceImpl.returnConfirm
    返还达量 com.erp.server.plm.service.impl.MouldRefCalcQtyServiceImpl.calcRefundQty MouldRefCalcQty

    //------wms------
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
    渠道更换 com.erp.server.tms.service.impl.TmsFirstMileLogisticServiceImpl.updateChannel  / com.erp.server.tms.service.impl.TmsFirstMileLogisticServiceImpl.batchUpdateChannel

    组包预报生成【新增】  --组包称重
    物流单下单成功【新增】 -- B2C销售订单跟踪号
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
    private SysUserFeign sysUserFeign;

    @Resource
    private SpElServer spElServer;

    @Resource
    private FsService fsService;
    @Resource
    private ThirdNoticePushRecordService thirdNoticePushRecordService;

    @Resource
    private SysPostFeign sysPostFeign;

    @Resource
    private DictNoticeRoleOptionService dictNoticeRoleOptionService;
    @Resource
    private WorkflowFeign workflowFeign;

    private String namespace = SpringUtil.getProperty("spring.cloud.nacos.discovery.namespace");

    @Override
//    @Transactional(rollbackFor = Exception.class)
    public void onMessage(String jsonStr) {
        log.info("MqRecordConsumerService 开始");
        if(StringUtils.isBlank(jsonStr)){
            return ;
        }

        MqConsumerRecordDTO.MqDTO dto = new MqConsumerRecordDTO.MqDTO();
        // 创建 Gson 实例
        Gson gson = new Gson();
        Map<String, Object> jsonMap = gson.fromJson(jsonStr, Map.class);
        dto.setDb(jsonMap.get("db") == null ? "" : String.valueOf(jsonMap.get("db")));
        dto.setTable(jsonMap.get("table") == null ? "" : String.valueOf(jsonMap.get("table")));
        dto.setOperationType(jsonMap.get("P_TAG_IUD") == null ? "" : String.valueOf(jsonMap.get("P_TAG_IUD")));
//        dto.setDataJson((Map<String, Object> )jsonMap.get("dataJson"));
        //需要把每个字段都转出驼峰
        Map<String, Object> convertedMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
            String originalKey = entry.getKey();
            Object value = entry.getValue();
            // 转换为驼峰命名
            String camelCaseKey = CharSequenceUtil.toCamelCase(originalKey);
            convertedMap.put(camelCaseKey, value);
        }
        dto.setDataJson(convertedMap);

        //接收中台发送的ddl变更
        //参数不能为空
        if (StringUtils.isNotBlank(dto.getDb()) && StringUtils.isNotBlank(dto.getTable()) && StringUtils.isNotBlank(dto.getOperationType()) && Objects.nonNull(dto.getDataJson())) {

            //保存mq消费记录
            // 将 Map 转换为 JSON 字符串
            String id = addMqRecord( dto);
            sendMsg(dto);
//            if (StringUtils.isNotBlank(id)) {
//                // 异步执行 sendMsg，不阻塞当前事务
//                new Thread(() -> {
//                    try {
//                        sendMsg(dto);
//                    } catch (Exception e) {
//                        log.error("sendMsg 异常", e);
//                    }
//                }).start();
//            }
        }
        log.info("MqRecordConsumerService 结束");
    }

    private boolean sendMsg(MqConsumerRecordDTO.MqDTO dto) {
        //根据参数判断一下通知的单据类型
        CfgQueryOptionDTO.MqParamsDTO mqParamsDTO = new CfgQueryOptionDTO.MqParamsDTO();
        mqParamsDTO.setTableName(dto.getTable());
        mqParamsDTO.setSysClassify(dto.getDb().replace("erp-", ""));
        //查询出common 、表头、明细的配置
        List<CfgQueryOptionEntity> cfgQueryOptionList = cfgQueryOptionFeign.listByMqParams(mqParamsDTO);
        if (CollUtil.isEmpty(cfgQueryOptionList)) {
            return true;
        }
        CfgQueryOptionEntity cfgQueryOptionEntity = cfgQueryOptionList.stream().filter(e -> StringUtils.isNotBlank(e.getBussinessKey())).findFirst().orElse(null);
        //单据类型
        String bussinessKey = cfgQueryOptionEntity.getBussinessKey();

        //获取三方通知配置的信息--单条--即时通知
        List<CfgThirdNoticeEntity> cfgThirdNoticeList = cfgThirdNoticeService.lambdaQuery()
                .eq(CfgThirdNoticeEntity::getBusinessType, bussinessKey)
                .eq(CfgThirdNoticeEntity::getMethod, CfgThirdNoticeMethodEnum.SINGLE.getCode())
                .eq(CfgThirdNoticeEntity::getNoticeStatus, Boolean.TRUE)
                .list();
        if (CollUtil.isEmpty(cfgThirdNoticeList)) {
            return true;
        }

        List<String> cfgThirdNoticeIdList = cfgThirdNoticeList.stream().map(CfgThirdNoticeEntity::getId).collect(Collectors.toList());
        //三方通知配置--推送消息
        List<CfgApproveSyncFieldMapEntity> fieldMapList = cfgApproveSyncFieldMapService.lambdaQuery().in(CfgApproveSyncFieldMapEntity::getMainId, cfgThirdNoticeIdList).list();
        Map<String, List<CfgApproveSyncFieldMapEntity>> fieldMap = fieldMapList.stream().collect(Collectors.groupingBy(CfgApproveSyncFieldMapEntity::getMainId));
        //三方通知配置--规则条件
        List<CfgRuleConditionEntity> ruleConditionList = cfgRuleConditionService.lambdaQuery().in(CfgRuleConditionEntity::getRuleId, cfgThirdNoticeIdList).list();
        Map<String, List<CfgRuleConditionEntity>> ruleConditionMap = ruleConditionList.stream().collect(Collectors.groupingBy(CfgRuleConditionEntity::getRuleId));

        //主表map
        Map<String, Object> variablesMap = dto.getDataJson();
        for (CfgThirdNoticeEntity noticeEntity : cfgThirdNoticeList) {
            //主键id
            String businessId = String.valueOf(variablesMap.getOrDefault("id", ""));
            //校验规则条件
            if (!checkRule(dto,noticeEntity, ruleConditionMap, bussinessKey)) continue;

            String roleType = noticeEntity.getRoleType();
            String specificPerson = noticeEntity.getSpecificPerson();
            String post = noticeEntity.getPost();
            if (StringUtils.isBlank(post) && StringUtils.isBlank(roleType) && StringUtils.isBlank(specificPerson)) {
                String errorReason = "通知人员不能为空";
                saveFailedRecord(noticeEntity, businessId, bussinessKey,errorReason, variablesMap);
                continue;
            }

            List<String> userIdList = getUserList(post,roleType, specificPerson, businessId,noticeEntity.getBusinessType());
            if (CollUtil.isEmpty(userIdList)) {
                //如果没有unionId，则保存失败记录
                String errorReason = "通知人员id不存在";
                saveFailedRecord(noticeEntity, businessId, bussinessKey,errorReason, variablesMap);
                continue;
            }

            //根据通知方式查找人员 目前只有飞书
            String noticeMethod = noticeEntity.getNoticeMethod();
            if (StringUtils.isNotBlank(noticeMethod)) {
                List<String> noticeMethodList = Arrays.asList(noticeMethod.split(","));
                for (String str : noticeMethodList) {
                    //获取飞书的unionid 与用户关系
                    if (CfgApproveSyncSyncPlatformEnum.FEISHU.getCode().equals(str)) {
                        List<ThirdUnionDTO> unionList  = sysUserFeign.getThirdByUserIds(ThirdpartyPlatformEnum.FS.getCode() , userIdList);
                        if (CollUtil.isEmpty(unionList)) {
                            //如果没有unionId，则保存失败记录
                            String errorReason = "通知人员未绑定飞书";
                            saveFailedRecord(noticeEntity, businessId, bussinessKey,errorReason, variablesMap);
                            continue;
                        }
                        //组装推送消息请求体
                        List<CfgApproveSyncFieldMapEntity> fieldList = fieldMap.get(noticeEntity.getId()).stream()
                                .sorted(Comparator.comparing(CfgApproveSyncFieldMapEntity::getSort))
                                .collect(Collectors.toList());
                        //获取需要推送的表字段（包括common、主表、明细）
                        List<String> fieldIds = fieldList.stream().map(CfgApproveSyncFieldMapEntity::getFieldId).collect(Collectors.toList());

                        //过滤出需要推送的表字段，并且根据字段所属单据类型进行分组
                        Map<String, List<CfgQueryOptionEntity>> fieldBelongsTypeByMap = cfgQueryOptionList.stream()
                                .collect(Collectors.groupingBy(CfgQueryOptionEntity::getFieldBelongsType));
                        //获取主表字段配置
                        List<CfgQueryOptionEntity> mainCfgQueryOptionList = fieldBelongsTypeByMap.get(CfgQueryOptionFieldBelongsTypeEnum.MAIN.getCode());
                        //遍历
                        for (Map.Entry<String, List<CfgQueryOptionEntity>> entry : fieldBelongsTypeByMap.entrySet()) {
                            //common和主表不需要再查询
                            if(entry.getKey().equals(CfgQueryOptionFieldBelongsTypeEnum.COMMON.getCode()) || entry.getKey().equals(CfgQueryOptionFieldBelongsTypeEnum.MAIN.getCode())){
                                continue;
                            }
                            //判断fieldIds 里是否存在某个明细的字段
                            List<CfgQueryOptionEntity> value = entry.getValue();
                            CfgQueryOptionEntity cfgQueryOptionDetail = value.stream().filter(e -> fieldIds.contains(e.getId())).findFirst().orElse(null);
                            if(Objects.isNull(cfgQueryOptionDetail)){
                                continue;
                            }
                            //如果存在，则需要找对明细表里的关联字段，并根据该字段来进行FeignQuery查询出对应的明细列表
                            CfgQueryOptionEntity refEntity = value.stream().filter(e -> StringUtils.isNotBlank(e.getParentId())).findFirst().orElse(null);
                            if (Objects.isNull(refEntity)) {
                                continue;
                            }

                            //获取关联记录
                            String parentId = refEntity.getParentId();
                            CfgQueryOptionEntity mainEntity = mainCfgQueryOptionList.stream().filter(e -> e.getId().equals(parentId)).findFirst().orElse(null);
                            String mainField = mainEntity.getConditionField();
                            String mainValue = String.valueOf(variablesMap.get(mainField));

                            String classpath = cfgQueryOptionDetail.getClasspath();
                            classpath = classpath.replace("class ", "");
                            Class<BaseEntity> clazz = null;
                            try {
                                clazz = (Class<BaseEntity>) Class.forName(classpath);
                            } catch (ClassNotFoundException e) {
                                throw new ServiceException(classpath + "实体不存在");
                            }
                            List<BaseEntity> detailList = FeignQuery.create(clazz)
                                    .eq(refEntity.getConditionField(), mainValue)
                                    .list();

                            if (CollUtil.isEmpty(detailList)) {
                                continue;
                            }
                            //明细数据
                            variablesMap.put(entry.getKey() , BeanUtil.copyToList(detailList,Map.class));
                        }

                        //进行值映射处理
                        Map<String,String> handlerValueMap = new HashMap<>();
                        Map<String,String> remoteValues = new HashMap<>();
                        for (CfgApproveSyncFieldMapEntity entity : fieldList) {
                            //获取CfgApproveSyncFieldMap对应该的配置记录
                            CfgQueryOptionEntity queryOptionEntity = cfgQueryOptionList.stream().filter(e -> Objects.equals(entity.getFieldId(), e.getId())).findFirst().orElse(null);
                            if(Objects.isNull(queryOptionEntity)){
                                continue;
                            }
                            //设置原始值
                            //设置原始值
                            Object fieldValue = variablesMap.getOrDefault(entity.getFieldSource(), "");
                            if(Objects.isNull(fieldValue)){
                                handlerValueMap.put(entity.getFieldId(),"");
                            }else {
                                handlerValueMap.put(entity.getFieldId(),String.valueOf(fieldValue));
                            }
//                            handlerValueMap.put(entity.getFieldId(),String.valueOf(variablesMap.getOrDefault(entity.getFieldSource(), "")));

                            //判断是类型是common、主表还是明细
                            if(queryOptionEntity.getFieldBelongsType().equals(CfgQueryOptionFieldBelongsTypeEnum.COMMON.getCode())
                                    || queryOptionEntity.getFieldBelongsType().equals(CfgQueryOptionFieldBelongsTypeEnum.MAIN.getCode())){
                                String fieldSourceValueStr = getFieldSourceValueStr(entity.getFieldSource(), variablesMap);
                                if(StringUtils.isNotBlank(fieldSourceValueStr)){
                                    handlerValueMap.put(entity.getFieldId(),fieldSourceValueStr);
                                }
                            }else{//其余均为明细表
                                List<Object> detail =( List<Object> ) variablesMap.get(queryOptionEntity.getFieldBelongsType());
                                if(CollUtil.isNotEmpty(detail)){
                                    StringBuffer sb = new StringBuffer();
                                    for (Object object : detail) {
                                        Map<String, Object> map = BeanUtil.beanToMap(object);
                                        String string = getFieldSourceValueStr(entity.getFieldSource(), map);
                                        if(StringUtils.isNotBlank(string)){
                                            sb.append(string);
                                            sb.append(",");
                                        }
                                    }
                                    String fieldSourceDetailValueStr = sb.toString();
                                    if(StringUtils.isNotBlank(fieldSourceDetailValueStr)){
                                        if (fieldSourceDetailValueStr.endsWith(",")) {
                                            fieldSourceDetailValueStr = fieldSourceDetailValueStr.substring(0, fieldSourceDetailValueStr.length() - 1); // 移除最后一个逗号
                                        }
                                        handlerValueMap.put(entity.getFieldId(),fieldSourceDetailValueStr);
                                    }
                                }
                            }
                        }
                        //需要进行值映射
                        if(handlerValueMap.size() > 0) {
                            remoteValues = cfgQueryOptionFeign.getRemoteValues(handlerValueMap);
                        }

                        for (ThirdUnionDTO unionDTO : unionList) {
                            ThirdNoticePushRecordEntity recordEntity = new ThirdNoticePushRecordEntity();
                            recordEntity.setCfgThirdNoticeId(noticeEntity.getId());
                            recordEntity.setNoticeType(ThirdNoticePushRecordNoticeTypeEnum.MESSAGEPUSH.getCode());
                            recordEntity.setBusinessId(businessId);
                            recordEntity.setBusinessType(bussinessKey);
                            recordEntity.setBusinessCode(String.valueOf(variablesMap.getOrDefault("code", "")));
                            recordEntity.setNoticeMethod(CfgApproveSyncSyncPlatformEnum.FEISHU.getCode());
                            recordEntity.setReceiverId(unionDTO.getUserId());
                            recordEntity.setReceiverName(unionDTO.getUserName());
                            recordEntity.setSendTime(LocalDateTime.now());
                            recordEntity.setTitle(noticeEntity.getTitle());
                            recordEntity.setStatus(ThirdNoticePushRecordStatusEnum.SENDING.getCode());
                            boolean save = thirdNoticePushRecordService.save(recordEntity);
                            if(Boolean.TRUE.equals(save)){
                                SendThirdNoticeConsumerDTO sendMessage = new SendThirdNoticeConsumerDTO();
                                sendMessage.setUnionIds(Arrays.asList(unionDTO.getThirdUnionId()));
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
                                    sb.append(remoteValues.getOrDefault(entity.getFieldId(),""));
                                    sb.append("\n");
                                }
                                String content = sb.toString();

                                //跳转URL
                                String url = noticeEntity.getUrl();
                                Map<String, Object> contentMap = fsService.getCardMessageMap(title, content, url);
                                sendMessage.setContentMap(contentMap);
                                sendMessage.setMessageId(recordEntity.getId());

                                mqProducerService.asyncClassMsgByDelayLevel(RocketMqTopic.SEND_THIRD_NOTICE_SYS_TOPIC, RocketMqTagEnum.SYS_SEND_THIRD_NOTICE_TAG.getName(), sendMessage, IdUtil.simpleUUID(), 1);
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private void saveFailedRecord(CfgThirdNoticeEntity noticeEntity, String businessId, String bussinessKey,String errorReason , Map<String, Object> variablesMap) {
        ThirdNoticePushRecordEntity recordEntity = new ThirdNoticePushRecordEntity();
        recordEntity.setCfgThirdNoticeId(noticeEntity.getId());
        recordEntity.setNoticeType(ThirdNoticePushRecordNoticeTypeEnum.MESSAGEPUSH.getCode());
        recordEntity.setBusinessId(businessId);
        recordEntity.setBusinessType(bussinessKey);
        recordEntity.setBusinessCode(String.valueOf(variablesMap.getOrDefault("code", "")));
        recordEntity.setNoticeMethod(CfgApproveSyncSyncPlatformEnum.FEISHU.getCode());
        recordEntity.setSendTime(LocalDateTime.now());
        recordEntity.setTitle(noticeEntity.getTitle());
        recordEntity.setStatus(ThirdNoticePushRecordStatusEnum.FAILED.getCode());
        recordEntity.setErrorReason(errorReason);
        boolean save = thirdNoticePushRecordService.save(recordEntity);
    }

    //规则校验
    private boolean checkRule(MqConsumerRecordDTO.MqDTO dto,CfgThirdNoticeEntity noticeEntity, Map<String, List<CfgRuleConditionEntity>> ruleConditionMap, String bussinessKey) {
        List<CfgRuleConditionEntity> cfgRuleConditionEntities = ruleConditionMap.get(noticeEntity.getId());
        if (CollUtil.isNotEmpty(cfgRuleConditionEntities)) {
            //规则条件转map
            Map<String, String> cfgRuleConditionMap = cfgRuleConditionEntities.stream().collect(Collectors.toMap(CfgRuleConditionEntity::getField, CfgRuleConditionEntity::getValue,(o1,o2) -> o2));

            Map<String, Object> variablesMap = dto.getDataJson();
            //主键id
            String businessId = String.valueOf(variablesMap.getOrDefault("id", ""));
            //单据类型
            String businessType = noticeEntity.getBusinessType();

            List<String> fieldList = cfgRuleConditionEntities.stream().map(CfgRuleConditionEntity::getField).collect(Collectors.toList());
            //查询是否有拓展
            CfgQueryOptionDTO.ListByFieldDTO listByFieldDTO = new CfgQueryOptionDTO.ListByFieldDTO();
            listByFieldDTO.setBusinessType(businessType);
            listByFieldDTO.setFieldList(fieldList);
            List<CfgQueryOptionEntity> cfgQueryOptionList = cfgQueryOptionFeign.listExtendByFieldCondition(listByFieldDTO);
            if(CollUtil.isNotEmpty(cfgQueryOptionList)){
                for (CfgQueryOptionEntity cfgQueryOptionEntity : cfgQueryOptionList) {
                    //规则条件字段对应的值
                    String feildValue = cfgRuleConditionMap.get(cfgQueryOptionEntity.getConditionField());
                    //提审
                    if("waitSubmitToApproveIng".equals(feildValue)){
                        //提交操作
                        ProcessManagementDTO.CheckSubmitByBusinessIdDTO checkSubmitByBusinessIdDTO = new ProcessManagementDTO.CheckSubmitByBusinessIdDTO();
                        checkSubmitByBusinessIdDTO.setBusinessId(businessId);
                        checkSubmitByBusinessIdDTO.setBusinessKey(bussinessKey);
                        Boolean allMatch = workflowFeign.checkSubmitByBusinessId(checkSubmitByBusinessIdDTO);
                        if(Boolean.FALSE.equals(allMatch)){
                            return Boolean.FALSE;
                        }
                    }
                    //新增
                    if("addRecord".equals(feildValue)){
                        //不等于新增则返回false
                       if(!Objects.equals(ThirdNoticeRecordOperationTypeEnum.INSERT.getCode(), dto.getOperationType())){
                           return Boolean.FALSE;
                       }
                    }
                }
            }

            List<String> cfgQueryOptionfieldList = cfgRuleConditionEntities.stream().map(CfgRuleConditionEntity::getField).collect(Collectors.toList());
            //封装条件参数
            Map<String, Object> map = cfgRuleConditionEntities.stream()
                    .filter(e -> Objects.nonNull(e.getField()) && !cfgQueryOptionfieldList.contains(e.getField()))
                    .collect(Collectors.toMap(
                            CfgRuleConditionEntity::getField,
                            e -> variablesMap.get(e.getField())
                    ));
            if(CollUtil.isNotEmpty(map)){
                // 获取所有符合条件的规则
                List<CfgRuleConditionEntity> conditionList = cfgRuleConditionEntities.stream()
                        .sorted(Comparator.comparing(CfgRuleConditionEntity::getIndex))
                        .collect(Collectors.toList());
                List<ConditionElement> conditionElementList = BeanMapper.copyList(conditionList, ConditionElement.class);
                //获取到表达式,判断表达式是否匹配
                Boolean match = spElServer.matchExpressionByConditionList(conditionElementList, map);
                if(Boolean.FALSE.equals(match)){
                    return Boolean.FALSE;
                }
            }
        }
        return Boolean.TRUE;
    }


    private String addMqRecord(MqConsumerRecordDTO.MqDTO dto) {
        Gson gson = new Gson();
        MqConsumerRecordEntity mqConsumerRecord = new MqConsumerRecordEntity();
        mqConsumerRecord.setTopic(RocketMqTopic.SEND_THIRD_NOTICE_SYS_TOPIC.replace("${spring.cloud.nacos.discovery.namespace}", namespace));
        mqConsumerRecord.setTag(RocketMqTagEnum.SYS_SEND_THIRD_NOTICE_TAG.name());
        mqConsumerRecord.setConsumerGroup(RocketMqConsumerGroup.SYS_SEND_THIRD_NOTICE_CONSUMER.replace("${spring.cloud.nacos.discovery.namespace}", namespace));
        mqConsumerRecord.setDataJson(gson.toJson(dto.getDataJson()));
        boolean flag = mqConsumerRecordService.save(mqConsumerRecord);
        if(flag){
            return mqConsumerRecord.getId();
        }
        return "";
    }


    /**
     * 获取系统设置的岗位人员(去重)
     * @param
     * @return java.util.List<java.lang.String>
     * @author jack
     * @date 2025-05-30
     */
    private List<String> getUserList(String post, String roleType, String specificPerson, String businessId,String businessKey) {
        List<String> resultList = new ArrayList<>();

        //具体人员
        if(StringUtils.isNotBlank(specificPerson)){
            List<String> otherPeopleIds = Arrays.asList(specificPerson.split(","));
            resultList.addAll(otherPeopleIds);
        }

        if(StringUtils.isNotBlank(post)){
            List<String> postIdList = Arrays.asList(post.split(","));

            //岗位id
            List<SysPostUserEntity> userEntityList = sysPostFeign.getUserIdByPostIds(postIdList);
            if(CollectionUtils.isNotEmpty(userEntityList)){
                resultList.addAll(userEntityList.stream().map(SysPostUserEntity::getUserId).distinct().collect(Collectors.toList()));
            }
        }
        //
        if(StringUtils.isNotBlank(roleType)){
            List<String> roleId = Arrays.asList(roleType.split(","));
            List<DictNoticeRoleOptionEntity> list = dictNoticeRoleOptionService.lambdaQuery().in(DictNoticeRoleOptionEntity::getId, roleId).list();
            if(CollUtil.isNotEmpty(list)){
                for (DictNoticeRoleOptionEntity optionEntity : list) {
                    String field = optionEntity.getField();
                    String classPath = optionEntity.getClassPath();
                    String refField = optionEntity.getRefField();
                    if(StringUtils.isNotBlank(field) && StringUtils.isNotBlank(classPath)) {
                        try {
                            //获取当前审批人逻辑需要特殊处理
                            if (field.equals("approveUserId")) {
                                String[] split = classPath.split("#");
                                String controller = split[0];
                                String methodName = split[1];
                                ProcessManagementDTO.HistoryActivityDTO dto = new ProcessManagementDTO.HistoryActivityDTO();
                                dto.setBusinessKey(businessKey);
                                dto.setBusinessId(businessId);
                                ApiResult select = FeignQuery.invoke(ApiResult.class, controller, methodName, Arrays.asList(dto));
                                if(Objects.nonNull(select)){
                                    List<ProcessManagementDTO.CurApproveInfoDTO> data = JSON.parseArray(JSON.toJSONString(select.getData()), ProcessManagementDTO.CurApproveInfoDTO.class);
                                    if(CollUtil.isNotEmpty(data)){
                                        resultList.addAll(Arrays.asList(data.get(0).getCurApproveId().split(",")));
                                    }
                                }
                            } else {
                                String ref = "id";
                                Class<BaseEntity> clazz = (Class<BaseEntity>) Class.forName(classPath);
                                if (StringUtils.isNotBlank(refField) && optionEntity.getTableType().equals(DictNoticeRoleOptionTableTypeEnum.DETAIL.getCode())) {
                                    ref = refField;
                                }
                                List<BaseEntity> baseEntityList = FeignQuery.create(clazz)
                                        .eq(ref, businessId)
                                        .list();
                                if (CollUtil.isNotEmpty(baseEntityList)) {
                                    // 获取字段值
                                    String result = baseEntityList.stream()
                                            .map(item -> String.valueOf(ReflectUtil.getFieldValue(item, field)))
                                            .filter(value -> StringUtils.isNotBlank(value))
                                            .collect(Collectors.joining(","));
                                    if (StringUtils.isNotBlank(result)) {
                                        resultList.add(result);
                                    }
                                }
                            }
                        } catch (ClassNotFoundException e) {

                        }
                    }
                }
            }
        }
        return resultList.stream().filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
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
}
