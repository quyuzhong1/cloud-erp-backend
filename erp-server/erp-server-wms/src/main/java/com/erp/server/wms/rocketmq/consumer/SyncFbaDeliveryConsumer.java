package com.erp.server.wms.rocketmq.consumer;

import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncKingdeeStatusEnum;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.dmp.dto.DmpSyncMqDTO;
import com.erp.model.dmp.entity.DmpFbaDeliveryEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.wms.rocketmq.sync.SyncFbaDeliveryService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
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

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Autowired
    private SyncFbaDeliveryService syncFbaDeliveryService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void onMessage(DmpSyncMqDTO dmpSyncMqDTO) {
        DmpSyncMqDTO.ParamDTO paramDTO = new DmpSyncMqDTO.ParamDTO();
        paramDTO.setDmpSyncTaskId(dmpSyncMqDTO.getDmpSyncTaskId());
        String dataJson = dmpSyncMqDTO.getMqData();
        log.info("监听到马帮FBA发货单需要同步生成加工单：entity={}", dataJson);
        try {
            DmpFbaDeliveryEntity dmpFbaDeliveryEntity = JSONObject.parseObject(dataJson, DmpFbaDeliveryEntity.class);
            syncFbaDeliveryService.syncFbaDelivery(dmpFbaDeliveryEntity, SourceTypeEnum.MABANG_FBA_DELIVERY.getCode());
            //同步成功
            paramDTO.setSyncStatus(SyncKingdeeStatusEnum.SUCCESS_SYNC.getCode());
            paramDTO.setResponseMsg("同步成功");
            dmpTaskFeign.updateSyncInfo(paramDTO);
        } catch (Exception e) {
            log.error("马帮FBA发货单同步生成加工单失败",e);
            //同步失败
            paramDTO.setSyncStatus(SyncKingdeeStatusEnum.FAILED_SYNC.getCode());
            paramDTO.setResponseMsg(e.getMessage());
            dmpTaskFeign.updateSyncInfo(paramDTO);
        }
    }


}