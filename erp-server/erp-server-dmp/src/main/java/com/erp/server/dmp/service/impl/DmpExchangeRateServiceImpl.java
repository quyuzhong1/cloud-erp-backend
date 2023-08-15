package com.erp.server.dmp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncKingdeeStatusEnum;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.DmpExchangeRateDTO;
import com.erp.model.dmp.dto.DmpSyncMqDTO;
import com.erp.model.dmp.entity.DmpSyncTaskEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.server.dmp.service.DmpExchangeRateService;
import com.erp.server.dmp.service.DmpSyncTaskService;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/8/14 16:45
 */
@Service
public class DmpExchangeRateServiceImpl implements DmpExchangeRateService {

    @Resource
    private DmpSyncTaskService dmpSyncTaskService;

    @Resource
    private MQProducerService mqProducerService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendSyncTask(DmpExchangeRateDTO ext) {
        //新增发送任务
        DmpSyncTaskEntity dmpSyncTaskEntity = new DmpSyncTaskEntity();
        dmpSyncTaskEntity.setSourcePlatformName(PlatformEnum.KINGDEE.getDesc());
        dmpSyncTaskEntity.setSourceType(SourceTypeEnum.BD_RATE.getCode());
        dmpSyncTaskEntity.setSourceId(ext.getSourceId());
        dmpSyncTaskEntity.setTargetPlatformName(PlatformEnum.ERP.getDesc());
        dmpSyncTaskEntity.setStatus(SyncKingdeeStatusEnum.TO_BE_SYNC.getCode());
        dmpSyncTaskEntity.setMqTopic(RocketMqTopic.DMP_SYNC_TASK_TOPIC);
        dmpSyncTaskEntity.setMqTag(RocketMqTagEnum.SYNC_KINGDEE_EXCHANGE_RATE_TO_WMS_TAG.getName());
        String mqData = JSONObject.toJSONString(ext);
        dmpSyncTaskEntity.setMqData(mqData);
        dmpSyncTaskService.saveOrUpdateDmpSyncTask(dmpSyncTaskEntity);

        //汇率列表消息推送
        DmpSyncMqDTO dmpSyncMqDTO = new DmpSyncMqDTO(dmpSyncTaskEntity.getId(), mqData);
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_SYNC_TASK_TOPIC, RocketMqTagEnum.SYNC_KINGDEE_EXCHANGE_RATE_TO_WMS_TAG.getName(),
                dmpSyncMqDTO, StrUtil.uuid().toLowerCase());
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
            throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
    }
}
