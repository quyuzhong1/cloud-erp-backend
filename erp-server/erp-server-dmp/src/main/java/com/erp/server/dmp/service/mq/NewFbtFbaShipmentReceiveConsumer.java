package com.erp.server.dmp.service.mq;

import cn.hutool.core.collection.CollUtil;
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
 * 新中台-FBT货件签收消费
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_FBT_FBA_SHIPMENT_RECEIVE_TO_DMP_TOPIC,
        selectorExpression = RocketMqNewTag.DMP_FBT_FBA_SHIPMENT_RECEIVE_TO_DMP_TAG,
        consumerGroup = RocketMqNewConsumerGroup.DMP_FBT_FBA_SHIPMENT_RECEIVE_TO_DMP_GROUP)
public class NewFbtFbaShipmentReceiveConsumer extends AbstractNewPlatformConsumerHandler {

    @Resource
    private WmsShipmentFeign wmsShipmentFeign;

    @Override
    public String getBizName() {
        return "FBT货件签收";
    }

    @Override
    public void handle(String data) {
        log.info("监听FBT签收明细消息：entity={}", JSONUtil.toJsonStr(data));
        FbaReceiveGroupEntity ext = JSONUtil.toBean(data, FbaReceiveGroupEntity.class);
        if (ext == null || StrUtil.isBlank(ext.getShopId())) {
            throw new ServiceException(StrUtil.format("来源数据异常, 店铺ID为空, dto={}", data));
        }
        if (CollUtil.isEmpty(ext.getDetailList())) {
            throw new ServiceException(StrUtil.format("来源数据异常, 签收明细为空, dto={}", data));
        }
        wmsShipmentFeign.saveAndCheckTransfer(ext);
    }
}

