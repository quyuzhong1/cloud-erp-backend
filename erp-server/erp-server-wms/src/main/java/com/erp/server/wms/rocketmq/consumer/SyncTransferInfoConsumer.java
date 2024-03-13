package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.enums.SyncStatusEnum;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.dmp.dto.DmpTransferInfoDTO;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.wms.rocketmq.sync.SyncTransferInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Will
 * @version 1.0

 * @date 2023/4/20 11:12
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.DMP_SYNC_TASK_TOPIC, selectorExpression = "sync_kingdee_transfer_info_to_wms_tag", consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_TRANSFER_INFO_TO_WMS)
public class SyncTransferInfoConsumer implements RocketMQListener<Object> {

    @Resource
    private SyncTransferInfoService syncTransferInfoService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Override
    public void onMessage(Object ext) {
        //json数据
        JSONObject jsonObject = JSONUtil.parseObj(ext);
        String dmpSyncTaskId = jsonObject.get("dmpSyncTaskId").toString();

        DmpSyncMqDTO.ParamDTO paramDTO = new DmpSyncMqDTO.ParamDTO();
        paramDTO.setDmpSyncTaskId(dmpSyncTaskId);
        log.info("监听到金蝶直接调拨单需要同步：entity={}", jsonObject);
        DmpTransferInfoDTO dmpTransferInfoDTO = JSONUtil.toBean(jsonObject,  DmpTransferInfoDTO.class);
        try {
            syncTransferInfoService.syncKingdeeTransferInfo(dmpTransferInfoDTO);
        } catch (Exception e) {
            log.error("金蝶直接调拨单同步失败，msg = {}",e.getMessage());
            //同步失败
            paramDTO.setSyncStatus(SyncStatusEnum.FAILED_SYNC.getCode());
            paramDTO.setResponseMsg(e.getMessage());
            dmpTaskFeign.updateSyncInfo(paramDTO);
            //错误预警
            dmpTaskFeign.sendWarnMsg(dmpSyncTaskId);
            return;
        }
        //同步成功
        paramDTO.setSyncStatus(SyncStatusEnum.SUCCESS_SYNC.getCode());
        paramDTO.setResponseMsg("同步成功");
        dmpTaskFeign.updateSyncInfo(paramDTO);

    }

}

