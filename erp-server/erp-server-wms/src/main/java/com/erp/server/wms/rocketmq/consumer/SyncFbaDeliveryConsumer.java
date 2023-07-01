package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncKingdeeStatusEnum;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.DmpSyncMqDTO;
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
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 马帮FBA货件同步到ERP WMS生成加工单
 * @CreateTime: 2023-06-30  14:27
 * @Author: zhangchunlin
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.DMP_SYNC_TASK_TOPIC, selectorExpression = "sync_mabang_fba_delivery_to_wms_tag", consumerGroup = RocketMqConsumerGroup.SYNC_MABANG_FBA_DELIVERY_TO_WMS)
public class SyncFbaDeliveryConsumer implements RocketMQListener<DmpSyncMqDTO> {

    @Autowired
    private SyncFbaDeliveryService syncFbaDeliveryService;

    @Autowired
    private MQProducerService mqProducerService;

    @Override
    public void onMessage(DmpSyncMqDTO dmpSyncMqDTO) {
        DmpSyncMqDTO.ParamDTO paramDTO = new DmpSyncMqDTO.ParamDTO();
        paramDTO.setDmpSyncTaskId(dmpSyncMqDTO.getDmpSyncTaskId());
        String dataJson = dmpSyncMqDTO.getMqData();
        log.info("监听到马帮FBA发货单需要同步生成加工单：entity={}", dataJson);
        try {
            DmpFbaDeliveryEntity dmpFbaDeliveryEntity = JSONObject.parseObject(dataJson, DmpFbaDeliveryEntity.class);
            syncFbaDeliveryService.syncFbaDelivery(dmpFbaDeliveryEntity, SourceTypeEnum.MABANG_FBA_DELIVERY.getCode(), dmpSyncMqDTO.getDmpSyncTaskId());
            // 同步成功
            paramDTO.setSyncStatus(SyncKingdeeStatusEnum.SUCCESS_SYNC.getCode());
            paramDTO.setResponseMsg("同步成功");
        } catch (Exception e) {
            log.error("马帮FBA发货单同步生成加工单失败",e);
            // 同步失败
            paramDTO.setSyncStatus(SyncKingdeeStatusEnum.FAILED_SYNC.getCode());
            paramDTO.setResponseMsg(e.getMessage());
        }

        // 同步任务状态回调
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_SYNC_TASK_TOPIC, RocketMqTagEnum.DMP_SYNC_TASK_CALLBACK_TAG.getName(),
                paramDTO, StrUtil.uuid().toLowerCase());
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())){
            this.sendNotice(dmpSyncMqDTO.getDmpSyncTaskId(), "FBA发货单生成ERP加工单成功，发送同步状态通知消息异常，需手工修改同步状态");
        }

    }

    /**
     * 发送异常通知
     * @param syncTaskId
     * @param errInfo
     */
    private void sendNotice(String syncTaskId, String errInfo) {
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