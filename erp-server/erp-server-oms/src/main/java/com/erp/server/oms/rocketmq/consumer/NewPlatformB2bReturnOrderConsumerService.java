package com.erp.server.oms.rocketmq.consumer;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.PlatformB2BReturnOrderDTO;
import com.common.business.dto.PlatformReturnOrderDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.OrderTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.BillTypeEnum;
import com.erp.model.oms.enums.ShopOrderRouteEnum;
import com.erp.model.oms.enums.SoB2cReturnSourceTypeEnum;
import com.erp.model.oms.enums.SoB2cReturnStatusEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.SysDepartmentUserNumberDTO;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.wms.dto.SoReturnInstockDetailDTO;
import com.erp.model.wms.entity.SoReturnInstockDetailEntity;
import com.erp.model.wms.entity.SoReturnInstockEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.ReturnReasonEnum;
import com.erp.model.wms.enums.ReturnTypeEnum;
import com.erp.rpc.dmp.feign.DmpThirdMappingFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.SoReturnInstockFeign;
import com.erp.server.oms.service.*;
import io.seata.common.util.CollectionUtils;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 平台退货入库单消费者
 * @author Cloud
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_PLATFORM_B2B_RETURN_ORDER_TO_OMS_TOPIC,
selectorExpression = RocketMqNewTag.DMP_PLATFORM_B2B_RETURN_ORDER_TO_OMS_TAG,
consumerGroup = RocketMqNewConsumerGroup.DMP_PLATFORM_B2B_RETURN_ORDER_TO_OMS_GROUP,
consumeMode = ConsumeMode.ORDERLY)
public class NewPlatformB2bReturnOrderConsumerService extends AbstractNewPlatformConsumerHandler{

	@Resource
	private SoReturnService soReturnService;

	@Resource
	private SoInfoService soInfoService;

	@Resource
	private SoDetailService soDetailService;

	@Resource
	private ShopInfoService shopInfoService;

	@Resource
	private CustomerInfoService customerInfoService;

	@Resource
	private CustomerAddressService customerAddressService;

	@Resource
	private SysUserFeign sysUserFeign;

	@Resource
	private DmpThirdMappingFeign dmpThirdMappingFeign;

	@Resource
	private PlmTaskFeign plmTaskFeign;

	@Override
	public String getBizName() {
		return "平台B2B退货订单";
	}
	
    @Override
	public void handle(String data) {
		log.debug("平台B2B退货单消费:{}", data);
		PlatformB2BReturnOrderDTO dto = JSONUtil.toBean(data, PlatformB2BReturnOrderDTO.class);
		if(Objects.isNull(dto)){
			return;
		}
        SoReturnEntity exist = soReturnService.getByPlatformOrderCode(dto.getPlatformOrderCode());

		if(Objects.nonNull(exist)){
			log.warn("平台B2B退货单消费:订单已存在:{}", dto.getPlatformOrderCode());
			return;
		}
		if(CollectionUtils.isEmpty(dto.getDetailList())){
			return;
		}
		SoInfoEntity soInfo = null;
		if(StringUtils.isNotBlank(dto.getPlatformOrderCode())){
			List<SoInfoEntity> soInfoEntityList = soInfoService.getByPlatformOrderCode(dto.getPlatformOrderCode());
			if(CollectionUtils.isNotEmpty(soInfoEntityList)){
				soInfo = soInfoEntityList.get(0);
			}
		}

		SoReturnEntity soReturn = this.buildReturn(dto,soInfo);
		List<SoReturnDetailEntity> soB2cReturnDetailEntityList = this.buildRefundDetail(dto);
		soReturnService.addByPlatform(soReturn,soB2cReturnDetailEntityList);

	}

	private List<SoReturnDetailEntity> buildRefundDetail(PlatformB2BReturnOrderDTO dto) {
		List<SoReturnDetailEntity> soReturnDetailEntities = new ArrayList<>();
		List<PlatformB2BReturnOrderDTO.Detail> detailList = dto.getDetailList();
		List<String> skuNoList = detailList.stream().map(PlatformB2BReturnOrderDTO.Detail::getSkuNo).collect(Collectors.toList());
		List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(skuNoList);
		for (PlatformB2BReturnOrderDTO.Detail detail : detailList) {
			SoReturnDetailEntity soReturnDetailEntity = new SoReturnDetailEntity();
			SkuVO skuVO = skuVOList.stream().filter(sku -> sku.getSkuNo().equals(detail.getSkuNo())).findFirst().orElse(null);
			if(Objects.isNull(skuVO)){
				throw new ServiceException("未匹配到SKU，sku编号："+detail.getSkuNo());
			}
			soReturnDetailEntity.setSkuId(skuVO.getSkuId());
			soReturnDetailEntity.setSkuNo(skuVO.getSkuNo());
			soReturnDetailEntity.setPlatformSkuNo( detail.getPlatformSkuNo());
			soReturnDetailEntity.setReturnQty(detail.getReturnQty());
			soReturnDetailEntity.setReturnAmount(detail.getReturnAmount());
			soReturnDetailEntity.setTaxReturnAmount(detail.getReturnAmount());
			soReturnDetailEntity.setReturnAmountLocalCurrency(detail.getReturnAmount());
			soReturnDetailEntity.setTaxReturnAmountLocalCurrency(detail.getReturnAmount());
			soReturnDetailEntity.setExchangeRate(BigDecimal.ONE);
			soReturnDetailEntity.setReturnReasonDict(ReturnReasonEnum.OTHER.getCode());
			soReturnDetailEntity.setReturnTypeDict(detail.getReturnTypeDict());
			soReturnDetailEntity.setRemark(detail.getRemark());
			soReturnDetailEntities.add(soReturnDetailEntity);
		}
		return soReturnDetailEntities;
	}

	private SoReturnEntity buildReturn(PlatformB2BReturnOrderDTO dto, SoInfoEntity soInfo) {
		SoReturnEntity soReturn = new SoReturnEntity();
		//查询店铺
		String shopId = dto.getShopId();
		ShopInfoEntity shopInfo = shopInfoService.getById(shopId);
		if(!ShopOrderRouteEnum.B2B.getCode().equals(shopInfo.getOrderRouteType())){
			throw new ServiceException("非b2b订单，不处理");
		}
		String customerId = shopInfo.getCustomerId();
		CustomerInfoEntity customerInfo = customerInfoService.getById(customerId);
		if(Objects.isNull(customerInfo)){
			throw new ServiceException("未匹配到客户，客户id："+customerId);
		}
		CustomerAddressEntity customerAddressEntity = customerAddressService.getDefaultAddrByMainId(customerInfo.getId());
		ThirdMappingDTO.ViewParamDTO viewParamDTO = new ThirdMappingDTO.ViewParamDTO();
		viewParamDTO.setThirdId(dto.getPlatformWarehouseId());
		viewParamDTO.setType(ThirdSysTypeEnum.WAREHOUSE.getCode());
		viewParamDTO.setSysType(PlatformDictEnum.WDT.getCode());
		List<ThirdMappingEntity> thirdMappingEntityList = dmpThirdMappingFeign.getByThirdId(viewParamDTO);
		if(CollectionUtils.isEmpty(thirdMappingEntityList)){
			throw new ServiceException("未匹配到仓库映射关系，平台仓库id："+dto.getPlatformWarehouseId());
		}
		ThirdMappingEntity thirdMappingEntity = thirdMappingEntityList.get(0);
		WarehouseEntity warehouseEntity = FeignQuery.getById(WarehouseEntity.class,thirdMappingEntity.getSysId());

		soReturn.setType(OrderTypeEnum.B2B.getCode());
		if(Objects.nonNull(soInfo)){
			soReturn.setSourceType(SourceTypeEnum.SO_INFO.getCode());
			soReturn.setSourceId(soInfo.getId());
			soReturn.setSourceCode(soInfo.getCode());
		}
		soReturn.setBillDate(dto.getBillDate());
		soReturn.setThirdCode(dto.getThirdCode());
		soReturn.setPlatformOrderCode(dto.getPlatformOrderCode());
		soReturn.setSalesOrgId(customerInfo.getUseOrgId());
		soReturn.setSalesOrgName(customerInfo.getUseOrgName());
		if(StringUtils.isNotBlank(customerInfo.getSellerId())){
			SysDepartmentUserNumberDTO deptByUserId = sysUserFeign.getDeptByUserId(customerInfo.getSellerId());
			soReturn.setSalesDeptId(deptByUserId.getDepartmentId());
			soReturn.setSalesDeptName(deptByUserId.getDepartmentName());
		}
		soReturn.setSellerId(customerInfo.getSellerId());
		soReturn.setSellerName(customerInfo.getSellerName());
		soReturn.setWarehouseId(thirdMappingEntity.getSysId());
		soReturn.setWarehouseName(thirdMappingEntity.getSysName());
		soReturn.setInventoryOrgId(warehouseEntity.getOrgId());
		//获取核算公司
		SysAccountingCompanyEntity companyEntity = sysUserFeign.getCompanyById(warehouseEntity.getOrgId());
		if (ObjectUtil.isNotEmpty(companyEntity)) {
			soReturn.setInventoryOrgName(companyEntity.getCompanyName());
		}
		soReturn.setReturnLogisticCode(dto.getReturnLogisticCode());
		soReturn.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
		soReturn.setCurrencySymbol(CurrencyEnum.CNY.getCurrencySymbol());
		soReturn.setCustomerId(customerInfo.getId());
		soReturn.setCustomerName(customerInfo.getName());
		soReturn.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
		if(Objects.nonNull(customerAddressEntity)){
			soReturn.setReceiverName(customerAddressEntity.getPerson());
			soReturn.setReceiveAddress(customerAddressEntity.getAddress());
			soReturn.setTelNumber(customerAddressEntity.getTelNumber());
		}
		return soReturn;
	}


}