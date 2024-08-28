package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.erp.model.wms.entity.OtherInstockEntity;
import com.erp.model.wms.entity.SoReturnInstockEntity;
import com.erp.server.wms.service.OtherInstockService;
import com.erp.server.wms.service.SoReturnInstockService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import com.erp.model.dmp.dto.DmpSoPrestockInfoDTO.PrestockDTO;

import javax.annotation.Resource;

@Component
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_WDT_PRE_STOCK_TO_WMS_TOPIC,
selectorExpression = RocketMqNewTag.DMP_WDT_PRE_STOCK_TO_WMS_TAG,
consumerGroup = RocketMqNewConsumerGroup.DMP_WDT_PRE_STOCK_TO_WMS_GROUP)
public class SyncNewWdtPreStockConsumer extends AbstractNewPlatformConsumerHandler {

	@Resource
	private OtherInstockService otherInstockService;

    @Override
	public String getBizName() {
		return "旺店通预入库";
	}

	@Override
	public void handle(String data) {
		PrestockDTO dto = JSON.parseObject(data,  PrestockDTO.class);
		//保存数据
		otherInstockService.syncWdtPreInstock(dto);
	}
	
}