package com.erp.server.dmp.service.mq;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import com.erp.model.dmp.lingxing.FbaReceiveGroupEntity;
import com.erp.rpc.wms.feign.WmsShipmentFeign;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 新中台-领星FBA货件签收消费
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_LX_FBA_SHIPMENT_RECEIVE_TO_DMP_TOPIC,
        selectorExpression = RocketMqNewTag.DMP_LX_FBA_SHIPMENT_RECEIVE_TO_DMP_TAG,
        consumerGroup = RocketMqNewConsumerGroup.DMP_LX_FBA_SHIPMENT_RECEIVE_TO_DMP_GROUP)
public class NewLxFbaShipmentReceiveConsumer extends AbstractNewPlatformConsumerHandler {

    @Resource
    private WmsShipmentFeign wmsShipmentFeign;

    @Override
    public String getBizName() {
        return "领星FBA货件";
    }

    @Override
    public void handle(String data) {
        log.info("监听领星Fba签收明细消息：entity={}", JSONUtil.toJsonStr(data));
        FbaReceiveGroupEntity ext = JSONUtil.toBean(data, FbaReceiveGroupEntity.class);
        // 检查店铺ID
        if (null == ext.getShopId()) {
            throw new ServiceException(StrUtil.format("来源数据异常, 店铺ID为空, dto={}", data));
        }
        // 保存和检查调拨
        wmsShipmentFeign.saveAndCheckTransfer(ext);
    }
}