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
import com.common.core.enums.ApiError;
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
import com.erp.model.workflow.enums.ApproveSyncFailedTypeEnum;
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
    装箱完成通知 com.erp.server.wms.service.impl.PackingTaskServiceImpl.NOPERSONMsg

    //------mrp------
    确认发货建议 com.erp.server.mrp.schedule.CfgNoticeJob.sendMrpNotice
    生成发货建议 com.erp.server.mrp.schedule.CfgNoticeJob.sendMrpNotice

    //------tms------
    渠道更换 com.erp.server.tms.service.impl.TmsFirstMileLogisticServiceImpl.updateChannel  / com.erp.server.tms.service.impl.TmsFirstMileLogisticServiceImpl.batchUpdateChannel

    组包预报生成【新增】  --组包称重
    物流单下单成功【新增】 -- B2C销售订单跟踪号
*/



    @Resource
    private CfgQueryOptionFeign cfgQueryOptionFeign;

    @Resource
    private CfgThirdNoticeService cfgThirdNoticeService;

    @Resource
    private CfgApproveSyncFieldMapService cfgApproveSyncFieldMapService;

    @Resource
    private CfgRuleConditionService cfgRuleConditionService;

    @Resource
    private ThirdNoticePushRecordService thirdNoticePushRecordService;

    @Resource
    private MqConsumerRecordService mqConsumerRecordService;

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
            String id = addMqRecord(dto);
            if (StringUtils.isNotBlank(id)) {
                // 异步执行 sendMsg，不阻塞当前事务
                new Thread(() -> {
                    try {
                        send(dto);
                    } catch (Exception e) {
                        log.error("sendMsg 异常", e);
                    }
                }).start();
            }
        }
        log.info("MqRecordConsumerService 结束");
    }

    private boolean send(MqConsumerRecordDTO.MqDTO dto) {
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

        for (CfgThirdNoticeEntity noticeEntity : cfgThirdNoticeList) {
            thirdNoticePushRecordService.sendMsgByCfg(dto, noticeEntity, ruleConditionMap, bussinessKey, fieldMap, cfgQueryOptionList);
        }
        return false;
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


}
