package com.erp.server.dmp.service.mq;

import cn.hutool.json.JSONUtil;
import com.common.business.dto.PlatformThirdLogisticsChannelDTO;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import com.erp.model.dmp.entity.DmpLogisticsChannelEntity;
import com.erp.model.dmp.entity.ThirdLogisticsEntity;
import com.erp.server.dmp.service.ThirdLogisticsService;
import com.erp.server.dmp.service.ThirdShopService;
import com.sdk.wangdian.dto.ErpShopDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Component
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_THIRD_LOGISTICS_TO_DMP_TOPIC,
selectorExpression = RocketMqNewTag.DMP_THIRD_LOGISTICS_TO_DMP_TAG,
consumerGroup = RocketMqNewConsumerGroup.DMP_THIRD_LOGISTICS_TO_DMP_GROUP)
public class NewThirdLogisticsConsumer extends AbstractNewPlatformConsumerHandler {

	@Resource
	private ThirdLogisticsService thirdLogisticsService;

	@Override
	public String getBizName() {
		return "第三方物流方式";
	}
	
	@Override
	public void handle(String data) {
		PlatformThirdLogisticsChannelDTO sourceDto = JSONUtil.toBean(data, PlatformThirdLogisticsChannelDTO.class);
		log.debug("第三方物流方式消费：{}", data);

		ThirdLogisticsEntity oldEntity = thirdLogisticsService.lambdaQuery()
				.eq(ThirdLogisticsEntity::getPlatformType, sourceDto.getPlatformType())
				.eq(ThirdLogisticsEntity::getLogisticsTypeId, sourceDto.getLogisticsTypeId())
				.eq(ThirdLogisticsEntity::getLogisticsSupplierId, sourceDto.getLogisticsSupplierId())
				.eq(ThirdLogisticsEntity::getType, sourceDto.getType())
				.last(" LIMIT 1")
				.one();
		if (null == oldEntity){
			ThirdLogisticsEntity newEntity = new ThirdLogisticsEntity();
			BeanUtils.copyProperties(sourceDto, newEntity);
			boolean save = thirdLogisticsService.save(newEntity);
			if (!save){
				log.error("第三方物流方式保存失败:{}", JSONUtil.toJsonStr(newEntity));
			}
		} else {
			BeanUtils.copyProperties(sourceDto, oldEntity);
			boolean result = thirdLogisticsService.updateById(oldEntity);
			if (!result){
				log.error("第三方物流方式更新失败:{}", JSONUtil.toJsonStr(oldEntity));
			}
		}
	}
}