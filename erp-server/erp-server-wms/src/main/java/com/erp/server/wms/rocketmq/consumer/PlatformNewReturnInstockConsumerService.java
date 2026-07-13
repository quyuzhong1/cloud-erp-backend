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
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.dto.ShopInfoDTO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.oms.enums.BillTypeEnum;
import com.erp.model.oms.enums.ListingMatchResultEnum;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cReturnStatusEnum;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.wms.dto.SoReturnPrestockDTO;
import com.erp.model.wms.dto.SoReturnPrestockDetailDTO;
import com.erp.model.wms.entity.*;
import com.erp.rpc.oms.feign.*;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.service.*;
import com.google.common.collect.Lists;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

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
	private SoInfoFeign soInfoFeign;

	@Resource
	private SoOutstockDetailService soOutstockDetailService;

	@Resource
	private InventoryClosedRecordService inventoryClosedRecordService;

    @Resource
    private SoReturnInstockDetailService soReturnInstockDetailService;

	@Resource
	private SoB2cReturnFeign soB2cReturnFeign;

	@Resource
	private SoReturnPrestockService soReturnPrestockService;

	@Resource
	private ThirdWarehouseDeliveryService thirdWarehouseDeliveryService;

	@Resource
	private PlatformTransactionManager transactionManager;

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
			SoReturnInstockEntity exist = soReturnInstockService.getBySourceId(dto.getSourceId());
			if(Objects.nonNull(exist)){
				return;
			}
		} else {
			SoReturnInstockEntity existEntity = soReturnInstockService.getByThirdCode(dto.getPlatformReturnOrderNo());
			if(Objects.nonNull(existEntity)){
				// 已知遗留限制：这里只判断"是否存在任意一条退货入库单"，不核对是否已覆盖全部平台推送明细；
				// 若此前处理是部分成功（例如物流单号匹配已生成一部分，参考单号匹配那一步还没跑完），本次会被直接跳过。
				// 打日志留痕，方便按 thirdCode 排查该单是否遗漏了未匹配SKU对应的预入库单
				log.warn("[海外仓退货入库] thirdCode={} 已存在退货入库单(id={}, code={})，本次消息跳过处理；若怀疑此前处理未覆盖全部平台推送明细，请人工核对该thirdCode下退货入库单+预入库单明细合计是否等于平台推送明细",
						dto.getPlatformReturnOrderNo(), existEntity.getId(), existEntity.getCode());
				return;
			}
		}

		OverseasProviderWarehouseEntity overseasProviderWarehouseEntity = overseasProviderWarehouseService.getByPlatform(dto.getAuthId(),dto.getWarehouseCode());
		if(Objects.isNull(overseasProviderWarehouseEntity) || CharSequenceUtil.isBlank(overseasProviderWarehouseEntity.getWarehouseId())){
			throw new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC,"仓库信息");
		}
		WarehouseEntity warehouseEntity = warehouseService.getById(overseasProviderWarehouseEntity.getWarehouseId());

		if (CharSequenceUtil.isNotBlank(dto.getReturnLogisticCode())) {
			// 按退货物流单号匹配B2B/B2C售后单：命中部分直接生成已审核退货入库单，未匹配的剩余数量带着继续走后续分支
			List<PlatformReturnInstockDTO.Detail> remaining = soReturnInstockService.matchAndCreateByReturnLogisticCode(dto, warehouseEntity);
			if (CollectionUtils.isEmpty(remaining)) {
				return;
			}
			dto.setProductDetailList(remaining);
		}
		if (CharSequenceUtil.isNotBlank(dto.getOrderReferenceNo())) {
			// 先按参考单号匹配售后单/退货单/销售订单，匹配不到再尝试按三方仓发货单号匹配，两者均无匹配则生成无关联预入库单
			if (handleReferenceNoAfterSaleMatch(dto, warehouseEntity)) {
				return;
			}
			if (handleWfhdReferenceNo(dto, warehouseEntity)) {
				return;
			}
		}
		// 参考单号无匹配或无参考单号：生成预入库单
		this.createSoReturnPrestockHeadless(dto, warehouseEntity);
	}

	/**
	 * 按三方仓发货单号查询对应B2C销售订单，命中则生成已审核退货入库单并返回 true，否则返回 false
	 */
	private boolean handleWfhdReferenceNo(PlatformReturnInstockDTO dto, WarehouseEntity warehouseEntity) {
		ThirdWarehouseDeliveryEntity thirdWarehouseDeliveryEntity = thirdWarehouseDeliveryService.getLatestByCode(dto.getOrderReferenceNo());
		if (Objects.nonNull(thirdWarehouseDeliveryEntity) && CharSequenceUtil.isNotBlank(thirdWarehouseDeliveryEntity.getSoCode())) {
			SoB2cEntity soB2cEntity = soB2cFeign.getSoCode(thirdWarehouseDeliveryEntity.getSoCode());
			if (Objects.nonNull(soB2cEntity)) {
				generateInstockBySo(dto, warehouseEntity, soB2cEntity);
				return true;
			}
		}
		return false;
	}

	/**
	 * 按参考单号依次匹配《B2C售后单-退货单》、《B2B/B2C销售订单》，命中则生成退货入库单并返回 true，均未匹配返回 false
	 */
	private boolean handleReferenceNoAfterSaleMatch(PlatformReturnInstockDTO dto, WarehouseEntity warehouseEntity) {
		List<SoB2cReturnEntity> candidates = matchB2cReturnCandidates(dto.getOrderReferenceNo());
		SoB2cReturnEntity matchedReturn = CollectionUtils.isEmpty(candidates) ? null : pickReturnBySkuMatch(candidates, dto);
		if (Objects.nonNull(matchedReturn)) {
			if (generateInstockByMatchedReturn(dto, warehouseEntity, matchedReturn)) {
				return true;
			}
		}
		Set<String> incomingSkuIds = resolveSkuIds(dto);
		SoB2cEntity soB2cEntity = matchB2cSoByReferenceNo(dto.getOrderReferenceNo(), incomingSkuIds);
		if (Objects.nonNull(soB2cEntity)) {
			generateInstockBySo(dto, warehouseEntity, soB2cEntity);
			return true;
		}
		SoInfoEntity soInfoEntity = matchB2bSoByReferenceNo(dto.getOrderReferenceNo(), incomingSkuIds);
		if (Objects.nonNull(soInfoEntity)) {
			generateInstockBySoInfo(dto, warehouseEntity, soInfoEntity);
			return true;
		}
		return false;
	}

	/**
	 * 按参考单号匹配《B2C售后单-退货单》候选列表：命中平台订单号、平台退货单号、销售单号、退货单号任一字段即算候选。
	 * 服务端一次查询完成四字段 OR 匹配，避免多次 Feign 往返。
	 */
	private List<SoB2cReturnEntity> matchB2cReturnCandidates(String orderReferenceNo) {
		List<SoB2cReturnEntity> candidates = soB2cReturnFeign.listByAnyReferenceNo(orderReferenceNo);
		return CollectionUtils.isEmpty(candidates) ? new ArrayList<>() : candidates;
	}

	/**
	 * 按SKU维度从候选退货单中挑选可匹配的一条：优先"待退货"状态，同状态内取第一个SKU命中的
	 */
	private SoB2cReturnEntity pickReturnBySkuMatch(List<SoB2cReturnEntity> candidates, PlatformReturnInstockDTO dto) {
		Set<String> incomingSkuIds = resolveSkuIds(dto);
		if (CollectionUtils.isEmpty(incomingSkuIds)) {
			return null;
		}
		// 批量一次性取全部候选单明细，避免逐个候选单发起Feign查询
		List<String> candidateIds = candidates.stream().map(SoB2cReturnEntity::getId).collect(Collectors.toList());
		List<SoB2cReturnDetailEntity> allDetails = FeignQuery.create(SoB2cReturnDetailEntity.class)
				.in(SoB2cReturnDetailEntity::getMainId, candidateIds).list();
		Map<String, List<SoB2cReturnDetailEntity>> detailsByMainId = allDetails.stream()
				.collect(Collectors.groupingBy(SoB2cReturnDetailEntity::getMainId));

		List<SoB2cReturnEntity> sorted = candidates.stream()
				.sorted(Comparator.comparing(v -> SoB2cReturnStatusEnum.TO_BE_RETURNED.getCode().equals(v.getStatus()) ? 0 : 1))
				.collect(Collectors.toList());
		for (SoB2cReturnEntity candidate : sorted) {
			List<SoB2cReturnDetailEntity> detailList = detailsByMainId.getOrDefault(candidate.getId(), Collections.emptyList());
			boolean skuMatched = detailList.stream()
					.anyMatch(d -> StringUtils.isNotBlank(d.getSkuId()) && incomingSkuIds.contains(d.getSkuId()));
			if (skuMatched) {
				return candidate;
			}
		}
		return null;
	}

	/**
	 * 解析退货入库明细对应的ERP SKU ID集合（仅保留已成功映射的）
	 */
	private Set<String> resolveSkuIds(PlatformReturnInstockDTO dto) {
		List<PlatformReturnInstockDTO.Detail> details = dto.getProductDetailList();
		if (CollectionUtils.isEmpty(details)) {
			return Collections.emptySet();
		}
		List<String> platformSkuNoList = details.stream().map(PlatformReturnInstockDTO.Detail::getProductSku).collect(Collectors.toList());
		ListingInfoParamDTO listingInfoParamDTO = new ListingInfoParamDTO();
		listingInfoParamDTO.setPlatformSkuNoList(platformSkuNoList);
		listingInfoParamDTO.setAuthId(dto.getAuthId());
		listingInfoParamDTO.setMatchResult(ListingMatchResultEnum.TRUE.getCode());
		List<SkuMappingDTO.MappingSkuViewDTO> mappingSkuViewDTOList = skuMappingFeign.listByPlatformSkuNoAndPlatform(listingInfoParamDTO);
		return mappingSkuViewDTOList.stream()
				.map(SkuMappingDTO.MappingSkuViewDTO::getProductSkuId)
				.filter(StringUtils::isNotBlank)
				.collect(Collectors.toSet());
	}

	/**
	 * 命中退货单后生成《已审核-退货入库单》，并将本次入库与该退货单及其SKU匹配明细关联
	 *
	 * @return 是否生成成功；售后单/店铺数据不一致导致无法生成时返回 false，交由调用方回退到下一步匹配
	 */
	private boolean generateInstockByMatchedReturn(PlatformReturnInstockDTO dto, WarehouseEntity warehouseEntity, SoB2cReturnEntity matchedReturn) {
		SoB2cEntity soB2cEntity = soB2cFeign.getById(matchedReturn.getSoId());
		if (Objects.isNull(soB2cEntity)) {
			log.warn("[海外仓退货入库] 匹配到退货单{}但对应销售订单不存在，回退到销售订单匹配：soId={}", matchedReturn.getCode(), matchedReturn.getSoId());
			return false;
		}
		ShopInfoEntity shopInfoEntity = shopInfoFeign.getShopInfoById(soB2cEntity.getShopId());
		if (Objects.isNull(shopInfoEntity)) {
			log.warn("[海外仓退货入库] 匹配到退货单{}但对应店铺不存在，回退到销售订单匹配：shopId={}", matchedReturn.getCode(), soB2cEntity.getShopId());
			return false;
		}
		SkuSplitDetailResult splitResult = this.buildPlatformSoReturnInstockDetailSplit(dto, warehouseEntity, soB2cEntity);
		if (CollectionUtils.isEmpty(splitResult.matchedList)) {
			ServiceException.runError("【海外仓退货入库】来源明细未匹配到映射");
		}
		SoReturnInstockEntity soReturnInstockEntity = this.buildPlatformSoReturnInstockEntity(dto, warehouseEntity, soB2cEntity, shopInfoEntity);
		// 来源编号：该场景已匹配到具体的B2C退货单，取其挂载的销售订单编号作为来源追溯
		if (StringUtils.isNotBlank(matchedReturn.getSoCode())) {
			soReturnInstockEntity.setSourceCode(matchedReturn.getSoCode());
		}

		// 关联已匹配到的退货单：按SKU回写明细的退货单明细ID，退货单状态由待退货流转为已退货
		List<SoB2cReturnDetailEntity> matchedDetailList = FeignQuery.create(SoB2cReturnDetailEntity.class)
				.eq(SoB2cReturnDetailEntity::getMainId, matchedReturn.getId()).list();
		for (SoReturnInstockDetailEntity soReturnInstockDetailEntity : splitResult.matchedList) {
			matchedDetailList.stream()
					.filter(v -> v.getSkuId().equals(soReturnInstockDetailEntity.getSkuId()))
					.findFirst()
					.ifPresent(v -> soReturnInstockDetailEntity.setSoReturnDetailId(v.getId()));
		}
		soReturnInstockEntity.setSoReturnId(matchedReturn.getId());
		soReturnInstockEntity.setSoReturnCode(matchedReturn.getCode());
		if (SoB2cReturnStatusEnum.TO_BE_RETURNED.getCode().equals(matchedReturn.getStatus())) {
			matchedReturn.setStatus(SoB2cReturnStatusEnum.RETURNED.getCode());
			soB2cReturnFeign.updateBatch(Collections.singletonList(matchedReturn));
		}

		// 已匹配到订单的SKU落退货入库单，订单里没有的SKU（unmatchedList）单独落预入库单，两者同一本地事务提交
		this.persistMatchedInstockAndUnmatchedPrestock(soReturnInstockEntity, splitResult.matchedList, dto, warehouseEntity, splitResult.unmatchedList);
		return true;
	}

	/**
	 * 按参考单号匹配《B2C销售订单》：平台订单号或销售单号=参考单号，且需状态为"已发货"、SKU 命中其一。
	 * 可匹配数据范围：B2C销售订单-已发货
	 */
	private SoB2cEntity matchB2cSoByReferenceNo(String orderReferenceNo, Set<String> incomingSkuIds) {
		List<SoB2cEntity> candidates = new ArrayList<>();
		List<SoB2cEntity> byPlatformCode = soB2cFeign.getSoB2cByPlatformCode(orderReferenceNo);
		if (CollectionUtils.isNotEmpty(byPlatformCode)) {
			candidates.addAll(byPlatformCode);
		}
		SoB2cEntity bySoCode = soB2cFeign.getSoCode(orderReferenceNo);
		if (Objects.nonNull(bySoCode)) {
			candidates.add(bySoCode);
		}
		return candidates.stream()
				.filter(v -> !Boolean.TRUE.equals(v.getInvalidStatus()))
				.filter(v -> SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(v.getBillStatus()))
				.filter(v -> soB2cHasMatchingSku(v.getId(), incomingSkuIds))
				.findFirst()
				.orElse(null);
	}

	/**
	 * 校验B2C销售订单明细是否存在命中的ERP SKU
	 */
	private boolean soB2cHasMatchingSku(String soId, Set<String> incomingSkuIds) {
		if (CollectionUtils.isEmpty(incomingSkuIds)) {
			return false;
		}
		List<SoB2cDetailEntity> detailList = soB2cFeign.listDetailByMainIds(Collections.singletonList(soId));
		return CollectionUtils.isNotEmpty(detailList) && detailList.stream()
				.anyMatch(d -> StringUtils.isNotBlank(d.getSkuId()) && incomingSkuIds.contains(d.getSkuId()));
	}

	/**
	 * 按参考单号匹配《B2B销售订单》：平台订单号或销售单号=参考单号，且需未作废、SKU 命中其一。
	 * 可匹配数据范围：B2B销售订单-作废状态：未作废
	 */
	private SoInfoEntity matchB2bSoByReferenceNo(String orderReferenceNo, Set<String> incomingSkuIds) {
		List<SoInfoEntity> candidates = new ArrayList<>();
		List<SoInfoEntity> byPlatformOrderCode = soInfoFeign.getByPlatformOrderCode(orderReferenceNo);
		if (CollectionUtils.isNotEmpty(byPlatformOrderCode)) {
			candidates.addAll(byPlatformOrderCode);
		}
		SoInfoEntity byCode = soInfoFeign.getByCode(orderReferenceNo);
		if (Objects.nonNull(byCode)) {
			candidates.add(byCode);
		}
		return candidates.stream()
				.filter(v -> !Boolean.TRUE.equals(v.getInvalidStatus()))
				.filter(v -> soInfoHasMatchingSku(v.getId(), incomingSkuIds))
				.findFirst()
				.orElse(null);
	}

	/**
	 * 校验B2B销售订单明细是否存在命中的ERP SKU
	 */
	private boolean soInfoHasMatchingSku(String soInfoId, Set<String> incomingSkuIds) {
		if (CollectionUtils.isEmpty(incomingSkuIds)) {
			return false;
		}
		List<SoDetailEntity> detailList = soInfoFeign.listSoDetailByMainIds(Collections.singletonList(soInfoId));
		return CollectionUtils.isNotEmpty(detailList) && detailList.stream()
				.anyMatch(d -> StringUtils.isNotBlank(d.getSkuId()) && incomingSkuIds.contains(d.getSkuId()));
	}

	/**
	 * 命中销售订单后生成《已审核-退货入库单》，字段映射与已存在销售订单场景（{@link #createByExistSoB2c}）保持一致，
	 * 仅仓库改用海外仓映射的 warehouseEntity
	 */
	private void generateInstockBySo(PlatformReturnInstockDTO dto, WarehouseEntity warehouseEntity, SoB2cEntity soB2cEntity) {
		ShopInfoEntity shopInfoEntity = shopInfoFeign.getShopInfoById(soB2cEntity.getShopId());
		if (Objects.isNull(shopInfoEntity)) {
			log.warn("[海外仓退货入库] 参考单号匹配到销售订单{}但对应店铺不存在，生成无关联预入库单：shopId={}", soB2cEntity.getCode(), soB2cEntity.getShopId());
			this.createSoReturnPrestockHeadless(dto, warehouseEntity);
			return;
		}
		SkuSplitDetailResult splitResult = this.buildPlatformSoReturnInstockDetailSplit(dto, warehouseEntity, soB2cEntity);
		if (CollectionUtils.isEmpty(splitResult.matchedList)) {
			ServiceException.runError("【海外仓退货入库】来源明细未匹配到映射");
		}
		SoReturnInstockEntity soReturnInstockEntity = this.buildPlatformSoReturnInstockEntity(dto, warehouseEntity, soB2cEntity, shopInfoEntity);
		// 来源编号：该场景按参考单号直接匹配到销售订单（未命中具体退货单），取销售订单编号作为来源追溯
		if (StringUtils.isNotBlank(soB2cEntity.getCode())) {
			soReturnInstockEntity.setSourceCode(soB2cEntity.getCode());
		}

		// 顺带尝试关联退货单（与既有销售订单流程一致）
		this.matchSoReturn(soReturnInstockEntity, splitResult.matchedList, dto, soB2cEntity);

		// 已匹配到订单的SKU落退货入库单，订单里没有的SKU（unmatchedList）单独落预入库单，两者同一本地事务提交
		this.persistMatchedInstockAndUnmatchedPrestock(soReturnInstockEntity, splitResult.matchedList, dto, warehouseEntity, splitResult.unmatchedList);
	}

	/**
	 * 命中B2B销售订单后生成《已审核-退货入库单》：B2B无"店铺"概念，客户直接取销售订单上的客户
	 */
	private void generateInstockBySoInfo(PlatformReturnInstockDTO dto, WarehouseEntity warehouseEntity, SoInfoEntity soInfoEntity) {
		SkuSplitDetailResult splitResult = this.buildPlatformSoReturnInstockDetailForSoInfoSplit(dto, warehouseEntity, soInfoEntity);
		if (CollectionUtils.isEmpty(splitResult.matchedList)) {
			ServiceException.runError("【海外仓退货入库】来源明细未匹配到映射");
		}
		SoReturnInstockEntity soReturnInstockEntity = this.buildPlatformSoReturnInstockEntityForSoInfo(dto, warehouseEntity, soInfoEntity);
		// 来源编号：该场景按参考单号匹配到B2B销售订单，取销售订单编号作为来源追溯
		if (StringUtils.isNotBlank(soInfoEntity.getCode())) {
			soReturnInstockEntity.setSourceCode(soInfoEntity.getCode());
		}

		// 已匹配到订单的SKU落退货入库单，订单里没有的SKU（unmatchedList）单独落预入库单，两者同一本地事务提交
		this.persistMatchedInstockAndUnmatchedPrestock(soReturnInstockEntity, splitResult.matchedList, dto, warehouseEntity, splitResult.unmatchedList);
	}

	/**
	 * B2C场景拆分版明细构建：逻辑与 {@link #buildPlatformSoReturnInstockDetail} 一致（含就近出库单匹配、
	 * 按订单明细 platformSkuNo/warehouseSkuNo 兜底匹配），唯一区别是SKU在订单里彻底找不到时不抛错，
	 * 而是收集进 unmatchedList，交由调用方单独生成预入库单。已匹配行按平台推送的 mustQty/receiveQty/realQty
	 * 原样落库，不按订单自身库存数量截断
	 */
	private SkuSplitDetailResult buildPlatformSoReturnInstockDetailSplit(PlatformReturnInstockDTO dto, WarehouseEntity warehouseEntity, SoB2cEntity soB2cEntity) {
		List<PlatformReturnInstockDTO.Detail> details = dto.getProductDetailList();
		List<SoB2cDetailEntity> soDetailEntityList = soB2cFeign.listDetailByMainIds(Collections.singletonList(soB2cEntity.getId()));
		SkuSplitDetailResult result = new SkuSplitDetailResult();
		if (CollectionUtils.isEmpty(soDetailEntityList)) {
			log.warn("[海外仓退货入库] 订单{}明细为空，本次明细全部改为生成预入库单", dto.getPlatformOrderNo());
			result.unmatchedList.addAll(details);
			return result;
		}
		// 平台SKU -> 就近出库单的实际出库SKU（出库日期<=退货日期，含当天，取最近一张）
		Map<String, SoOutstockDetailEntity> nearestOutstockSkuMap = buildNearestOutstockSkuMap(dto, soB2cEntity, soDetailEntityList);
		for (PlatformReturnInstockDTO.Detail detail : details) {
			SoOutstockDetailEntity outstockDetail = nearestOutstockSkuMap.get(detail.getProductSku());
			if (nearestOutstockSkuMap.containsKey(detail.getProductSku()) && Objects.nonNull(outstockDetail)) {
				SoReturnInstockDetailEntity soReturnInstockDetailEntity = new SoReturnInstockDetailEntity();
				soReturnInstockDetailEntity.setSkuId(outstockDetail.getSkuId());
				soReturnInstockDetailEntity.setSkuNo(outstockDetail.getSkuNo());
				soReturnInstockDetailEntity.setMustQty(detail.getMustQty());
				soReturnInstockDetailEntity.setReceiveQty(detail.getReceiveQty());
				soReturnInstockDetailEntity.setRealQty(detail.getRealQty());
				soReturnInstockDetailEntity.setWarehouseId(warehouseEntity.getId());
				soReturnInstockDetailEntity.setWarehouseName(warehouseEntity.getName());
				soReturnInstockDetailEntity.setRemark(dto.getReason());
				soReturnInstockDetailEntity.setReturnTypeDict(dto.getReturnType());
				soReturnInstockDetailEntity.setDefectiveProductFlag(detail.getDefectiveProductFlag());
				result.matchedList.add(soReturnInstockDetailEntity);
				continue;
			}
			// 未匹配到就近出库单，按订单明细 platformSkuNo/warehouseSkuNo 兜底匹配
			SoB2cDetailEntity detailEntity = soDetailEntityList.stream()
					.filter(e -> StringUtils.isNotBlank(e.getPlatformSkuNo()) && e.getPlatformSkuNo().equalsIgnoreCase(detail.getProductSku()))
					.findFirst()
					.orElseGet(() -> soDetailEntityList.stream()
							.filter(e -> StringUtils.isNotBlank(e.getWarehouseSkuNo()) && e.getWarehouseSkuNo().equalsIgnoreCase(detail.getProductSku()))
							.findFirst()
							.orElse(null));
			if (Objects.isNull(detailEntity)) {
				// 订单里彻底找不到这个SKU：不再整包报错，收集进unmatchedList单独生成预入库单
				log.warn("[海外仓退货入库] 参考单号匹配到订单{}但订单明细中找不到SKU={}，该行改为生成预入库单", dto.getPlatformOrderNo(), detail.getProductSku());
				result.unmatchedList.add(detail);
				continue;
			}
			log.warn("【平台退货入库】未匹配到就近出库单，按订单映射兜底:订单={}, 平台SKU={}", dto.getPlatformOrderNo(), detail.getProductSku());
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
			result.matchedList.add(soReturnInstockDetailEntity);
		}
		return result;
	}

	/**
	 * B2B销售订单明细按平台SKU直接映射入库SKU（B2B无出库单就近匹配逻辑，字段映射与B2C保持一致）；
	 * SKU在订单里找不到时收集进 unmatchedList，不抛错
	 */
	private SkuSplitDetailResult buildPlatformSoReturnInstockDetailForSoInfoSplit(PlatformReturnInstockDTO dto, WarehouseEntity warehouseEntity, SoInfoEntity soInfoEntity) {
		List<PlatformReturnInstockDTO.Detail> details = dto.getProductDetailList();
		List<SoDetailEntity> soDetailEntityList = soInfoFeign.listSoDetailByMainIds(Collections.singletonList(soInfoEntity.getId()));
		SkuSplitDetailResult result = new SkuSplitDetailResult();
		if (CollectionUtils.isEmpty(soDetailEntityList)) {
			log.warn("[海外仓退货入库] B2B订单{}明细为空，本次明细全部改为生成预入库单", dto.getPlatformOrderNo());
			result.unmatchedList.addAll(details);
			return result;
		}
		for (PlatformReturnInstockDTO.Detail detail : details) {
			SoDetailEntity detailEntity = soDetailEntityList.stream()
					.filter(e -> StringUtils.isNotBlank(e.getPlatformSkuNo()) && e.getPlatformSkuNo().equalsIgnoreCase(detail.getProductSku()))
					.findFirst().orElse(null);
			if (Objects.isNull(detailEntity)) {
				log.warn("[海外仓退货入库] 参考单号匹配到B2B订单{}但订单明细中找不到SKU={}，该行改为生成预入库单", dto.getPlatformOrderNo(), detail.getProductSku());
				result.unmatchedList.add(detail);
				continue;
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
			soReturnInstockDetailEntity.setSourceDetailId(detail.getThirdId());
			soReturnInstockDetailEntity.setCreateUserId(dto.getAuthId());
			soReturnInstockDetailEntity.setPlatformSkuNo(detail.getProductSku());
			soReturnInstockDetailEntity.setDefectiveProductFlag(detail.getDefectiveProductFlag());
			result.matchedList.add(soReturnInstockDetailEntity);
		}
		return result;
	}

	/**
	 * 已匹配退货入库单 + 未匹配预入库单 合并落库：用同一个本地事务包裹两次持久化调用。
	 * TransactionTemplate 默认传播行为 PROPAGATION_REQUIRED，内部 addByThirdWarehouse/
	 * createFromOverseasWhHeadless 各自的事务会直接加入这里开启的事务而不是另开一个，
	 * 保证已匹配退货入库单和未匹配预入库单要么一起提交，要么一起回滚
	 */
	private void persistMatchedInstockAndUnmatchedPrestock(SoReturnInstockEntity soReturnInstockEntity,
															List<SoReturnInstockDetailEntity> matchedList,
															PlatformReturnInstockDTO dto,
															WarehouseEntity warehouseEntity,
															List<PlatformReturnInstockDTO.Detail> unmatchedList) {
		String thirdCode = dto.getPlatformReturnOrderNo();
		int matchedQty = matchedList.stream().mapToInt(v -> Objects.nonNull(v.getMustQty()) ? v.getMustQty() : 0).sum();
		int unmatchedSkuCount = CollectionUtils.isEmpty(unmatchedList) ? 0 : unmatchedList.size();
		int unmatchedQty = CollectionUtils.isEmpty(unmatchedList) ? 0 : unmatchedList.stream().mapToInt(v -> Objects.nonNull(v.getMustQty()) ? v.getMustQty() : 0).sum();
		log.info("[海外仓退货入库-参考单号匹配] thirdCode={} 已匹配{}个SKU共{}件落退货入库单，未匹配{}个SKU共{}件落预入库单",
				thirdCode, matchedList.size(), matchedQty, unmatchedSkuCount, unmatchedQty);
		new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
			soReturnInstockService.addByThirdWarehouse(soReturnInstockEntity, matchedList);
			if (CollectionUtils.isNotEmpty(unmatchedList)) {
				SoReturnPrestockDTO.Add prestockAdd = buildPrestockAddDtoForDetails(dto, warehouseEntity, unmatchedList);
				if (Objects.nonNull(prestockAdd)) {
					soReturnPrestockService.createFromOverseasWhHeadless(prestockAdd);
				}
			}
		});
	}

	/**
	 * 既无退货物流单号又无参考单号：无法关联到任何单据，生成预入库单交由运营人工关联
	 */
	private void createSoReturnPrestockHeadless(PlatformReturnInstockDTO dto, WarehouseEntity warehouseEntity) {
		SoReturnPrestockDTO.Add addDTO = buildPrestockAddDtoForDetails(dto, warehouseEntity, dto.getProductDetailList());
		if (Objects.isNull(addDTO)) {
			log.warn("[海外仓退货入库-无头件] 明细为空，跳过：thirdCode={}", dto.getPlatformReturnOrderNo());
			return;
		}
		soReturnPrestockService.createFromOverseasWhHeadless(addDTO);
	}

	/**
	 * B2B销售订单场景字段映射：客户直接取销售订单上的 customerId（B2B无"店铺-客户关联"环节）
	 */
	private SoReturnInstockEntity buildPlatformSoReturnInstockEntityForSoInfo(PlatformReturnInstockDTO dto, WarehouseEntity warehouseEntity, SoInfoEntity soInfoEntity) {
		SoReturnInstockEntity soReturnInstockEntity = new SoReturnInstockEntity();
		soReturnInstockEntity.setApproveTime(LocalDateTime.now());
		soReturnInstockEntity.setApproveStatus(ApproveStatusEnum.APPROVE_ING.getStatus());
		soReturnInstockEntity.setBillDate(dto.getPutawayLocalDate());
		soReturnInstockEntity.setInventoryOrgId(warehouseEntity.getOrgId());
		SysAccountingCompanyEntity company = sysUserFeign.getCompanyById(warehouseEntity.getOrgId());
		soReturnInstockEntity.setInventoryOrgName(company.getCompanyName());
		soReturnInstockEntity.setWarehouseKeeperId(warehouseEntity.getChargeId());
		soReturnInstockEntity.setApproveUserName("system");
		// sourceCode在此处保持原语义（uniqueId），generateInstockBySoInfo中会按需覆盖为销售订单编号
		soReturnInstockEntity.setSourceCode(dto.getUniqueId());
		soReturnInstockEntity.setSourceType(SourceTypeEnum.PLATFORM_RETURN_INSTOCK.getCode());
		soReturnInstockEntity.setThirdCode(dto.getPlatformReturnOrderNo());
		soReturnInstockEntity.setPlatformOrderCode(dto.getPlatformOrderNo());
		soReturnInstockEntity.setCreated(dto.getCreateTime());
		soReturnInstockEntity.setReturnLogisticCode(dto.getReturnLogisticCode());
		soReturnInstockEntity.setType(BillTypeEnum.B2B.getCode());
		soReturnInstockEntity.setSoId(soInfoEntity.getId());
		soReturnInstockEntity.setSoCode(soInfoEntity.getCode());
		soReturnInstockEntity.setCurrency(soInfoEntity.getCurrency());
		// 部分海外仓来源平台（如WEGO）本身无"平台订单号"概念，只回传参考单号，
		// dto.getPlatformOrderNo()必为空；此时用匹配到的销售订单自身的平台订单号兜底
		if (StringUtils.isBlank(soReturnInstockEntity.getPlatformOrderCode())) {
			soReturnInstockEntity.setPlatformOrderCode(soInfoEntity.getPlatformOrderCode());
		}
		if (StringUtils.isBlank(soInfoEntity.getCustomerId())) {
			ServiceException.runError("B2B销售订单对应客户信息为空:{}", soInfoEntity.getCode());
		}
		CustomerInfoEntity customerInfo = customerFeign.getCustomerById(soInfoEntity.getCustomerId());
		if (null == customerInfo) {
			ServiceException.runError("B2B销售订单对应客户信息不存在:客户ID={}", soInfoEntity.getCustomerId());
		}
		soReturnInstockEntity.setSalesOrgId(customerInfo.getUseOrgId());
		soReturnInstockEntity.setSalesOrgName(customerInfo.getUseOrgName());
		soReturnInstockEntity.setCustomerId(soInfoEntity.getCustomerId());
		soReturnInstockEntity.setCustomerName(customerInfo.getName());
		if (StringUtils.isNotBlank(customerInfo.getSalesDeptId())) {
			SysDepartmentDTO department = sysUserFeign.getUserDeptById(customerInfo.getSalesDeptId());
			if (null != department) {
				soReturnInstockEntity.setSalesDeptId(customerInfo.getSalesDeptId());
				soReturnInstockEntity.setSalesDeptName(department.getName());
			}
		}
		soReturnInstockEntity.setSellerId(customerInfo.getSellerId());
		soReturnInstockEntity.setSellerName(customerInfo.getSellerName());
		return soReturnInstockEntity;
	}

	/**
	 * 组装预入库单新增参数（只负责组装，不落库）：既服务于完全无法关联单据的"无头件"场景，
	 * 也服务于参考单号匹配到订单/退货单后，订单里没有的SKU单独生成预入库单的场景。
	 * 落库动作交给调用方，以便和退货入库单的落库合并进同一个本地事务
	 */
	private SoReturnPrestockDTO.Add buildPrestockAddDtoForDetails(PlatformReturnInstockDTO dto, WarehouseEntity warehouseEntity, List<PlatformReturnInstockDTO.Detail> details) {
		List<SoReturnPrestockDetailDTO.Add> detailList = buildPrestockDetailList(dto, details);
		if (CollectionUtils.isEmpty(detailList)) {
			return null;
		}
		SysAccountingCompanyEntity company = sysUserFeign.getCompanyById(warehouseEntity.getOrgId());
		SoReturnPrestockDTO.Add addDTO = new SoReturnPrestockDTO.Add();
		// 无法判断真实业务类型（完全无参考号，或参考单号匹配到的订单里没有该SKU），按历史惯例默认落为B2C，后续人工在预入库单列表页可自行修正
		addDTO.setType(BillTypeEnum.B2C.getCode());
		addDTO.setReturnLogisticCode("");
		addDTO.setReturnTypeDict(dto.getReturnType());
		addDTO.setInventoryOrgId(warehouseEntity.getOrgId());
		addDTO.setInventoryOrgName(Objects.nonNull(company) ? company.getCompanyName() : "");
		addDTO.setWarehouseId(warehouseEntity.getId());
		addDTO.setWarehouseName(warehouseEntity.getName());
		addDTO.setThirdCode(dto.getPlatformReturnOrderNo());
		addDTO.setRemark(dto.getReason());
		addDTO.setDetailList(detailList);
		return addDTO;
	}

	/**
	 * 由平台退货入库明细构建预入库单明细行；未匹配到 SKU 映射时保留原始平台 SKU，不丢弃、不中断，交给运营人工核对
	 */
	private List<SoReturnPrestockDetailDTO.Add> buildPrestockDetailList(PlatformReturnInstockDTO dto, List<PlatformReturnInstockDTO.Detail> details) {
		if (CollectionUtils.isEmpty(details)) {
			return Collections.emptyList();
		}
		List<String> platformSkuNoList = details.stream().map(PlatformReturnInstockDTO.Detail::getProductSku).collect(Collectors.toList());
		ListingInfoParamDTO listingInfoParamDTO = new ListingInfoParamDTO();
		listingInfoParamDTO.setPlatformSkuNoList(platformSkuNoList);
		listingInfoParamDTO.setAuthId(dto.getAuthId());
		listingInfoParamDTO.setMatchResult(ListingMatchResultEnum.TRUE.getCode());
		List<SkuMappingDTO.MappingSkuViewDTO> mappingSkuViewDTOList = skuMappingFeign.listByPlatformSkuNoAndPlatform(listingInfoParamDTO);

		return details.stream().map(detail -> {
			SkuMappingDTO.MappingSkuViewDTO skuViewDTO = mappingSkuViewDTOList.stream()
					.filter(v -> v.getPlatformSkuNo().equals(detail.getProductSku()))
					.findFirst().orElse(null);
			SoReturnPrestockDetailDTO.Add detailDTO = new SoReturnPrestockDetailDTO.Add();
			detailDTO.setSkuId(Objects.nonNull(skuViewDTO) ? skuViewDTO.getProductSkuId() : "");
			// 未匹配到映射时，仍用平台SKU占位落库，不丢消息，等运营人工在预入库单里核对
			detailDTO.setSkuNo(Objects.nonNull(skuViewDTO) ? skuViewDTO.getProductSkuNo() : detail.getProductSku());
			detailDTO.setProductName(Objects.nonNull(skuViewDTO) ? skuViewDTO.getProductName() : "");
			detailDTO.setReturnQty(detail.getMustQty());
			detailDTO.setReceiveQty(detail.getReceiveQty());
			// 平台订单号、平台字典值均为【关联】相关字段，不代表本行数据来源渠道，此处无头件尚未关联，不写入
			detailDTO.setRemark(dto.getReason());
			return detailDTO;
		}).collect(Collectors.toList());
	}

	/**
	 * 参考单号匹配场景下，按SKU拆分入库明细构建结果：matchedList 落退货入库单，
	 * unmatchedList（订单/退货单没有的SKU）落预入库单，不再因SKU不在订单里而整包报错
	 */
	private static class SkuSplitDetailResult {
		private final List<SoReturnInstockDetailEntity> matchedList = new ArrayList<>();
		private final List<PlatformReturnInstockDTO.Detail> unmatchedList = new ArrayList<>();
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
		List<SoB2cReturnEntity> updateList = soB2cReturnEntityList.stream().filter(v->v.getStatus().equals(SoB2cReturnStatusEnum.TO_BE_RETURNED.getCode())).map(v->v.setStatus((SoB2cReturnStatusEnum.RETURNED.getCode()))).collect(Collectors.toList());
		if(CollectionUtils.isNotEmpty(updateList)){
			log.warn("匹配到销售订单{}的退货单{}，将退货单状态修改为已退货",soB2cEntity.getCode(),updateList.stream().map(SoB2cReturnEntity::getCode).collect(Collectors.joining(",")));
			soB2cReturnFeign.updateBatch(updateList);
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
		// 平台SKU -> 就近出库单的实际出库SKU（出库日期<=退货日期，含当天，取最近一张）
		Map<String, SoOutstockDetailEntity> nearestOutstockSkuMap = buildNearestOutstockSkuMap(dto, soB2cEntity, soDetailEntityList);
		List<SoReturnInstockDetailEntity> detailEntityList = new ArrayList<>();
		for (PlatformReturnInstockDTO.Detail detail : details) {
            SoOutstockDetailEntity outstockDetail = nearestOutstockSkuMap.get(detail.getProductSku());
            if (!nearestOutstockSkuMap.containsKey(detail.getProductSku()) || Objects.isNull(outstockDetail)) {
                // 兜底逻辑
                // 未匹配到就近出库单，按订单映射兜底
                log.warn("【平台退货入库】未匹配到就近出库单，按订单映射兜底:订单={}, 平台SKU={}", dto.getPlatformOrderNo(), detail.getProductSku());
                detailEntityList.add(buildFallbackDetailBySoB2c(detail, soDetailEntityList, warehouseEntity, dto));
                continue;
            }
			SoReturnInstockDetailEntity soReturnInstockDetailEntity = new SoReturnInstockDetailEntity();
			soReturnInstockDetailEntity.setSkuId(outstockDetail.getSkuId());
			soReturnInstockDetailEntity.setSkuNo(outstockDetail.getSkuNo());
			soReturnInstockDetailEntity.setMustQty(detail.getMustQty());
			soReturnInstockDetailEntity.setReceiveQty(detail.getReceiveQty());
			soReturnInstockDetailEntity.setRealQty(detail.getRealQty());
			soReturnInstockDetailEntity.setWarehouseId(warehouseEntity.getId());
			soReturnInstockDetailEntity.setWarehouseName(warehouseEntity.getName());
			soReturnInstockDetailEntity.setRemark(dto.getReason());
			soReturnInstockDetailEntity.setReturnTypeDict(dto.getReturnType());
			soReturnInstockDetailEntity.setDefectiveProductFlag(detail.getDefectiveProductFlag());
			detailEntityList.add(soReturnInstockDetailEntity);
		}
		return detailEntityList;
	}

	/**
	 * 解析销售订单明细可用于匹配第三方SKU的候选值：platformSkuNo（平台/销售渠道SKU）与
	 * warehouseSkuNo（仓库SKU）均纳入，二者均为空时返回空列表。
	 */
	private static List<String> resolveSkuMatchKeys(SoB2cDetailEntity e) {
		List<String> keys = new ArrayList<>(2);
		if (StringUtils.isNotBlank(e.getPlatformSkuNo())) {
			keys.add(e.getPlatformSkuNo());
		}
		if (StringUtils.isNotBlank(e.getWarehouseSkuNo()) && !keys.contains(e.getWarehouseSkuNo())) {
			keys.add(e.getWarehouseSkuNo());
		}
		return keys;
	}

	/**
	 * 构建 第三方SKU -> 就近出库单出库明细 的映射。
	 * 在该订单已审核且未作废、出库日期(bill_date)<=退货日期(含当天)的出库单中，
	 * 取出库日期最近(倒序首条)且包含该第三方SKU的出库明细，以其实际出库SKU为准。
	 * <p>
	 * 匹配键优先取销售订单明细的 platformSkuNo（平台/销售渠道SKU）；部分海外仓来源平台
	 * （如WEGO，属于第三方仓库服务商而非销售渠道）回传的 productSku 实际是仓库侧SKU，
	 * 因此同时把 warehouseSkuNo 纳入匹配键，platformSkuNo 未命中时可用 warehouseSkuNo 兜底。
	 */
	private Map<String, SoOutstockDetailEntity> buildNearestOutstockSkuMap(PlatformReturnInstockDTO dto, SoB2cEntity soB2cEntity, List<SoB2cDetailEntity> soDetailEntityList) {
		LocalDate returnDate = dto.getPutawayLocalDate();
		List<SoOutstockEntity> outstockList = soOutstockService.listBySoIds(Collections.singletonList(soB2cEntity.getId()));
		if (CollectionUtils.isEmpty(outstockList)) {
            log.warn("【平台退货入库】订单无出库单，按订单映射兜底:订单={}", dto.getPlatformOrderNo());
			return Collections.emptyMap();
		}
		List<SoOutstockEntity> validOutstockList = outstockList.stream()
				.filter(e -> ApproveStatusEnum.APPROVE.equals(e.getApproveStatus()))
				.filter(e -> !Boolean.TRUE.equals(e.getInvalidStatus()))
				.filter(e -> Objects.nonNull(e.getBillDate()) && !e.getBillDate().isAfter(returnDate))
				.sorted(Comparator.comparing(SoOutstockEntity::getBillDate).reversed())
				.collect(Collectors.toList());
		if (CollectionUtils.isEmpty(validOutstockList)) {
			return Collections.emptyMap();
		}
		// 出库单id -> 出库日期，用于明细按就近出库单排序
		Map<String, LocalDate> outstockBillDateMap = validOutstockList.stream()
				.collect(Collectors.toMap(SoOutstockEntity::getId, SoOutstockEntity::getBillDate, (a, b) -> a));
        List<SoOutstockDetailEntity> outstockDetailList = Lists.partition(
                        validOutstockList.stream().map(SoOutstockEntity::getId).collect(Collectors.toList()), 1000
                ).stream()
                .flatMap(batch -> soOutstockDetailService.listByMainIds(batch).stream())
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(outstockDetailList)) {
			return Collections.emptyMap();
		}
		// 销售订单明细id -> 候选第三方SKU集合（platformSkuNo优先，warehouseSkuNo兜底）
		Map<String, List<String>> soDetailIdToSkuKeys = soDetailEntityList.stream()
				.filter(e -> StringUtils.isNotBlank(e.getId()))
				.collect(Collectors.toMap(
						SoB2cDetailEntity::getId,
						PlatformNewReturnInstockConsumerService::resolveSkuMatchKeys,
						(a, b) -> a
				));

        // 第三方SKU -> 出库明细，按出库单出库日期倒序，保留最近一张(首次写入即最近)
		Map<String, SoOutstockDetailEntity> nearestOutstockSkuMap = new HashMap<>();
		outstockDetailList.stream()
				.filter(d -> StringUtils.isNotBlank(d.getSoDetailId()) && soDetailIdToSkuKeys.containsKey(d.getSoDetailId()))
				.sorted(Comparator.comparing((SoOutstockDetailEntity d) -> outstockBillDateMap.getOrDefault(d.getMainId(), LocalDate.MIN)).reversed())
				.forEach(d -> soDetailIdToSkuKeys.get(d.getSoDetailId())
						.forEach(key -> nearestOutstockSkuMap.putIfAbsent(key, d)));
		return nearestOutstockSkuMap;
	}

	/**
	 * 兜底：按销售订单明细的第三方SKU映射取入库SKU（原逻辑）。
	 * 优先匹配 platformSkuNo（平台/销售渠道SKU），未命中时按 warehouseSkuNo（仓库SKU）兜底匹配——
	 * 部分海外仓来源平台（如WEGO）回传的 productSku 实际是仓库侧SKU而非平台SKU。
	 */
	private SoReturnInstockDetailEntity buildFallbackDetailBySoB2c(PlatformReturnInstockDTO.Detail detail, List<SoB2cDetailEntity> soDetailEntityList, WarehouseEntity warehouseEntity, PlatformReturnInstockDTO dto) {
		SoB2cDetailEntity detailEntity = soDetailEntityList.stream()
				.filter(e -> StringUtils.isNotBlank(e.getPlatformSkuNo()) && e.getPlatformSkuNo().equalsIgnoreCase(detail.getProductSku()))
				.findFirst()
				.orElseGet(() -> soDetailEntityList.stream()
						.filter(e -> StringUtils.isNotBlank(e.getWarehouseSkuNo()) && e.getWarehouseSkuNo().equalsIgnoreCase(detail.getProductSku()))
						.findFirst()
						.orElse(null));
		if (null == detailEntity) {
            ServiceException.runError("找不到销售订单明细:订单={}, 平台SKU={}", dto.getPlatformOrderNo(), detail.getProductSku());
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
		return soReturnInstockDetailEntity;
	}

	private SoReturnInstockEntity buildPlatformSoReturnInstockEntity(PlatformReturnInstockDTO dto,
															 WarehouseEntity warehouseEntity,
															 SoB2cEntity soB2cEntity,
															 ShopInfoEntity shopInfoEntity
	){
		SoReturnInstockEntity soReturnInstockEntity = new SoReturnInstockEntity();
		soReturnInstockEntity.setApproveTime(LocalDateTime.now());
		soReturnInstockEntity.setApproveStatus(ApproveStatusEnum.APPROVE_ING.getStatus());
		soReturnInstockEntity.setBillDate(dto.getPutawayLocalDate());
		soReturnInstockEntity.setInventoryOrgId(warehouseEntity.getOrgId());
		//组织信息
		SysAccountingCompanyEntity company = sysUserFeign.getCompanyById(warehouseEntity.getOrgId());
		soReturnInstockEntity.setInventoryOrgName(company.getCompanyName());
		soReturnInstockEntity.setWarehouseKeeperId(warehouseEntity.getChargeId());
		soReturnInstockEntity.setApproveUserName("system");
		// 注意：sourceCode在平台仓入库场景（platformWarehouseHandle）用于按uniqueId去重，
		// 此处保持原语义不变；海外仓场景（generateInstockBySo/generateInstockByMatchedReturn）
		// 会在各自调用处按需覆盖为对应销售订单编号，不在此处统一处理
		soReturnInstockEntity.setSourceCode(dto.getUniqueId());
		soReturnInstockEntity.setSourceType(SourceTypeEnum.PLATFORM_RETURN_INSTOCK.getCode());
		soReturnInstockEntity.setThirdCode(dto.getPlatformReturnOrderNo());
		soReturnInstockEntity.setPlatformOrderCode(dto.getPlatformOrderNo());
		soReturnInstockEntity.setCreated(dto.getCreateTime());
		soReturnInstockEntity.setReturnLogisticCode(dto.getReturnLogisticCode());
		soReturnInstockEntity.setType(BillTypeEnum.B2C.getCode());
		if(Objects.nonNull(soB2cEntity)) {
			soReturnInstockEntity.setSoId(soB2cEntity.getId());
			soReturnInstockEntity.setSoCode(soB2cEntity.getCode());
			soReturnInstockEntity.setCurrency(soB2cEntity.getCurrency());
			// 部分海外仓来源平台（如WEGO）本身无"平台订单号"概念，只回传参考单号，
			// dto.getPlatformOrderNo()必为空；此时用匹配到的销售订单自身的平台订单号兜底
			if (StringUtils.isBlank(soReturnInstockEntity.getPlatformOrderCode())) {
				soReturnInstockEntity.setPlatformOrderCode(soB2cEntity.getPlatformCode());
			}
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
		LocalDate closedLocalDate = inventoryClosedRecordService.checkClosed(warehouseEntity.getOrgId(), dto.getPutawayLocalDate());
		if (null != closedLocalDate) {
			// 已关账
			log.warn("[退货入库单消费]:当前退货入库单消费日期【{}】因关账【{}】停止生成：单号={}", dto.getPutawayLocalDate(), closedLocalDate, dto.getPlatformOrderNo());
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
		paramDTO.setType(RuleTypeEnum.B2C_PLATFORM.getCode());
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