package com.erp.server.dmp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.common.business.dto.DmpSyncMqDTO;
import com.erp.model.dmp.dto.DmpTransferInfoDTO;
import com.erp.model.dmp.entity.DmpPullTaskEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.server.dmp.service.DmpPullTaskService;
import com.erp.server.dmp.service.DmpTransferInfoService;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author Will
 * @version 1.0
 * @date 2023/7/4 18:56
 */
@Service
public class DmpTransferInfoServiceImpl implements DmpTransferInfoService {

    @Resource
    private DmpPullTaskService dmpPullTaskService;

    @Resource
    private MQProducerService mqProducerService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendSyncTask(DmpTransferInfoDTO ext) {
        //新增发送任务
        DmpPullTaskEntity dmpSyncTaskEntity = new DmpPullTaskEntity();
        dmpSyncTaskEntity.setSourcePlatformName(PlatformEnum.KINGDEE.getDesc());
        dmpSyncTaskEntity.setSourceType(SourceTypeEnum.STK_TRANSFERDIRECT.getCode());
        dmpSyncTaskEntity.setSourceId(ext.getSourceId());
        dmpSyncTaskEntity.setSourceCode(ext.getCode());
        dmpSyncTaskEntity.setTargetPlatformName(PlatformEnum.ERP.getDesc());
        dmpSyncTaskEntity.setStatus(SyncStatusEnum.TO_BE_SYNC.getCode());
        dmpSyncTaskEntity.setMqTopic(RocketMqTopic.DMP_SYNC_TASK_TOPIC);
        dmpSyncTaskEntity.setMqTag(RocketMqTagEnum.SYNC_KINGDEE_TRANSFER_INFO_TO_WMS_TAG.getName());
        String mqData = JSONObject.toJSONString(ext);
        dmpSyncTaskEntity.setMqData(mqData);
        dmpPullTaskService.saveOrUpdateDmpSyncTask(dmpSyncTaskEntity);

        //直接调拨单消息推送
        DmpSyncMqDTO dmpSyncMqDTO = new DmpSyncMqDTO(dmpSyncTaskEntity.getId(), mqData);
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_SYNC_TASK_TOPIC, RocketMqTagEnum.SYNC_KINGDEE_TRANSFER_INFO_TO_WMS_TAG.getName(),
                dmpSyncMqDTO, StrUtil.uuid().toLowerCase());
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
            throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
    }
}
