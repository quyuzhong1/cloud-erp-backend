package com.erp.server.dmp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.DmpExchangeRateDTO;
import com.common.business.dto.DmpSyncMqDTO;
import com.erp.model.dmp.entity.DmpPullTaskEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.server.dmp.service.DmpExchangeRateService;
import com.erp.server.dmp.service.DmpPullTaskService;
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
    private DmpPullTaskService dmpPullTaskService;

    @Resource
    private MQProducerService mqProducerService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendSyncTask(DmpExchangeRateDTO ext) {
        //新增发送任务
        DmpPullTaskEntity dmpPullTaskEntity = new DmpPullTaskEntity();
        dmpPullTaskEntity.setSourcePlatformName(PlatformEnum.KINGDEE.getDesc());
        dmpPullTaskEntity.setSourceType(SourceTypeEnum.BD_RATE.getCode());
        dmpPullTaskEntity.setSourceId(ext.getSourceId());
        dmpPullTaskEntity.setTargetPlatformName(PlatformEnum.ERP.getDesc());
        dmpPullTaskEntity.setStatus(SyncStatusEnum.TO_BE_SYNC.getCode());
        dmpPullTaskEntity.setMqTopic(RocketMqTopic.DMP_SYNC_TASK_TOPIC);
        dmpPullTaskEntity.setMqTag(RocketMqTagEnum.SYNC_KINGDEE_EXCHANGE_RATE_TO_WMS_TAG.getName());
        String mqData = JSONObject.toJSONString(ext);
        dmpPullTaskEntity.setMqData(mqData);
        dmpPullTaskService.saveOrUpdateDmpSyncTask(dmpPullTaskEntity);

        //汇率列表消息推送
        DmpSyncMqDTO dmpSyncMqDTO = new DmpSyncMqDTO(dmpPullTaskEntity.getId(), mqData);
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_SYNC_TASK_TOPIC, RocketMqTagEnum.SYNC_KINGDEE_EXCHANGE_RATE_TO_WMS_TAG.getName(),
                dmpSyncMqDTO, StrUtil.uuid().toLowerCase());
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
            throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
    }
}
