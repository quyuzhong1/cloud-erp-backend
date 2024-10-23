package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.json.JSONUtil;
import com.common.business.dto.PlatformReturnInstockDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.wms.entity.*;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.service.OverseasProviderWarehouseService;
import com.erp.server.wms.service.SoOutstockService;
import com.erp.server.wms.service.SoReturnInstockService;
import com.erp.server.wms.service.WarehouseService;
import io.seata.common.util.CollectionUtils;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 下载平台退货入库数据消费服务
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_PLATFORM_RETURN_INSTOCK_TO_WMS_TOPIC,
selectorExpression = RocketMqNewTag.DMP_PLATFORM_RETURN_INSTOCK_TO_WMS_TAG,
consumerGroup = RocketMqNewConsumerGroup.DMP_PLATFORM_RETURN_INSTOCK_TO_WMS_GROUP,
consumeMode = ConsumeMode.ORDERLY)
public class PlatformNewReturnInstockConsumerService extends AbstractNewPlatformConsumerHandler {

	@Resource
	private SoReturnInstockService soReturnInstockService;

	@Resource
	private SkuMappingFeign skuMappingFeign;

	@Resource
	private WarehouseService warehouseService;

	@Resource
	private SysUserFeign sysUserFeign;

	@Resource
	private CustomerFeign customerFeign;

	@Resource
	private ShopInfoFeign shopInfoFeign;

	@Resource
	private SoOutstockService soOutstockService;

	@Resource
	private OverseasProviderWarehouseService overseasProviderWarehouseService;

	@Resource
	private SoB2cFeign soB2cFeign;

	@Override
	public String getBizName() {
		return "平台退货入库";
	}

	@Override
	public void handle(String data) {
		PlatformReturnInstockDTO dto = JSONUtil.toBean(data, PlatformReturnInstockDTO.class);
		if(Objects.isNull(dto) || StringUtils.isBlank(dto.getAuthId())|| StringUtils.isBlank(dto.getWarehouseCode())){
			return;
		}

		SoReturnInstockEntity existEntity = soReturnInstockService.getByThirdCode(dto.getPlatformReturnOrderNo());
		if(Objects.nonNull(existEntity)){
			return;
		}
		OverseasProviderWarehouseEntity overseasProviderWarehouseEntity = overseasProviderWarehouseService.getByPlatform(dto.getAuthId(),dto.getWarehouseCode());
		if(Objects.isNull(overseasProviderWarehouseEntity) || StringUtils.isBlank(overseasProviderWarehouseEntity.getWarehouseId())){
			throw new ServiceException("仓库信息不存在");
		}
		SoB2cEntity soB2cEntity = null;
		SoOutstockEntity soOutstock = null;
		WarehouseEntity warehouseEntity = warehouseService.getById(overseasProviderWarehouseEntity.getWarehouseId());
		if(StringUtils.isNotBlank(dto.getOrderReferenceNo())){
			soB2cEntity = soB2cFeign.getSoCode(dto.getOrderReferenceNo());
			if(Objects.nonNull(soB2cEntity)){
				soOutstock = soOutstockService.getBySoId(soB2cEntity.getId());
			}
		}
		SoReturnInstockEntity soReturnInstockEntity = this.buildSoReturnInstockEntity(dto,warehouseEntity,soB2cEntity,soOutstock);
		List<SoReturnInstockDetailEntity> detailEntityList = this.buildSoReturnInstockDetail(dto,soReturnInstockEntity,warehouseEntity);
		if(CollectionUtils.isEmpty(detailEntityList)){
			throw new ServiceException("没有映射");
		}
		//关联销售退货单
		this.matchSoReturn(soReturnInstockEntity,detailEntityList,dto,soB2cEntity);

		soReturnInstockService.addByThirdWarehouse(soReturnInstockEntity,detailEntityList);
	}

	/**
	 * 匹配退货订单
	 * @param soReturnInstockEntity
	 * @param detailEntityList
	 * @param dto
	 * @param soB2cEntity
	 */
	private void matchSoReturn(SoReturnInstockEntity soReturnInstockEntity, List<SoReturnInstockDetailEntity> detailEntityList, PlatformReturnInstockDTO dto,SoB2cEntity soB2cEntity) {
		if(Objects.isNull(soB2cEntity) || StringUtils.isBlank(soB2cEntity.getPlatformCode())){
			return;
		}
		List<SoB2cReturnEntity> soB2cReturnEntityList = FeignQuery.create(SoB2cReturnEntity.class).eq(SoB2cReturnEntity::getPlatformOrderNo,soB2cEntity.getPlatformCode()).list();
		if(CollectionUtils.isEmpty(soB2cReturnEntityList)){
			return;
		}
		List<String> soB2cReturnIds = soB2cReturnEntityList.stream().map(v->v.getId()).collect(Collectors.toList());
		List<SoB2cReturnDetailEntity> soB2cReturnDetailEntityList = FeignQuery.create(SoB2cReturnDetailEntity.class).in(SoB2cReturnDetailEntity::getMainId,soB2cReturnIds).list();
		if(CollectionUtils.isEmpty(soB2cReturnDetailEntityList)){
			return;
		}
		for (SoB2cReturnEntity soB2cReturnEntity : soB2cReturnEntityList) {
			List<SoB2cReturnDetailEntity> currentDetailList = soB2cReturnDetailEntityList.stream().filter(v->v.getMainId().equals(soB2cReturnEntity.getId())).collect(Collectors.toList());
			if(CollectionUtils.isEmpty(currentDetailList)){
				continue;
			}
			boolean isMatch = false;
			for (SoReturnInstockDetailEntity soReturnInstockDetailEntity : detailEntityList) {
				SoB2cReturnDetailEntity soB2cReturnDetailEntity = currentDetailList.stream().filter(v->v.getSkuId().equals(soReturnInstockDetailEntity.getSkuId())).findFirst().orElse(null);
				if(Objects.isNull(soB2cReturnDetailEntity)){
					continue;
				}
				soReturnInstockDetailEntity.setSoReturnDetailId(soB2cReturnDetailEntity.getId());
				isMatch = true;
			}
			if(isMatch){
				soReturnInstockEntity.setSoReturnId(soB2cReturnEntity.getId());
				soReturnInstockEntity.setSoReturnCode(soB2cReturnEntity.getCode());
				return;
			}
		}
	}

	private List<SoReturnInstockDetailEntity> buildSoReturnInstockDetail(PlatformReturnInstockDTO dto, SoReturnInstockEntity soReturnInstockEntity,WarehouseEntity warehouseEntity) {
		List<PlatformReturnInstockDTO.Detail> details = dto.getProductDetailList();
		if(CollectionUtils.isEmpty(details)){
			throw new ServiceException("明细为空");
		}
		List<String> platformSkuNoList = dto.getProductDetailList().stream().map(v->v.getProductSku()).collect(Collectors.toList());
		ListingInfoParamDTO listingInfoParamDTO = new ListingInfoParamDTO();
		listingInfoParamDTO.setPlatformSkuNoList(platformSkuNoList);
		listingInfoParamDTO.setAuthId(dto.getAuthId());
		listingInfoParamDTO.setMatchResult(true);
		List<SkuMappingDTO.MappingSkuViewDTO> mappingSkuViewDTOList = skuMappingFeign.listByPlatformSkuNoAndPlatform(listingInfoParamDTO);
		List<SoReturnInstockDetailEntity> detailEntityList = new ArrayList<>();
		for (PlatformReturnInstockDTO.Detail detail : details) {
			SkuMappingDTO.MappingSkuViewDTO skuViewDTO = mappingSkuViewDTOList.stream().filter(v->v.getPlatformSkuNo().equals(detail.getProductSku())).findFirst().orElse(null);
			if(Objects.isNull(skuViewDTO)){
				continue;
			}
			SoReturnInstockDetailEntity soReturnInstockDetailEntity = new SoReturnInstockDetailEntity();
			soReturnInstockDetailEntity.setSkuId(skuViewDTO.getProductSkuId());
			soReturnInstockDetailEntity.setSkuNo(skuViewDTO.getProductSkuNo());
			soReturnInstockDetailEntity.setMustQty(detail.getMustQty());
			soReturnInstockDetailEntity.setReceiveQty(detail.getReceiveQty());
			soReturnInstockDetailEntity.setRealQty(detail.getRealQty());
			soReturnInstockDetailEntity.setWarehouseId(warehouseEntity.getId());
			soReturnInstockDetailEntity.setWarehouseName(warehouseEntity.getName());
			soReturnInstockDetailEntity.setRemark(dto.getReason());
			soReturnInstockDetailEntity.setReturnTypeDict(dto.getReturnType());
			detailEntityList.add(soReturnInstockDetailEntity);
		}
		return detailEntityList;
	}

	private SoReturnInstockEntity buildSoReturnInstockEntity(PlatformReturnInstockDTO dto,WarehouseEntity warehouseEntity,SoB2cEntity soB2cEntity,SoOutstockEntity soOutstock){
		SoReturnInstockEntity soReturnInstockEntity = new SoReturnInstockEntity();
		soReturnInstockEntity.setApproveTime(LocalDateTime.now());
		soReturnInstockEntity.setApproveStatus(ApproveStatusEnum.APPROVE_ING.getStatus());
		soReturnInstockEntity.setBillDate(dto.getPutawayTime().toLocalDate());
		soReturnInstockEntity.setInventoryOrgId(warehouseEntity.getOrgId());
		//组织信息
		SysAccountingCompanyEntity company = sysUserFeign.getCompanyById(warehouseEntity.getOrgId());
		soReturnInstockEntity.setInventoryOrgName(company.getCompanyName());
		soReturnInstockEntity.setWarehouseKeeperId(warehouseEntity.getChargeId());
		soReturnInstockEntity.setApproveUserName("system");
		soReturnInstockEntity.setSourceCode(dto.getPlatformReturnOrderNo());
		soReturnInstockEntity.setSourceType(SourceTypeEnum.THIRD_WAREHOUSE_RETURN_INSTOCK.getCode());
		soReturnInstockEntity.setThirdCode(dto.getPlatformReturnOrderNo());
		soReturnInstockEntity.setCreated(dto.getCreateTime());
		if(Objects.nonNull(soB2cEntity)){
			soReturnInstockEntity.setType("B2C");
			soReturnInstockEntity.setSalesOrgId(soB2cEntity.getOrgId());
			soReturnInstockEntity.setSalesOrgName(soB2cEntity.getOrgName());
			ShopInfoEntity shopInfoEntity = shopInfoFeign.getShopInfoById(soB2cEntity.getShopId());
			CustomerInfoEntity customerInfo = customerFeign.getCustomerById(shopInfoEntity.getCustomerId());
			soReturnInstockEntity.setCustomerId(shopInfoEntity.getCustomerId());
			soReturnInstockEntity.setCustomerName(customerInfo.getName());
			soReturnInstockEntity.setSoCode(soB2cEntity.getCode());
			soReturnInstockEntity.setSoId(soB2cEntity.getId());
		}
		if(Objects.nonNull(soOutstock)){
			SysDepartmentDTO department = sysUserFeign.getUserDeptById(soOutstock.getSalesDeptId());
			soReturnInstockEntity.setSalesDeptId(soOutstock.getSalesDeptId());
			soReturnInstockEntity.setSalesDeptName(department.getName());
			soReturnInstockEntity.setSellerId(soOutstock.getSellerId());
			soReturnInstockEntity.setSellerName(soOutstock.getSellerName());
		}
		return soReturnInstockEntity;
	}
}