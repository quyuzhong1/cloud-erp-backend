package com.erp.server.oms.rocketmq.consumer;

import cn.hutool.json.JSONUtil;
import com.common.business.dto.PlatformReceiptDTO;
import com.common.business.dto.PlatformReceiptDetailDTO;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractRestCloudPlatformConsumerHandler;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.server.oms.service.*;
import com.google.common.collect.Lists;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 收款单
 *
 */
@Service
@RocketMQMessageListener(topic = RocketMqNewTopic.RESTCLOUD_PLATFORM_RECEIPT_TO_OMS_TOPIC,
        selectorExpression = RocketMqNewTag.RESTCLOUD_PLATFORM_RECEIPT_TO_OMS_TAG,
        consumerGroup = RocketMqNewConsumerGroup.RESTCLOUD_PLATFORM_RECEIPT_TO_OMS_GROUP)
@Slf4j
public class PlatformReceiptConsumerService extends AbstractRestCloudPlatformConsumerHandler {

	@Resource
	private SoReceiptService soReceiptService;

	@Resource
	private SoReceiptDetailService soReceiptDetailService;

	@Resource
	private CustomerInfoService customerInfoService;

	@Resource
	private DictBasicService dictBasicService;

	@Resource
	private BankAccountService bankAccountService;

	@Resource
	private SoInfoService soInfoService;

	@Resource
	private OperateLogService operateLogService;

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
		this.fillDTO(dto);
		if(StringUtils.isBlank(dto.getErpCustomerId())){
			log.warn("PlatformReceiptConsumerService.handle 收款单消费失败，客户编码{}未找到对应客户",dto.getCustomerCode());
			return;
		}
		soReceiptService.handlePlatformConsumer(dto);

	}


	private void fillDTO(PlatformReceiptDTO dto) {

		CustomerInfoEntity customerInfo = customerInfoService.getCustomerByCode(dto.getCustomerCode());
		dto.setErpCustomerId(Objects.isNull(customerInfo)?"":customerInfo.getId());
		dto.setErpSaleOrgId(Objects.isNull(customerInfo)?"":customerInfo.getUseOrgId());
		List<String> dictKeys = Lists.newArrayList(DictBasicTypeEnum.RECEIVE_METHOD.getType(),DictBasicTypeEnum.DHT_ACCOUNT_TYPE.getType());
		List<DictBasicEntity> dictBasicEntityList = dictBasicService.getByKeyList(dictKeys);
		Map<String, List<DictBasicEntity>> dictBasicMap = dictBasicEntityList.stream().collect(Collectors.groupingBy(DictBasicEntity::getType));
		List<BankAccountEntity> bankAccountList = bankAccountService.list();
		// 收款方式
		List<DictBasicEntity> receiveMethodList = dictBasicMap.get(DictBasicTypeEnum.RECEIVE_METHOD.getType());
		// 账户类型
		List<DictBasicEntity> accountTypeList = dictBasicMap.get(DictBasicTypeEnum.DHT_ACCOUNT_TYPE.getType());

		BankAccountEntity bankAccountEntity = bankAccountList.stream().filter(b -> b.getAccountName().equals(dto.getReceiptAccount())).findFirst().orElse(null);
		if(bankAccountEntity != null){
			dto.setErpReceiptAccountId(bankAccountEntity.getId());
		}else{
			dto.setErpReceiptAccountId("");
		}

		DictBasicEntity receiveMethod = receiveMethodList.stream().filter(d -> d.getName().equals(dto.getReceiptMethod())).findFirst().orElse(null);
		if(receiveMethod != null){
			dto.setErpReceiptMethod(receiveMethod.getValue());
		}else{
			dto.setErpReceiptMethod("");
		}
		DictBasicEntity accountType = accountTypeList.stream().filter(d -> d.getValue().equals(dto.getPostedAccountId())).findFirst().orElse(null);
		if(accountType != null){
			dto.setErpPostedAccount(accountType.getName());
		}else{
			dto.setErpPostedAccount("");
		}

		List<PlatformReceiptDetailDTO> platformReceiptDetailDTOList = dto.getDetail();
		//过滤掉作废的
		if(CollectionUtils.isNotEmpty(platformReceiptDetailDTOList)){
			List<PlatformReceiptDetailDTO> filterList = platformReceiptDetailDTOList.stream().filter(d -> !d.getIsInvalid()).collect(Collectors.toList());
			List<String> soCodes = filterList.stream().map(PlatformReceiptDetailDTO::getSoCode).distinct().collect(Collectors.toList());
			List<SoInfoEntity> soInfoList = soInfoService.listByPlatformOrderCodes(soCodes,dto.getThirdSystem());
			Map<String, SoInfoEntity> soInfoMap = soInfoList.stream().collect(Collectors.toMap(SoInfoEntity::getPlatformOrderCode, e->e,(v1,v2)->v1));
			for(PlatformReceiptDetailDTO detailDTO : filterList){
				SoInfoEntity soInfo = soInfoMap.get(detailDTO.getSoCode());
				if(soInfo != null){
					detailDTO.setErpSoId(soInfo.getId());
					detailDTO.setSoCode(soInfo.getCode());
				}else{
					detailDTO.setErpSoId("");
					detailDTO.setSoCode("");
				}
			}

			dto.setDetail(filterList);
		}
	}


}
