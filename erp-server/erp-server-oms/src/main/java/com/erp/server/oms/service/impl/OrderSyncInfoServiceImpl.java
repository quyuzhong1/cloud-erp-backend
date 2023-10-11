package com.erp.server.oms.service.impl;

import com.common.business.service.impl.SuperServiceImpl;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.server.oms.mapper.SoInfoMapper;
import com.erp.server.oms.service.OrderSyncInfoService;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;

/**
 * @author zdy
 * @ClassName OrderInfoServiceImpl
 * @description: 订单同步处理服务类
 * @date 2023年10月10日
 * @version: 1.0
 */
@Service
public class OrderSyncInfoServiceImpl extends SuperServiceImpl<SoInfoMapper, SoInfoEntity> implements OrderSyncInfoService {

    @Resource
    private MQProducerService mQProducerService;
    /**
     * 异步推送订单到mq
     * @param soInfoEntity
     */
    @Override
    public void asyncOrderToDmp(SoInfoEntity soInfoEntity) {
        //异步推送mq
        CompletableFuture.supplyAsync(() -> {
            SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, RocketMqTagEnum.KINGDEE_SO_INFO_TAG.getName(), soInfoEntity, soInfoEntity.getId());
            if (result.getSendStatus().equals(SendStatus.SEND_OK)) {
                //TODO mq发送成功后 加推送记录 dmp_pull_task
                
            }
            return Boolean.TRUE;
        });
    }
}
