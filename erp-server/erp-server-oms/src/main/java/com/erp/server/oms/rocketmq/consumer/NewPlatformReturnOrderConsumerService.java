package com.erp.server.oms.rocketmq.consumer;

import cn.hutool.json.JSONUtil;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.dto.PlatformRefundOrderDTO;
import com.common.business.dto.PlatformReturnOrderDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.oms.enums.SoB2cReturnSourceTypeEnum;
import com.erp.model.oms.enums.SoB2cReturnStatusEnum;
import com.erp.model.oms.enums.SoB2cReturnTypeEnum;
import com.erp.model.wms.dto.SoReturnInstockDetailDTO;
import com.erp.model.wms.entity.SoReturnInstockDetailEntity;
import com.erp.model.wms.entity.SoReturnInstockEntity;
import com.erp.rpc.wms.feign.SoReturnInstockFeign;
import com.erp.server.oms.service.*;
import io.seata.common.util.CollectionUtils;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 平台退货入库单消费者
 * @author Cloud
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_PLATFORM_RETURN_ORDER_TO_OMS_TOPIC,
selectorExpression = RocketMqNewTag.DMP_PLATFORM_RETURN_ORDER_TO_OMS_TAG,
consumerGroup = RocketMqNewConsumerGroup.DMP_PLATFORM_RETURN_ORDER_TO_OMS_GROUP,
consumeMode = ConsumeMode.ORDERLY)
public class NewPlatformReturnOrderConsumerService extends AbstractNewPlatformConsumerHandler{

	@Resource
	private SoB2cReturnService soB2cReturnService;

	@Resource
	private SoB2cService soB2cService;

	@Resource
	private SoReturnInstockFeign soReturnInstockFeign;

	@Resource
	private SoB2cDetailService soB2cDetailService;

	@Resource
	private SkuMappingService skuMappingService;

	@Override
	public String getBizName() {
		return "平台退货订单";
	}
	
    @Override
	public void handle(String data) {
		log.debug("平台退货单消费:{}", data);
		PlatformReturnOrderDTO dto = JSONUtil.toBean(data, PlatformReturnOrderDTO.class);
		if(Objects.isNull(dto)){
			return;
		}
		SoB2cReturnEntity exist = soB2cReturnService.getByPlatformReturnCode(dto.getPlatformReturnNo());
		if(Objects.nonNull(exist)){
			return;
		}
		if(CollectionUtils.isEmpty(dto.getDetailList())){
			return;
		}
		SoB2cEntity soB2cEntity = null;
		List<SoB2cDetailEntity> soB2cDetailEntityList = new ArrayList<>();
		if(StringUtils.isNotBlank(dto.getPlatformOrderNo())){
			List<SoB2cEntity> soB2cEntityList = soB2cService.getByPlatformCode(dto.getPlatformOrderNo());
			//过滤手工单
			soB2cEntityList = soB2cEntityList.stream().filter(v-> !SourceTypeEnum.SELF_ADD.getCode().equals(v.getSourceType())).collect(Collectors.toList());
			if(CollectionUtils.isNotEmpty(soB2cEntityList)){
				soB2cEntity = soB2cEntityList.get(0);
			}
			List<String> soIds = soB2cEntityList.stream().map(v->v.getId()).collect(Collectors.toList());
			soB2cDetailEntityList = soB2cDetailService.listByMainIds(soIds);
		}
		if(Objects.isNull(soB2cEntity)){
			return;
		}
		SoB2cReturnEntity soB2cReturnEntity = this.buildReturn(dto,soB2cEntity);
		List<SoB2cReturnDetailEntity> soB2cReturnDetailEntityList = this.buildRefundDetail(dto,soB2cDetailEntityList);
		soB2cReturnService.addByPlatform(soB2cReturnEntity,soB2cReturnDetailEntityList);

		//关联销售退货入库单
		this.matchSoReturnInstock(soB2cReturnEntity,soB2cReturnDetailEntityList,dto,soB2cEntity);
	}

	/**
	 * 匹配退货入库单
	 * @param soB2cReturnEntity
	 * @param soB2cReturnDetailEntityList
	 * @param dto
	 * @param soB2cEntity
	 */
	private void matchSoReturnInstock(SoB2cReturnEntity soB2cReturnEntity, List<SoB2cReturnDetailEntity> soB2cReturnDetailEntityList, PlatformReturnOrderDTO dto, SoB2cEntity soB2cEntity) {
		if(StringUtils.isBlank(soB2cReturnEntity.getSoId())){
			return;
		}
		List<SoReturnInstockEntity> soReturnInstockEntityList = FeignQuery.create(SoReturnInstockEntity.class).eq(SoReturnInstockEntity::getSoId,soB2cReturnEntity.getSoId()).eq(SoReturnInstockEntity::getSoReturnId,"").list();
		if(CollectionUtils.isEmpty(soReturnInstockEntityList)){
			return;
		}
		List<String> soReturnIds = soB2cReturnDetailEntityList.stream().map(v->v.getId()).collect(Collectors.toList());
		List<SoReturnInstockDetailEntity> soReturnInstockDetailEntityList = FeignQuery.create(SoReturnInstockDetailEntity.class).in(SoReturnInstockDetailEntity::getMainId,soReturnIds).eq(SoReturnInstockDetailEntity::getSoReturnDetailId,"").list();
		if(CollectionUtils.isEmpty(soReturnInstockDetailEntityList)){
			return;
		}
		List<SoReturnInstockEntity> updateList = new ArrayList<>();
		List<SoReturnInstockDetailEntity> updateDetailList = new ArrayList<>();
		for (SoB2cReturnDetailEntity soB2cReturnDetailEntity : soB2cReturnDetailEntityList) {
			SoReturnInstockDetailEntity matched = soReturnInstockDetailEntityList.stream().filter(v->StringUtils.isBlank(v.getSoReturnDetailId()) && v.getSkuId().equals(soB2cReturnDetailEntity.getSkuId())).findFirst().orElse(null);
			if(Objects.isNull(matched)){
				continue;
			}
			SoReturnInstockEntity matchedMain = soReturnInstockEntityList.stream().filter(v->v.getId().equals(matched.getMainId())).findFirst().orElse(null);
			if(Objects.isNull(matchedMain)){
				continue;
			}
			matched.setSoReturnDetailId(soB2cReturnDetailEntity.getId());
			updateDetailList.add(matched);
			matchedMain.setSoReturnId(soB2cReturnEntity.getId());
			updateList.add(matchedMain);
		}
		if(CollectionUtils.isNotEmpty(updateList) && CollectionUtils.isNotEmpty(updateDetailList)){
			soReturnInstockFeign.clearSoReturnAndUpdate(new SoReturnInstockDetailDTO.ClearSoReturnAndUpdateDTO(new ArrayList<>(),updateList,updateDetailList));
		}
	}

	private List<SoB2cReturnDetailEntity> buildRefundDetail(PlatformReturnOrderDTO dto, List<SoB2cDetailEntity> soB2cDetailEntityList) {
		List<SoB2cReturnDetailEntity> list = new ArrayList<>();
		for (PlatformReturnOrderDTO.Detail detail : dto.getDetailList()) {
			SoB2cReturnDetailEntity refundOrderDetailEntity = new SoB2cReturnDetailEntity();
			SoB2cDetailEntity soB2cDetailEntity = soB2cDetailEntityList.stream().filter(v->v.getPlatformSkuNo().equals(detail.getPlatformSkuNo())).findFirst().orElse(new SoB2cDetailEntity());
			refundOrderDetailEntity.setSkuId(soB2cDetailEntity.getSkuId());
			refundOrderDetailEntity.setSkuNo(soB2cDetailEntity.getSkuNo());
			refundOrderDetailEntity.setPlatformSkuNo(detail.getPlatformSkuNo());
			refundOrderDetailEntity.setSaleQty(soB2cDetailEntity.getQty());
			refundOrderDetailEntity.setReturnQty(detail.getReturnQty());
			refundOrderDetailEntity.setRemark(detail.getRemark());
			refundOrderDetailEntity.setSoDetailId(soB2cDetailEntity.getId());
			list.add(refundOrderDetailEntity);
		}
		return list;
	}

	private SoB2cReturnEntity buildReturn(PlatformReturnOrderDTO dto, SoB2cEntity soB2cEntity) {
		SoB2cReturnEntity soB2cReturnEntity = new SoB2cReturnEntity();
		soB2cReturnEntity.setPlatformOrderNo(dto.getPlatformOrderNo());
		soB2cReturnEntity.setPlatformReturnNo(dto.getPlatformReturnNo());
		soB2cReturnEntity.setSoId(soB2cEntity.getId());
		soB2cReturnEntity.setSoCode(soB2cEntity.getCode());
		soB2cReturnEntity.setDictPlatform(dto.getDictPlatform());
		soB2cReturnEntity.setShopId(soB2cEntity.getShopId());
		soB2cReturnEntity.setAmount(soB2cEntity.getAmount());
		soB2cReturnEntity.setCurrency(soB2cEntity.getCurrency());
		soB2cReturnEntity.setType(SoB2cReturnTypeEnum.CUSTOMER_RETURNS.code);
		soB2cReturnEntity.setReason(dto.getReason());
		soB2cReturnEntity.setStatus(SoB2cReturnStatusEnum.RETURNED.code);
		soB2cReturnEntity.setSysReturnTime(LocalDateTime.now());
		soB2cReturnEntity.setSourceType(SoB2cReturnSourceTypeEnum.AUTO_ADD.code);
		return soB2cReturnEntity;
	}

}