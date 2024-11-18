package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.entity.DmpFbaDeliveryEntity;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.wms.rocketmq.sync.SyncFbaDeliveryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 马帮FBA货件同步到ERP WMS生成加工单
 * @CreateTime: 2023-06-30  14:27
 * @Author: zhangchunlin
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.DMP_SYNC_TASK_TOPIC, selectorExpression = "sync_mabang_fba_delivery_to_wms_tag", consumerGroup = RocketMqConsumerGroup.SYNC_MABANG_FBA_DELIVERY_TO_WMS)
public class SyncFbaDeliveryConsumer implements RocketMQListener<Object> {

    @Resource
    private SyncFbaDeliveryService syncFbaDeliveryService;

    @Resource
    private MQProducerService mqProducerService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Override
    public void onMessage(Object ext) {

        //json数据
        JSONObject jsonObject = JSONUtil.parseObj(ext);
        String dmpSyncTaskId = jsonObject.get("dmpSyncTaskId").toString();

        DmpSyncMqDTO.ParamDTO paramDTO = new DmpSyncMqDTO.ParamDTO();
        paramDTO.setDmpSyncTaskId(dmpSyncTaskId);
        log.warn("监听到马帮FBA发货单需要同步生成加工单：entity={}", jsonObject);
        try {
            DmpFbaDeliveryEntity dmpFbaDeliveryEntity = JSONUtil.toBean(jsonObject, DmpFbaDeliveryEntity.class);
            syncFbaDeliveryService.syncFbaDelivery(dmpFbaDeliveryEntity, SourceTypeEnum.MABANG_FBA_DELIVERY.getCode(), dmpSyncTaskId);
            // 同步成功
            paramDTO.setSyncStatus(SyncStatusEnum.SUCCESS_SYNC.getCode());
            paramDTO.setResponseMsg("同步成功");
        } catch (Exception e) {
            log.error("马帮FBA发货单同步生成加工单失败",e);
            // 同步失败
            paramDTO.setSyncStatus(SyncStatusEnum.FAILED_SYNC.getCode());
            paramDTO.setResponseMsg(e.getMessage());
            //错误预警
            dmpTaskFeign.sendWarnMsg(dmpSyncTaskId);
            // 发送消息通知
            this.sendTaskNotice(dmpSyncTaskId,
                    CharSequenceUtil.format("FBA发货单生成ERP加工单异常，同步任务id：{}，异常原因：{}", paramDTO.getDmpSyncTaskId(), e.getMessage()));
        }

        // 同步任务状态回调
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_SYNC_TASK_TOPIC, RocketMqTagEnum.DMP_SYNC_TASK_CALLBACK_TAG.getName(),
                paramDTO, StrUtil.uuid().toLowerCase());
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
            if(Objects.equals(paramDTO.getSyncStatus(), SyncStatusEnum.SUCCESS_SYNC.getCode())) {
                this.sendTaskCallbackNotice(dmpSyncTaskId, "FBA发货单生成ERP加工单成功，发送同步状态通知消息异常，需手工修改同步状态");
            } else if (Objects.equals(paramDTO.getSyncStatus(), SyncStatusEnum.FAILED_SYNC.getCode())) {
                log.info("FBA发货单生成ERP加工单失败，失败原因：【{}】,同步任务id：【{}】", paramDTO.getDmpSyncTaskId());
                this.sendTaskCallbackNotice(dmpSyncTaskId, "FBA发货单生成ERP加工单失败，发送同步状态通知消息异常，需手工修改同步状态");
            }
        }
    }

    /**
     * FBA发货单生成ERP加工单异常通知
     * @param syncTaskId
     * @param errInfo
     */
    private void sendTaskNotice(String syncTaskId, String errInfo) {
        WarnMsgInfoDTO warnMsgInfoDTO = new WarnMsgInfoDTO();
        warnMsgInfoDTO.setTitle("FBA发货单生成ERP加工单异常");
        warnMsgInfoDTO.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_WMS);
        warnMsgInfoDTO.setBizName("FBA发货单生成ERP加工单");
        warnMsgInfoDTO.setTableName("dmp_sync_task");
        warnMsgInfoDTO.setTableId(syncTaskId);
        warnMsgInfoDTO.setKeyInfo(errInfo);
        mqProducerService.sendWarnMsg(warnMsgInfoDTO);
    }

    /**
     * MQ发送状态同步状态信息异常通知
     * @param syncTaskId
     * @param errInfo
     */
    private void sendTaskCallbackNotice(String syncTaskId, String errInfo) {
        WarnMsgInfoDTO warnMsgInfoDTO = new WarnMsgInfoDTO();
        warnMsgInfoDTO.setTitle("FBA发货单生成ERP加工单发送同步状态通知消息异常");
        warnMsgInfoDTO.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_DMP);
        warnMsgInfoDTO.setBizName("FBA发货单生成ERP加工单发送同步状态通知");
        warnMsgInfoDTO.setTableName("dmp_sync_task");
        warnMsgInfoDTO.setTableId(syncTaskId);
        warnMsgInfoDTO.setKeyInfo(errInfo);
        mqProducerService.sendWarnMsg(warnMsgInfoDTO);
    }


}