package com.erp.server.sys.rocketmq.consumer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.common.business.utils.RedisUtil;
import com.common.business.wrapper.FeignQuery;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.sys.dto.MqConsumerRecordDTO;
import com.erp.model.sys.entity.*;
import com.erp.model.sys.enums.*;
import com.erp.model.workflow.entity.CfgQueryOptionEntity;
import com.erp.model.workflow.enums.CfgQueryOptionFieldBelongsTypeEnum;
import com.erp.server.sys.service.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.Executor;

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
    private CfgThirdNoticeService cfgThirdNoticeService;

    @Resource
    private ThirdNoticePushRecordService thirdNoticePushRecordService;

    @Resource
    private MqConsumerRecordService mqConsumerRecordService;

    private String namespace = SpringUtil.getProperty("spring.cloud.nacos.discovery.namespace");

    @Resource
    private RedisUtil redisUtil;

    @Resource
    @Qualifier("thirdNoticePushExecutor")
    private Executor thirdNoticePushExecutor;


    public static final String TABLE_BUSINESS_KEY = "TABLE_BUSINESS_KEY";

    @Override
    public void onMessage(String jsonStr) {
        log.info("MqRecordConsumerService 开始");
        if (StringUtils.isBlank(jsonStr)) {
            return;
        }
        // 创建 Gson 实例
        Gson gson = new Gson();
        List<Map<String, Object>> list = new ArrayList<>();
        //jsonStr 有可能是数组的，也有可能是非数组
        if (jsonStr.startsWith("[")) {//表示数组
            list = gson.fromJson(jsonStr, new TypeToken<List<Map<String, Object>>>(){}.getType());
        }else{
            Map<String, Object> jsonMap = gson.fromJson(jsonStr, Map.class);
            list.add(jsonMap);
        }

        for (Map<String, Object> jsonMap : list) {

            //根据table获取单据类型
            String table = jsonMap.get("table") == null ? "" : String.valueOf(jsonMap.get("table"));
            String businessKey = getBusinessKey(table);
            if(StringUtils.isBlank(businessKey)){
                return;
            }

            //获取三方通知配置的信息--单条--即时通知
            Integer count = cfgThirdNoticeService.lambdaQuery()
                    .eq(CfgThirdNoticeEntity::getBusinessType, businessKey)
                    .eq(CfgThirdNoticeEntity::getMethod, CfgThirdNoticeMethodEnum.SINGLE.getCode())
                    .eq(CfgThirdNoticeEntity::getNoticeStatus, Boolean.TRUE)
                    .count();
            if (count <= 0) {
                return;
            }

            MqConsumerRecordDTO.MqDTO dto = new MqConsumerRecordDTO.MqDTO();
            dto.setDb(jsonMap.get("db") == null ? "" : String.valueOf(jsonMap.get("db")));
            dto.setTable(jsonMap.get("table") == null ? "" : String.valueOf(jsonMap.get("table")));
            dto.setOperationType(jsonMap.get("P_TAG_IUD") == null ? "" : String.valueOf(jsonMap.get("P_TAG_IUD")));
            //需要把每个字段都转出驼峰
            Map<String, Object> convertedMap = convertToCamelCaseMap(jsonMap);
            dto.setDataJson(convertedMap);
            dto.setBusinessKey(businessKey);

            //接收中台发送的ddl变更
            //参数不能为空
            if (StringUtils.isNotBlank(dto.getDb()) && StringUtils.isNotBlank(dto.getTable()) && StringUtils.isNotBlank(dto.getOperationType()) && Objects.nonNull(dto.getDataJson())) {
                //保存mq消费记录
                // 将 Map 转换为 JSON 字符串
                String id = addMqRecord(dto);
                if (StringUtils.isNotBlank(id)) {
                    dto.setMqConsumerRecordId(id);
                    thirdNoticePushRecordService.sendThirdNoticeByMqAsync(dto);
                }
            }
        }
        log.info("MqRecordConsumerService 结束");
    }

    /**
     *  根据table获取单据类型
     */
    private String getBusinessKey(String table){
        String bussinessKey = String.valueOf(redisUtil.hget(TABLE_BUSINESS_KEY, table));
        if(StringUtils.isBlank(bussinessKey)){
            List<CfgQueryOptionEntity> cfgQueryOptionEntityList = FeignQuery.create(CfgQueryOptionEntity.class)
                    .eq(CfgQueryOptionEntity::getTableName, table)
                    .eq(CfgQueryOptionEntity::getFieldBelongsType,CfgQueryOptionFieldBelongsTypeEnum.MAIN.getCode()) //限定主表类型
                    .ne(CfgQueryOptionEntity::getBussinessKey, "")
                    .last( "limit 1")
                    .list();
            if(CollUtil.isEmpty(cfgQueryOptionEntityList)){
                return bussinessKey;
            }

            bussinessKey = cfgQueryOptionEntityList.get(0).getBussinessKey();

            //缓存table 和 busineskey的映射关系
            redisUtil.hset(TABLE_BUSINESS_KEY,table,bussinessKey);
        }
        return bussinessKey;
    }

    /**
     *  下划线转驼峰
     */
    private Map<String, Object> convertToCamelCaseMap(Map<String, Object> jsonMap) {
        Map<String, Object> convertedMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
            String originalKey = entry.getKey();
            Object value = entry.getValue();
            String camelCaseKey = CharSequenceUtil.toCamelCase(originalKey);
            convertedMap.put(camelCaseKey, value);
        }
        return convertedMap;
    }

    /**
     * 新增一条mq的消费记录
     */
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
