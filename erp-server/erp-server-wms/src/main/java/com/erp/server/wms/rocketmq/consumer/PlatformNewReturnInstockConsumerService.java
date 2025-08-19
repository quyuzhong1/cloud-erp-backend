package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.PlatformReturnInstockDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.dto.ShopInfoDTO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.oms.enums.ListingMatchResultEnum;
import com.erp.model.oms.enums.RuleTypeEnum;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
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

	@Resource
	private InventoryClosedRecordService inventoryClosedRecordService;

    @Resource
    private SoReturnInstockDetailService soReturnInstockDetailService;

	@Resource
	private ThirdWarehouseDeliveryService thirdWarehouseDeliveryService;
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

		if (PlatformDictEnum.GOOD_CANG.getCode().equalsIgnoreCase(dto.getPlatform())){
			if (CollectionUtils.isEmpty(dto.getProductDetailList())){
				ServiceException.runError("谷仓退货入库明细流水为空");
			}
			String thirdId = dto.getProductDetailList().get(0).getThirdId();
			if (StringUtils.isBlank(thirdId)){
				ServiceException.runError("谷仓退货入库明细流水thirdId不能为空");
			}
			// 谷仓按明细ID判断
			Integer count = soReturnInstockDetailService.lambdaQuery()
					.eq(SoReturnInstockDetailEntity::getSourceDetailId, thirdId)
					.eq(SoReturnInstockDetailEntity::getCreateUserId, dto.getAuthId())
					.count();
			if (count > 0) {
				return;
			}
		}else if (PlatformDictEnum.DA_MAI.getCode().equalsIgnoreCase(dto.getPlatform())){
			//根据明细判断
			String sku = dto.getProductDetailList().get(0).getProductSku();
			SoReturnInstockEntity exist = soReturnInstockService.getByThirdCodeAndPlatformSkuNo(dto.getPlatformReturnOrderNo(),sku);
			if(Objects.nonNull(exist)){
				return;
			}
		} else {
			SoReturnInstockEntity existEntity = soReturnInstockService.getByThirdCode(dto.getPlatformReturnOrderNo());
			if(Objects.nonNull(existEntity)){
				return;
			}
		}

		OverseasProviderWarehouseEntity overseasProviderWarehouseEntity = overseasProviderWarehouseService.getByPlatform(dto.getAuthId(),dto.getWarehouseCode());
		if(Objects.isNull(overseasProviderWarehouseEntity) || CharSequenceUtil.isBlank(overseasProviderWarehouseEntity.getWarehouseId())){
			throw new ServiceException(ApiError.NOT_EXIST,"仓库信息");
		}
		SoB2cEntity soB2cEntity = null;
		SoOutstockEntity soOutstock = null;
		WarehouseEntity warehouseEntity = warehouseService.getById(overseasProviderWarehouseEntity.getWarehouseId());
		if(CharSequenceUtil.isNotBlank(dto.getOrderReferenceNo())){
			ThirdWarehouseDeliveryEntity thirdWarehouseDeliveryEntity;
			if(dto.getOrderReferenceNo().contains(BusinessNoConstant.WFHD)){
				//查询三方仓发货单
				thirdWarehouseDeliveryEntity = thirdWarehouseDeliveryService.getLatestByCode(dto.getOrderReferenceNo());
				if(Objects.nonNull(thirdWarehouseDeliveryEntity)){
					String soCode = thirdWarehouseDeliveryEntity.getSoCode();
					soB2cEntity = soB2cFeign.getSoCode(soCode);
				}
			}else{
				soB2cEntity = soB2cFeign.getSoCode(dto.getOrderReferenceNo());
			}
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
			soReturnInstockDetailEntity.setSourceDetailId(detail.getThirdId());
			soReturnInstockDetailEntity.setCreateUserId(dto.getAuthId());
			soReturnInstockDetailEntity.setPlatformSkuNo(detail.getProductSku());
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
		soReturnInstockEntity.setReturnLogisticCode(dto.getReturnLogisticCode());
		//组织信息
		SysAccountingCompanyEntity company = sysUserFeign.getCompanyById(warehouseEntity.getOrgId());
		soReturnInstockEntity.setInventoryOrgName(company.getCompanyName());
		soReturnInstockEntity.setWarehouseKeeperId(warehouseEntity.getChargeId());
		soReturnInstockEntity.setApproveUserName("system");
		soReturnInstockEntity.setSourceCode(Objects.nonNull(soB2cEntity)?soB2cEntity.getCode():dto.getOrderReferenceNo());
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
			soReturnInstockEntity.setShopId(soB2cEntity.getShopId());
			soReturnInstockEntity.setCurrency(soB2cEntity.getCurrency());
		} else {
			soReturnInstockEntity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
			soReturnInstockEntity.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
			soReturnInstockEntity.setCurrencySymbol("¥");
		}
		if(Objects.nonNull(soOutstock)){
			if (StringUtils.isNotBlank(soOutstock.getSalesDeptId())){
				SysDepartmentDTO department = sysUserFeign.getUserDeptById(soOutstock.getSalesDeptId());
				if( null != department){
					soReturnInstockEntity.setSalesDeptId(soOutstock.getSalesDeptId());
					soReturnInstockEntity.setSalesDeptName(department.getName());
				}
			}
			soReturnInstockEntity.setSellerId(soOutstock.getSellerId());
			soReturnInstockEntity.setSellerName(soOutstock.getSellerName());
		}
		return soReturnInstockEntity;
	}


	/**
	 * 平台仓入库平台处理
	 */
	public void platformWarehouseHandle(PlatformReturnInstockDTO dto) {
		// 根据
		List<SoReturnInstockEntity> soReturnList = soReturnInstockService.lambdaQuery()
				.in(SoReturnInstockEntity::getSourceCode, dto.getUniqueId())
				.list();
		if(CollectionUtils.isNotEmpty(soReturnList)){
			log.warn("【平台退货入库】退货入库单已存在:{}", dto.getUniqueId());
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
			log.warn("【平台退货入库】店铺不存在:店铺代号{}", dto.getAuthId());
			ServiceException.runError("【平台退货入库】店铺不存在:店铺代号{}", dto.getAuthId());
		}
		List<PlatformReturnInstockDTO.Detail> details = dto.getProductDetailList();
		if(CollectionUtils.isEmpty(details)){
			ServiceException.runError("【平台退货入库】来源明细为空");
		}
		// 查询对应销售订单
		// 忽略店铺
		List<SoB2cEntity> soB2cEntityList = soB2cFeign.getByPlatformCode(
				Collections.singletonList(dto.getPlatformOrderNo()),
				dto.getPlatform(),
				"",
				SourceTypeEnum.SO_B2C.getCode()
		);
		// 对应销售订单是否存在
		SoB2cEntity soB2cEntity = soB2cEntityList.stream().filter(e -> shopIds.contains(e.getShopId())).findFirst().orElse(null);
		if (null == soB2cEntity){
			log.warn("【平台退货入库】匹配销售订单不存在:{}", dto.getPlatformOrderNo());
			// 不存在按当前映射关系生成
			createByNotExistSoB2c(dto, shopList, shopIds);
		} else {
			// 存在按销售订单映射生成
			createByExistSoB2c(dto, shopList, soB2cEntity);
		}

	}


	private List<SoReturnInstockDetailEntity> buildPlatformSoReturnInstockDetail(PlatformReturnInstockDTO dto, WarehouseEntity warehouseEntity, SoB2cEntity soB2cEntity) {
		List<PlatformReturnInstockDTO.Detail> details = dto.getProductDetailList();
		// 销售订单明细
		List<SoB2cDetailEntity> soDetailEntityList = soB2cFeign.listDetailByMainIds(Collections.singletonList(soB2cEntity.getId()));
		if (CollectionUtils.isEmpty(soDetailEntityList)){
			ServiceException.runError("【平台退货入库】对应订单明细为空：{}", dto.getPlatformOrderNo());
		}
		List<SoReturnInstockDetailEntity> detailEntityList = new ArrayList<>();
		for (PlatformReturnInstockDTO.Detail detail : details) {
			SoB2cDetailEntity detailEntity = soDetailEntityList.stream().filter(e -> e.getPlatformSkuNo().equalsIgnoreCase(detail.getProductSku())).findFirst().orElse(null);
			if(null == detailEntity){
				ServiceException.runError("找不到销售订单明细:{}", dto.getPlatformOrderNo());
			}
			SoReturnInstockDetailEntity soReturnInstockDetailEntity = new SoReturnInstockDetailEntity();
			soReturnInstockDetailEntity.setSkuId(detailEntity.getSkuId());
			soReturnInstockDetailEntity.setSkuNo(detailEntity.getSkuNo());
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

	private SoReturnInstockEntity buildPlatformSoReturnInstockEntity(PlatformReturnInstockDTO dto,
															 WarehouseEntity warehouseEntity,
															 SoB2cEntity soB2cEntity,
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
		soReturnInstockEntity.setSourceCode(dto.getUniqueId());
		soReturnInstockEntity.setSourceType(SourceTypeEnum.PLATFORM_RETURN_INSTOCK.getCode());
		soReturnInstockEntity.setThirdCode(dto.getPlatformReturnOrderNo());
		soReturnInstockEntity.setPlatformOrderCode(dto.getPlatformOrderNo());
		soReturnInstockEntity.setCreated(dto.getCreateTime());
		soReturnInstockEntity.setType("B2C");
		if(Objects.nonNull(soB2cEntity)) {
			soReturnInstockEntity.setSoId(soB2cEntity.getId());
			soReturnInstockEntity.setSoCode(soB2cEntity.getCode());
			soReturnInstockEntity.setCurrency(soB2cEntity.getCurrency());
		} else {
			soReturnInstockEntity.setCurrency(shopInfoEntity.getSettlementCurrency());
		}
		if (StringUtils.isBlank(shopInfoEntity.getCustomerId())){
			ServiceException.runError("店铺对应客户信息为空:{}", shopInfoEntity.getName());
		}
		CustomerInfoEntity customerInfo = customerFeign.getCustomerById(shopInfoEntity.getCustomerId());
		if (null == customerInfo){
			ServiceException.runError("店铺对应客户信息不存在:客户ID={}", shopInfoEntity.getCustomerId());
		}
		soReturnInstockEntity.setSalesOrgId(customerInfo.getUseOrgId());
		soReturnInstockEntity.setSalesOrgName(customerInfo.getUseOrgName());
		soReturnInstockEntity.setCustomerId(shopInfoEntity.getCustomerId());
		soReturnInstockEntity.setCustomerName(customerInfo.getName());
		if (StringUtils.isNotBlank(customerInfo.getSalesDeptId())){
			SysDepartmentDTO department = sysUserFeign.getUserDeptById(customerInfo.getSalesDeptId());
			if( null != department){
				soReturnInstockEntity.setSalesDeptId(customerInfo.getSalesDeptId());
				soReturnInstockEntity.setSalesDeptName(department.getName());
			}
		}
		soReturnInstockEntity.setSellerId(customerInfo.getSellerId());
		soReturnInstockEntity.setSellerName(customerInfo.getSellerName());
		return soReturnInstockEntity;
	}

	/**
	 * 不存在销售订单创建退货入库单
	 */
	private void createByNotExistSoB2c(PlatformReturnInstockDTO dto, List<ShopInfoEntity> shopList, List<String> shopIds) {
		List<ShopInfoEntity> allAccnountShopList = shopList.stream().filter(e -> shopIds.contains(e.getId())).collect(Collectors.toList());
		if (CollectionUtils.isEmpty(allAccnountShopList)){
			log.warn("【平台退货入库】店铺列表不存在:{}", dto.getPlatformOrderNo());
			ServiceException.runError("【平台退货入库】店铺列表不存在:店铺代号{}", dto.getAuthId());
		}
		// TODO 临时指定站点
		ShopInfoEntity shopInfoEntity;
		if (allAccnountShopList.size() > 2){
			shopInfoEntity = allAccnountShopList.stream()
					.filter(e-> "DE".equalsIgnoreCase(e.getDictCountryCode()) || "US".equalsIgnoreCase(e.getDictCountryCode()))
					.findFirst()
					.orElse(allAccnountShopList.get(0));
		} else {
			shopInfoEntity = allAccnountShopList.get(0);
		}
		if (null == shopInfoEntity){
			log.warn("【平台退货入库】历史销售订单对应店铺不存在:{}", dto.getPlatformOrderNo());
			ServiceException.runError("【平台退货入库】历史销售订单对应店铺不存在:店铺代号{}", dto.getAuthId());
		}
		WarehouseEntity warehouseEntity = checkAndGetWarehouseByShopInfo(shopInfoEntity);
		// 查询是否已关账
		LocalDate closedLocalDate = inventoryClosedRecordService.checkClosed(warehouseEntity.getOrgId(), dto.getPutawayTime().toLocalDate());
		if (null != closedLocalDate) {
			// 已关账
			log.warn("[退货入库单消费]:当前退货入库单消费日期【{}】因关账【{}】停止生成：单号={}", dto.getPutawayTime().toLocalDate(), closedLocalDate, dto.getPlatformOrderNo());
			return;
		}
		List<SoReturnInstockDetailEntity> detailEntityList = this.buildPlatformSoReturnInstockDetailWithoutSoB2c(dto, warehouseEntity, shopInfoEntity, shopIds);
		if(CollectionUtils.isEmpty(detailEntityList)){
			ServiceException.runError("【平台退货入库】来源明细未匹配到映射");
		}
		SoReturnInstockEntity soReturnInstockEntity = this.buildPlatformSoReturnInstockEntity(dto, warehouseEntity, null, shopInfoEntity);


		soReturnInstockService.addByThirdWarehouse(soReturnInstockEntity,detailEntityList);
	}


	/**
	 * 已存在销售订单创建退货入库单
	 */
	private void createByExistSoB2c(PlatformReturnInstockDTO dto, List<ShopInfoEntity> shopList, SoB2cEntity soB2cEntity) {
		ShopInfoEntity shopInfoEntity = shopList.stream().filter(e -> e.getId().equalsIgnoreCase(soB2cEntity.getShopId())).findFirst().orElse(null);
		if (null == shopInfoEntity){
			log.warn("【平台退货入库】店铺不存在:{}", dto.getPlatformOrderNo());
			ServiceException.runError("【平台退货入库】店铺不存在:店铺代号{}", dto.getAuthId());
		}
		WarehouseEntity warehouseEntity = checkAndGetWarehouseByShopInfo(shopInfoEntity);
		// 查询是否已关账
		LocalDate closedLocalDate = inventoryClosedRecordService.checkClosed(warehouseEntity.getOrgId(), dto.getPutawayTime().toLocalDate());
		if (null != closedLocalDate) {
			// 已关账
			log.warn("[退货入库单消费]:当前退货入库单消费日期【{}】因关账【{}】停止生成：单号={}", dto.getPutawayTime().toLocalDate(), closedLocalDate, dto.getPlatformOrderNo());
			return;
		}

		List<SoReturnInstockDetailEntity> detailEntityList = this.buildPlatformSoReturnInstockDetail(dto, warehouseEntity, soB2cEntity);
		if(CollectionUtils.isEmpty(detailEntityList)){
			ServiceException.runError("【平台退货入库】来源明细未匹配到映射");
		}
		SoReturnInstockEntity soReturnInstockEntity = this.buildPlatformSoReturnInstockEntity(dto, warehouseEntity, soB2cEntity, shopInfoEntity);

		//关联销售退货单
		this.matchSoReturn(soReturnInstockEntity,detailEntityList,dto, soB2cEntity);

		soReturnInstockService.addByThirdWarehouse(soReturnInstockEntity,detailEntityList);
	}


	/**
	 * 平台退货入库
	 */
	private WarehouseEntity checkAndGetWarehouseByShopInfo(ShopInfoEntity shopInfoEntity) {
		String returnWarehouse = StringUtils.isBlank(shopInfoEntity.getReturnWarehouse()) ? shopInfoEntity.getWarehouseId() : shopInfoEntity.getReturnWarehouse();
		WarehouseEntity warehouseEntity = warehouseService.getById(returnWarehouse);
		if (null == warehouseEntity){
			ServiceException.runError("未找到店铺配置的退货仓库或关联店铺:{}", shopInfoEntity.getName());
		}
		return warehouseEntity;
	}


	private List<SoReturnInstockDetailEntity> buildPlatformSoReturnInstockDetailWithoutSoB2c(PlatformReturnInstockDTO dto,
																							 WarehouseEntity warehouseEntity,
																							 ShopInfoEntity shopInfoEntity,
																							 List<String> shopIds
	) {
		List<String> platformSkuList = dto.getProductDetailList().stream().map(PlatformReturnInstockDTO.Detail::getProductSku).distinct().collect(Collectors.toList());
		// 查询店铺映射:
		ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
		paramDTO.setPlatform(PlatformDictEnum.AMAZON.getCode());
		paramDTO.setPlatformSkuNoList(platformSkuList);
		paramDTO.setShopIdList(shopIds);
		paramDTO.setType(RuleTypeEnum.PLATFORM.getCode());
		paramDTO.setMatchResult(ListingMatchResultEnum.TRUE.getCode());
		paramDTO.setIsExpire(false);
		// 查询ListingInfo和skuMapping的关系
		List<ListingInfoWithSkuMappingDTO> listingedInfoWithSkuMappingList = skuMappingFeign.listingInfoWithSkuMappingList(paramDTO);
		Map<String, List<ListingInfoWithSkuMappingDTO>> mappingRelationMap = listingedInfoWithSkuMappingList.stream()
				.collect(Collectors.groupingBy(ListingInfoWithSkuMappingDTO::getPlatformSkuNo));

		List<SoReturnInstockDetailEntity> detailEntityList = new ArrayList<>();
		for (PlatformReturnInstockDTO.Detail detail : dto.getProductDetailList()) {
			List<ListingInfoWithSkuMappingDTO> mappingDTOList = mappingRelationMap.get(detail.getProductSku());
			if(CollectionUtils.isEmpty(mappingDTOList)){
				ServiceException.runError("未找到sku映射记录:{}", dto.getPlatformOrderNo());
			}
			ListingInfoWithSkuMappingDTO mappingDTO = checkAndMappingDTO(mappingDTOList, shopInfoEntity.getId());
			SoReturnInstockDetailEntity soReturnInstockDetailEntity = new SoReturnInstockDetailEntity();
			soReturnInstockDetailEntity.setSkuId(mappingDTO.getProductSkuId());
			soReturnInstockDetailEntity.setSkuNo(mappingDTO.getProductSkuNo());
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

	/**
	 * 检查或获取映射关系
	 */
	public ListingInfoWithSkuMappingDTO checkAndMappingDTO(List<ListingInfoWithSkuMappingDTO> mappingDTOList,String shopId) {
		if (1 == mappingDTOList.size()){
			return mappingDTOList.get(0);
		}
		return mappingDTOList.stream()
				.filter(e->e.getShopId().equalsIgnoreCase(shopId))
				.findFirst()
				.orElse(mappingDTOList.get(0));
	}
}