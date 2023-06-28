package com.erp.server.dmp.push.consumer.mabang;

import com.alibaba.fastjson.JSONObject;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.dmp.dto.mabang.DmpMabangInOutStockMsgDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

/**
 * @CreateTime: 2023-06-28  14:45
 * @Author: zhangchunlin
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_DMP_TO_MABANG_TOPIC, selectorExpression = "dmp_mabang_transfer_info_tag", consumerGroup = RocketMqConsumerGroup.SYNC_DMP_TRANSFER_INFO_TO_MABANG)
public class DmpMabangTransferInfoConsume implements RocketMQListener<DmpMabangInOutStockMsgDTO>  {


    @Override
    public void onMessage(DmpMabangInOutStockMsgDTO dmpMabangInOutStockMsgDTO) {
        log.info("监听到DMP直接调拨单信息->出入库，内容：{}", JSONObject.toJSONString(dmpMabangInOutStockMsgDTO));

    }

}