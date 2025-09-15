package com.erp.server.oms.rocketmq.consumer;

import cn.hutool.json.JSONUtil;
import com.common.business.dto.PlatformB2bOrderDTO;
import com.common.business.dto.PlatformReceiptDTO;
import com.common.business.dto.PlatformReceiptDetailDTO;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractRestCloudPlatformConsumerHandler;
import com.erp.model.oms.entity.BankAccountEntity;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.server.oms.service.*;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * b2b销售单
 *
 */
@Service
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_PLATFORM_RECEIPT_TO_OMS_TOPIC,
        selectorExpression = RocketMqNewTag.DMP_PLATFORM_RECEIPT_TO_OMS_TAG,
        consumerGroup = RocketMqNewConsumerGroup.DMP_PLATFORM_RECEIPT_TO_OMS_GROUP)
@Slf4j
public class PlatformB2bOrderConsumerService extends AbstractRestCloudPlatformConsumerHandler {

	@Resource
	private CustomerInfoService customerInfoService;

	@Resource
	private DictBasicService dictBasicService;

	@Resource
	private BankAccountService bankAccountService;

	@Resource
	private SoInfoService soInfoService;

	@Override
	public String getBizName() {
		return "b2b销售订单";
	}

    @Override
	@Transactional(rollbackFor = Exception.class)
	public void handle(String data) {
		PlatformB2bOrderDTO dto = JSONUtil.toBean(data.toString(), PlatformB2bOrderDTO.class);
		if(dto == null) {
			log.error("  b2b订单消费失败，参数为空");
			return;
		}
		this.fillDTO(dto);

	}


	private void fillDTO(PlatformB2bOrderDTO dto) {

	}


}
