package com.erp.server.dmp.pull.service.stock;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.dto.GoodcangDTO;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.GoodcangStockService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 谷仓库存业务处理实现
 *
 * @Author Cloud
 * @Date 2023/3/29 16:02
 **/
@Slf4j
@Service
public class GoodcangStockServiceImpl implements GoodcangStockService {


    @Resource
    private MongoService mongoService;
    @Resource
    private MQProducerService<GoodcangDTO.MessageDTO> mqProducerService;
    @Override
    @Transactional(transactionManager = "mongoTransactionManager", rollbackFor = Exception.class)
    public void receiveGoDownEntry(GoodcangDTO.MessageDTO message) {
        // 保存到mongo
        message.setPlatformSign("谷仓");
        mongoService.saveMongoData(message, MongoTableNameContant.ORIGINAL_GC_INBOUND_ORDER);
        // 同步推送到MQ
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, RocketMqTagEnum.GC_STOCK_INBOUND_ORDER_TAG.getName(),
                message, message.getReceivingCode());
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())){
            throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
    }
}
