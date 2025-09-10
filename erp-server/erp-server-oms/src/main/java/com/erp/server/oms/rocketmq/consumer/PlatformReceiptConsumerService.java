package com.erp.server.oms.rocketmq.consumer;

import cn.hutool.json.JSONUtil;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.PlatformProductDTO;
import com.common.business.dto.PlatformReceiptDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.SoReceiptDetailEntity;
import com.erp.model.oms.entity.SoReceiptEntity;
import com.erp.server.oms.service.CustomerInfoService;
import com.erp.server.oms.service.SoReceiptDetailService;
import com.erp.server.oms.service.SoReceiptService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 收款单
 *
 */
@Service
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_PLATFORM_RECEIPT_TO_OMS_TOPIC,
        selectorExpression = RocketMqNewTag.DMP_PLATFORM_RECEIPT_TO_OMS_TAG,
        consumerGroup = RocketMqNewConsumerGroup.DMP_PLATFORM_RECEIPT_TO_OMS_GROUP)
@Slf4j
public class PlatformReceiptConsumerService extends AbstractNewPlatformConsumerHandler{

	@Resource
	private SoReceiptService soReceiptService;

	@Resource
	private SoReceiptDetailService soReceiptDetailService;

	@Resource
	private CustomerInfoService customerInfoService;

	@Override
	public String getBizName() {
		return "收款单";
	}
	
    @Override
	@Transactional(rollbackFor = Exception.class)
	public void handle(String data) {
		PlatformReceiptDTO dto = JSONUtil.toBean(data.toString(), PlatformReceiptDTO.class);
		if(dto == null) {
			log.error("PlatformReceiptConsumerService.handle 收款单消费失败，参数为空");
			return;
		}
		CustomerInfoEntity customerInfo = customerInfoService.getCustomerByCode(dto.getCustomerCode());
		//查询是否存在
		SoReceiptEntity exist = soReceiptService.getByThirdSystemAndCode(dto.getThirdSystem(),dto.getCode());
		List<SoReceiptDetailEntity> existList;
		if(exist != null) {
			existList = soReceiptDetailService.listByMainIds(Arrays.asList((exist.getId())));
			//如果是作废，erp单据也要作废
			if(dto.getIsInvalid()){
				if(exist.getInvalidStatus()){
					log.warn("PlatformReceiptConsumerService.handle 收款单已作废，参数：{}",data);
					return;
				}
				exist.setInvalidStatus(true);
				exist.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT);
				soReceiptService.updateById(exist);
				return;
			}
			//存在判断是否有字段变更
			boolean hasChange = judgeHasChange(exist,existList,dto,customerInfo);
		}else{
			//如果是作废，直接跳过
			if(dto.getIsInvalid()){
				return;
			}
			//新增单据
		}

	}

	private boolean judgeHasChange(SoReceiptEntity exist, List<SoReceiptDetailEntity> existList, PlatformReceiptDTO dto,CustomerInfoEntity customerInfo) {
		//校验主表字段
		return true;
	}

}
