package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.PlatformReturnInstockDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ShopInfoDTO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.oms.enums.ListingMatchResultEnum;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.wms.entity.*;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.service.*;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.checkerframework.checker.units.qual.A;
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

	@Resource
	private SoOutstockDetailService soOutstockDetailService;

	@Override
	public String getBizName() {
		return "平台退货入库";
	}

	@Override
	public void handle(String data) {
		PlatformReturnInstockDTO dto = JSONUtil.toBean(data, PlatformReturnInstockDTO.class);
		if (PlatformDictEnum.AMAZON.getCode().equalsIgnoreCase(dto.getPlatform())){
			// 平台仓入库处理
			platformWarehouseHandle(dto);
		} else {
			// 海外仓入库处理
			overseasWarehouseHandle(dto);
		}
	}

	/**
	 * 海外仓入库平台处理
	 */
	public void overseasWarehouseHandle(PlatformReturnInstockDTO dto) {
		if(Objects.isNull(dto) || CharSequenceUtil.isBlank(dto.getAuthId())|| CharSequenceUtil.isBlank(dto.getWarehouseCode())){
			return;
		}

		SoReturnInstockEntity existEntity = soReturnInstockService.getByThirdCode(dto.getPlatformReturnOrderNo());
		if(Objects.nonNull(existEntity)){
			return;
		}
		OverseasProviderWarehouseEntity overseasProviderWarehouseEntity = overseasProviderWarehouseService.getByPlatform(dto.getAuthId(),dto.getWarehouseCode());
		if(Objects.isNull(overseasProviderWarehouseEntity) || CharSequenceUtil.isBlank(overseasProviderWarehouseEntity.getWarehouseId())){
			throw new ServiceException(ApiError.NOT_EXIST,"仓库信息");
		}
		SoB2cEntity soB2cEntity = null;
		SoOutstockEntity soOutstock = null;
		WarehouseEntity warehouseEntity = warehouseService.getById(overseasProviderWarehouseEntity.getWarehouseId());
		if(CharSequenceUtil.isNotBlank(dto.getOrderReferenceNo())){
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
		if(Objects.isNull(soB2cEntity) || CharSequenceUtil.isBlank(soB2cEntity.getPlatformCode())){
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
				soReturnInstockEntity.setPlatformOrderCode(Objects.nonNull(soB2cEntity) ? soB2cEntity.getPlatformCode() : "");
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
		listingInfoParamDTO.setMatchResult(ListingMatchResultEnum.TRUE.getCode());
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
		soReturnInstockEntity.setSourceCode(dto.getOrderReferenceNo());
		soReturnInstockEntity.setSourceType(SourceTypeEnum.THIRD_WAREHOUSE_RETURN_INSTOCK.getCode());
		soReturnInstockEntity.setThirdCode(dto.getPlatformReturnOrderNo());
		soReturnInstockEntity.setCreated(dto.getCreateTime());
		soReturnInstockEntity.setType("B2C");
		if(Objects.nonNull(soB2cEntity)){
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


	/**
	 * 平台仓入库平台处理
	 */
	public void platformWarehouseHandle(PlatformReturnInstockDTO dto) {
		// 查询对应销售订单
		// 忽略店铺
		List<SoB2cEntity> soB2cEntityList = soB2cFeign.getByPlatformCode(
				Collections.singletonList(dto.getPlatformOrderNo()),
				dto.getPlatform(),
				"",
				SourceTypeEnum.SO_B2C.getCode()
		);
		if(CollectionUtils.isEmpty(soB2cEntityList)){
			log.warn("销售订单不存在:{}", dto.getPlatformOrderNo());
			return;
		}
		List<String> soIds = soB2cEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList());

		// 根据
		List<SoReturnInstockEntity> soReturnList = soReturnInstockService.lambdaQuery()
				.in(SoReturnInstockEntity::getSourceCode, dto.getUniqueId())
				.list();
		if(CollectionUtils.isNotEmpty(soReturnList)){
			log.warn("退货入库单已存在:{}", dto.getPlatformOrderNo());
			return;
		}
		List<SoOutstockEntity> soOutstockList = soOutstockService.listBySoIds(soIds);
		if (CollectionUtils.isEmpty(soOutstockList)){
			log.warn("销售出库单不存在:{}", dto.getPlatformOrderNo());
			return;
		}
		// 查询对应店铺
		String platformShopCode = dto.getAuthId();
		List<ShopInfoEntity> shopList = shopInfoFeign.listByParams(new ShopInfoDTO.ListParamDTO(AuthStatusEnum.ALREADY.getCode(), PlatformDictEnum.AMAZON.getCode(), null));
		List<String> shopIds = shopList.stream()
				.filter(e -> e.getPlatformShopCode().equalsIgnoreCase(platformShopCode))
				.map(BaseEntity::getId)
				.collect(Collectors.toList());
		if (CollectionUtils.isEmpty(shopIds)){
			log.warn("店铺不存在:店铺代号{}", dto.getAuthId());
			return;
		}
		// 对应订单
		SoB2cEntity soB2cEntity = soB2cEntityList.stream().filter(e -> shopIds.contains(e.getShopId())).findFirst().orElse(null);
		if (null == soB2cEntity){
			log.warn("销售订单单不存在:{}", dto.getPlatformOrderNo());
			return;
		}
		ShopInfoEntity shopInfoEntity = shopList.stream().filter(e -> e.getId().equalsIgnoreCase(soB2cEntity.getShopId())).findFirst().orElse(null);

		SoOutstockEntity soOutstock = soOutstockList.get(0);
		WarehouseEntity warehouseEntity = warehouseService.getById(soOutstock.getWarehouseId());

		List<PlatformReturnInstockDTO.Detail> details = dto.getProductDetailList();
		if(CollectionUtils.isEmpty(details)){
			throw new ServiceException("明细为空");
		}
		SoReturnInstockEntity soReturnInstockEntity = this.buildPlatformSoReturnInstockEntity(dto, warehouseEntity, soB2cEntity, soOutstock, shopInfoEntity);

		List<SoReturnInstockDetailEntity> detailEntityList = this.buildPlatformSoReturnInstockDetail(dto, warehouseEntity, soOutstock);
		if(CollectionUtils.isEmpty(detailEntityList)){
			throw new ServiceException("没有映射");
		}
		//关联销售退货单
		this.matchSoReturn(soReturnInstockEntity,detailEntityList,dto, soB2cEntity);

		soReturnInstockService.addByThirdWarehouse(soReturnInstockEntity,detailEntityList);
	}


	private List<SoReturnInstockDetailEntity> buildPlatformSoReturnInstockDetail(PlatformReturnInstockDTO dto, WarehouseEntity warehouseEntity, SoOutstockEntity soOutstock) {
		List<PlatformReturnInstockDTO.Detail> details = dto.getProductDetailList();
		if(CollectionUtils.isEmpty(details)){
			throw new ServiceException("明细为空");
		}
		// 对应销售出库明细
		List<SoOutstockDetailEntity> soOutstockDetailEntityList = soOutstockDetailService.listByMainIds(Collections.singletonList(soOutstock.getId()));

		// 销售订单明细
		List<SoB2cDetailEntity> soDetailEntityList = soB2cFeign.listDetailByMainIds(Collections.singletonList(soOutstock.getSoId()));

		List<SoReturnInstockDetailEntity> detailEntityList = new ArrayList<>();
		for (PlatformReturnInstockDTO.Detail detail : details) {
			List<SoB2cDetailEntity> curDetailList = soDetailEntityList.stream().filter(e -> e.getPlatformSkuNo().equalsIgnoreCase(detail.getProductSku())).collect(Collectors.toList());
			if(CollectionUtils.isEmpty(curDetailList)){
				ServiceException.runError("找不到销售订单明细:{}", dto.getPlatformOrderNo());
			}
			for (SoB2cDetailEntity detailEntity : curDetailList) {
				SoOutstockDetailEntity soOutDetailEntity = soOutstockDetailEntityList.stream().filter(e -> e.getSoDetailId().equals(detailEntity.getId())).findFirst().orElse(null);
				if (null == soOutDetailEntity){
					continue;
				}
				SoReturnInstockDetailEntity soReturnInstockDetailEntity = new SoReturnInstockDetailEntity();
				soReturnInstockDetailEntity.setSkuId(soOutDetailEntity.getSkuId());
				soReturnInstockDetailEntity.setSkuNo(soOutDetailEntity.getSkuNo());
				soReturnInstockDetailEntity.setMustQty(detail.getMustQty());
				soReturnInstockDetailEntity.setReceiveQty(detail.getReceiveQty());
				soReturnInstockDetailEntity.setRealQty(detail.getRealQty());
				soReturnInstockDetailEntity.setWarehouseId(warehouseEntity.getId());
				soReturnInstockDetailEntity.setWarehouseName(warehouseEntity.getName());
				soReturnInstockDetailEntity.setRemark(dto.getReason());
				soReturnInstockDetailEntity.setReturnTypeDict(dto.getReturnType());
				detailEntityList.add(soReturnInstockDetailEntity);
			}

		}
		return detailEntityList;
	}

	private SoReturnInstockEntity buildPlatformSoReturnInstockEntity(PlatformReturnInstockDTO dto,
															 WarehouseEntity warehouseEntity,
															 SoB2cEntity soB2cEntity,
															 SoOutstockEntity soOutstock,
															 ShopInfoEntity shopInfoEntity
	){
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
		soReturnInstockEntity.setSourceCode(dto.getOrderReferenceNo());
		soReturnInstockEntity.setSourceType(SourceTypeEnum.PLATFORM_RETURN_INSTOCK.getCode());
		soReturnInstockEntity.setThirdCode(dto.getPlatformReturnOrderNo());
		soReturnInstockEntity.setCreated(dto.getCreateTime());
		soReturnInstockEntity.setType("B2C");
		if(Objects.nonNull(soB2cEntity)){
			soReturnInstockEntity.setSalesOrgId(soB2cEntity.getOrgId());
			soReturnInstockEntity.setSalesOrgName(soB2cEntity.getOrgName());
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