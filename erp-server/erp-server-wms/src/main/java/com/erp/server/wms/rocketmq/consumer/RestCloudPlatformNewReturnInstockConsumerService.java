package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.PlatformReturnInstockDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractRestCloudPlatformConsumerHandler;
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
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.SoB2cReturnEntity;
import com.erp.model.oms.entity.SoB2cReturnDetailEntity;
import com.erp.model.wms.dto.SoReturnPrestockDTO;
import com.erp.model.wms.dto.SoReturnPrestockDetailDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.*;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.service.*;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 下载平台退货入库数据消费服务
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqNewTopic.RESTCLOUD_PLATFORM_RETURN_INSTOCK_TO_WMS_TOPIC,
selectorExpression = RocketMqNewTag.RESTCLOUD_PLATFORM_RETURN_INSTOCK_TO_WMS_TAG,
consumerGroup = RocketMqNewConsumerGroup.RESTCLOUD_PLATFORM_RETURN_INSTOCK_TO_WMS_GROUP,
consumeMode = ConsumeMode.ORDERLY)
public class RestCloudPlatformNewReturnInstockConsumerService extends AbstractRestCloudPlatformConsumerHandler {

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
	private SoReturnPrestockService soReturnPrestockService;

	@Resource
	private ReturnInstockGapReconciler returnInstockGapReconciler;

	@Resource
	private SoB2cReturnFeign soB2cReturnFeign;

	@Resource
	private ThirdWarehouseDeliveryService thirdWarehouseDeliveryService;

	@Resource
	private B2bThirdDeliveryService b2bThirdDeliveryService;

	@Resource
	private DmpTaskFeign dmpTaskFeign;

	@Resource
	private PlatformTransactionManager transactionManager;


	@Override
	public String getBizName() {
		return "平台退货入库";
	}

	@Override
	public void handle(String data) {
		PlatformReturnInstockDTO dto = JSONUtil.toBean(data, PlatformReturnInstockDTO.class);
		if (Objects.isNull(dto.getPutawayTime())) {
			// putawayTime缺失将由getPutawayLocalDate()兜底createTime/当前日期，此处打日志便于后续核对单据日期是否与真实业务日期一致
			log.warn("[平台退货入库]上架完成时间(putawayTime)缺失，将兜底createTime/当前日期：platform={}，平台单号={}，平台退货单号={}",
					dto.getPlatform(), dto.getPlatformOrderNo(), dto.getPlatformReturnOrderNo());
		}
		if (PlatformDictEnum.AMAZON.getCode().equalsIgnoreCase(dto.getPlatform()) || PlatformDictEnum.NASDAQ_JD.getCode().equalsIgnoreCase(dto.getPlatform())){
			String thisPlatform = PlatformDictEnum.AMAZON.getCode().equalsIgnoreCase(dto.getPlatform()) ?  PlatformDictEnum.AMAZON.getCode() : PlatformDictEnum.NASDAQ_JD.getCode();
			dto.setPlatform(thisPlatform);
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
		if(Objects.isNull(dto) || CharSequenceUtil.isBlank(dto.getAuthId())){
			return;
		}
		//根据产品条码查询sku
		handleThirdBarcode(dto);

		if (PlatformDictEnum.GOOD_CANG.getCode().equalsIgnoreCase(dto.getPlatform())){
			if (CollectionUtils.isEmpty(dto.getProductDetailList())){
				ServiceException.runError("谷仓退货入库明细流水为空");
			}
			String thirdId = dto.getProductDetailList().get(0).getThirdId();
			if (StringUtils.isBlank(thirdId)){
				ServiceException.runError("谷仓退货入库明细流水thirdId不能为空");
			}
			// 谷仓按明细ID判断；命中时补偿售后状态（入库已成功但 Feign 改状态失败后的重试路径）
			SoReturnInstockDetailEntity existDetail = soReturnInstockDetailService.lambdaQuery()
					.eq(SoReturnInstockDetailEntity::getSourceDetailId, thirdId)
					.eq(SoReturnInstockDetailEntity::getCreateUserId, dto.getAuthId())
					.last("limit 1")
					.one();
			if (Objects.nonNull(existDetail)) {
				compensateReturnStatusIfNeeded(soReturnInstockService.getById(existDetail.getMainId()));
				return;
			}
		}else if (PlatformDictEnum.DA_MAI.getCode().equalsIgnoreCase(dto.getPlatform())){
			SoReturnInstockEntity exist = soReturnInstockService.getBySourceId(dto.getSourceId());
			if(Objects.nonNull(exist)){
				compensateReturnStatusIfNeeded(exist);
				return;
			}
		} else {
			// 默认平台：按明细缺口对账，禁止「存在任意一条退货入库单就整单跳过」
			List<PlatformReturnInstockDTO.Detail> unprocessed = returnInstockGapReconciler.resolveUnprocessedDetails(
					dto, this::resolveSkuMappingByPlatformSkuNo);
			if (CollectionUtils.isEmpty(unprocessed)) {
				log.warn("[海外仓退货入库] thirdCode={} 推送明细已全部被退货入库单/预入库单覆盖，本次消息跳过",
						dto.getPlatformReturnOrderNo());
				// 入库/预入库已齐但售后可能仍为待退货（落库成功、Feign 失败后重试），补偿同步状态
				compensateReturnStatusByThirdCode(dto.getPlatformReturnOrderNo());
				return;
			}
			if (unprocessed.size() != dto.getProductDetailList().size()
					|| !returnInstockGapReconciler.sameDetailQtyFingerprint(unprocessed, dto.getProductDetailList())) {
				int pushQty = returnInstockGapReconciler.sumPushQty(dto.getProductDetailList());
				int gapQty = returnInstockGapReconciler.sumPushQty(unprocessed);
				log.warn("[海外仓退货入库] thirdCode={} 存在已处理明细，本次仅处理缺口：推送明细行数={}、推送数量={}，缺口行数={}、缺口数量={}",
						dto.getPlatformReturnOrderNo(),
						dto.getProductDetailList().size(), pushQty,
						unprocessed.size(), gapQty);
				dto.setProductDetailList(unprocessed);
			}
		}

		if (isLegacySpecialWarehousePlatform(dto.getPlatform())) {
			// 众包/极兔/艾姆勒/通邮：金额计算、B2B三方发货单匹配等能力为统一匹配流程未覆盖的历史专属逻辑，独立分支处理，不进入下方统一匹配
			handleLegacySpecialWarehouse(dto);
			return;
		}

		// 仓库统一走 overseasProviderWarehouseService 解析
		// （艾姆勒 IML / 统佑历史上依赖订单出库明细解析仓库，此处暂不支持，解析不到时记录 error 并跳过，
		// 属于已知限制，留给后续实现「留空」分支时一并重新设计）
		OverseasProviderWarehouseEntity overseasProviderWarehouseEntity = overseasProviderWarehouseService.getByPlatform(dto.getAuthId(), dto.getWarehouseCode());
		if (Objects.isNull(overseasProviderWarehouseEntity) || CharSequenceUtil.isBlank(overseasProviderWarehouseEntity.getWarehouseId())) {
			// 故意 return 而非抛异常触发 MQ 重试：
			// 1) 众包/极兔/艾姆勒/通邮等历史平台已在上方 isLegacySpecialWarehousePlatform 分流，不会走到这里；
			// 2) 能落到此处多为仓库映射未配置的持久性配置问题，重试无法自愈，只会空转至死信；
			// 3) 用 error 日志告警，待运营补齐映射或后续「留空仓库」分支落地后再改处理策略（补偿表/告警），勿改成 throw。
			log.error("[海外仓退货入库-无头件] 仓库信息缺失，跳过生成预入库单：platform={}, warehouseCode={}, thirdCode={}",
					dto.getPlatform(), dto.getWarehouseCode(), dto.getPlatformReturnOrderNo());
			return;
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
				generateInstockBySo(dto, warehouseEntity, soB2cEntity, resolveSkuMappingByPlatformSkuNo(dto));
				return true;
			}
		}
		return false;
	}

	/**
	 * 按参考单号依次匹配《B2C售后单-退货单》、《B2B/B2C销售订单》，命中则生成退货入库单并返回 true，均未匹配返回 false
	 * <p>
	 * 平台SKU -> ERP SKU 映射只在本方法入口处调用一次 Feign（{@link #resolveSkuMappingByPlatformSkuNo}），
	 * 并作为参数向下传递给候选退货单SKU匹配、销售订单SKU匹配、明细拆分等步骤复用，避免同一批
	 * platformSkuNoList/authId 在一次消息处理内被重复请求（此前候选退货单匹配、销售订单候选匹配、
	 * 明细拆分三处各自独立调用，最多产生3次内容相同的远程调用）
	 */
	private boolean handleReferenceNoAfterSaleMatch(PlatformReturnInstockDTO dto, WarehouseEntity warehouseEntity) {
		Map<String, SkuMappingDTO.MappingSkuViewDTO> skuMappingMap = resolveSkuMappingByPlatformSkuNo(dto);
		Set<String> incomingSkuIds = toSkuIdSet(skuMappingMap);
		List<SoB2cReturnEntity> candidates = matchB2cReturnCandidates(dto.getOrderReferenceNo());
		SoB2cReturnEntity matchedReturn = CollectionUtils.isEmpty(candidates) ? null : pickReturnBySkuMatch(candidates, incomingSkuIds);
		if (Objects.nonNull(matchedReturn)) {
			// null=无法按退货单落库（回退原明细继续匹配）；empty=本批已处理完；non-empty=退货缺口已扣，剩余继续销售订单匹配/预入库
			List<PlatformReturnInstockDTO.Detail> remainingAfterReturn =
					generateInstockByMatchedReturn(dto, warehouseEntity, matchedReturn, skuMappingMap);
			if (Objects.nonNull(remainingAfterReturn)) {
				if (CollectionUtils.isEmpty(remainingAfterReturn)) {
					return true;
				}
				dto.setProductDetailList(remainingAfterReturn);
			}
		}
		SoB2cEntity soB2cEntity = matchB2cSoByReferenceNo(dto.getOrderReferenceNo(), incomingSkuIds);
		if (Objects.nonNull(soB2cEntity)) {
			generateInstockBySo(dto, warehouseEntity, soB2cEntity, skuMappingMap);
			return true;
		}
		SoInfoEntity soInfoEntity = matchB2bSoByReferenceNo(dto.getOrderReferenceNo(), incomingSkuIds);
		if (Objects.nonNull(soInfoEntity)) {
			generateInstockBySoInfo(dto, warehouseEntity, soInfoEntity, skuMappingMap);
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
	 * 按SKU维度从候选退货单中挑选可匹配的一条：
	 * 优先"待退货"，且要求推送 SKU 在该单上仍有未入库缺口；已全部入库的候选跳过，避免误关联已完结单据。
	 */
	private SoB2cReturnEntity pickReturnBySkuMatch(List<SoB2cReturnEntity> candidates, Set<String> incomingSkuIds) {
		if (CollectionUtils.isEmpty(incomingSkuIds)) {
			return null;
		}
		Map<String, List<ReturnInstockGapReconciler.B2cReturnGap>> gapsByMainId =
				returnInstockGapReconciler.loadOpenB2cReturnGapsByMains(candidates);
		List<SoB2cReturnEntity> sorted = candidates.stream()
				.sorted(Comparator.comparing(v -> SoB2cReturnStatusEnum.TO_BE_RETURNED.getCode().equals(v.getStatus()) ? 0 : 1))
				.collect(Collectors.toList());
		for (SoB2cReturnEntity candidate : sorted) {
			List<ReturnInstockGapReconciler.B2cReturnGap> openGaps =
					gapsByMainId.getOrDefault(candidate.getId(), Collections.emptyList());
			boolean skuMatched = openGaps.stream()
					.anyMatch(g -> StringUtils.isNotBlank(g.getSkuId()) && incomingSkuIds.contains(g.getSkuId()));
			if (skuMatched) {
				return candidate;
			}
		}
		return null;
	}

	/**
	 * 从"平台SKU -> ERP SKU映射"结果中提取 ERP SKU ID 集合（仅保留已成功映射的），纯内存转换、不发起远程调用。
	 * 原先通过独立的 resolveSkuIds(dto) 单独调用一次 Feign 获取，与
	 * {@link #resolveSkuMappingByPlatformSkuNo} 请求参数完全一致，属重复远程调用，现统一改为基于
	 * 后者的结果做二次派生，调用方应保证在一次消息处理内只调用一次 resolveSkuMappingByPlatformSkuNo。
	 */
	private Set<String> toSkuIdSet(Map<String, SkuMappingDTO.MappingSkuViewDTO> skuMappingMap) {
		if (skuMappingMap == null || skuMappingMap.isEmpty()) {
			return Collections.emptySet();
		}
		return skuMappingMap.values().stream()
				.map(SkuMappingDTO.MappingSkuViewDTO::getProductSkuId)
				.filter(StringUtils::isNotBlank)
				.collect(Collectors.toSet());
	}

	/**
	 * 解析退货入库明细的"平台原始SKU -> ERP SKU映射"（key为平台原始 productSku，大小写不敏感）。
	 * <p>
	 * 拆分明细落库（{@link #buildPlatformSoReturnInstockDetailSplit}、
	 * {@link #buildPlatformSoReturnInstockDetailForSoInfoSplit}）需要按 ERP skuId 与订单明细比较，
	 * 而不能直接拿平台原始 productSku 去和订单的 platformSkuNo/warehouseSkuNo 做字符串比较——
	 * 部分海外仓平台（如 WEGO）回传的 productSku 是平台侧仓库SKU（如 10386），与订单记录的
	 * platformSkuNo/warehouseSkuNo 不是同一套编码，只有经过 listing/SKU映射转换成 ERP skuId
	 * （如 2717）后才能与订单明细的 skuId 对上；直接字符串比较会导致本应命中的行被误判为
	 * "订单里找不到"，改走预入库单而非退货入库单。
	 * <p>
	 * 调用方须保证同一次消息处理只调用本方法一次，并把结果向下传递复用，避免对同一批
	 * platformSkuNoList/authId 反复发起相同的 Feign 请求。
	 */
	private Map<String, SkuMappingDTO.MappingSkuViewDTO> resolveSkuMappingByPlatformSkuNo(PlatformReturnInstockDTO dto) {
		List<PlatformReturnInstockDTO.Detail> details = dto.getProductDetailList();
		if (CollectionUtils.isEmpty(details)) {
			return Collections.emptyMap();
		}
		List<String> platformSkuNoList = details.stream().map(PlatformReturnInstockDTO.Detail::getProductSku).collect(Collectors.toList());
		ListingInfoParamDTO listingInfoParamDTO = new ListingInfoParamDTO();
		listingInfoParamDTO.setPlatformSkuNoList(platformSkuNoList);
		listingInfoParamDTO.setAuthId(dto.getAuthId());
		listingInfoParamDTO.setMatchResult(ListingMatchResultEnum.TRUE.getCode());
		List<SkuMappingDTO.MappingSkuViewDTO> mappingSkuViewDTOList = skuMappingFeign.listByPlatformSkuNoAndPlatform(listingInfoParamDTO);
		Map<String, SkuMappingDTO.MappingSkuViewDTO> mappingMap = new HashMap<>();
		for (SkuMappingDTO.MappingSkuViewDTO mapping : mappingSkuViewDTOList) {
			if (StringUtils.isBlank(mapping.getPlatformSkuNo()) || StringUtils.isBlank(mapping.getProductSkuId())) {
				continue;
			}
			String key = mapping.getPlatformSkuNo().toUpperCase();
			SkuMappingDTO.MappingSkuViewDTO existing = mappingMap.putIfAbsent(key, mapping);
			// 同一 authId+platformSkuNo 命中多条指向不同ERP SKU的有效映射时，接口返回顺序是否具备
			// 稳定的生效时间/更新时间排序保证不在本方法可确认范围内，此处仅保留第一条并打日志留痕，
			// 出现该日志需人工核实映射数据是否重复/冲突
			if (existing != null && !existing.getProductSkuId().equals(mapping.getProductSkuId())) {
				log.warn("[海外仓退货入库] authId={} 平台SKU={} 命中多条ERP SKU映射（{} / {}），按接口返回顺序保留第一条，请人工核实映射数据",
						dto.getAuthId(), mapping.getPlatformSkuNo(), existing.getProductSkuId(), mapping.getProductSkuId());
			}
		}
		return mappingMap;
	}

	/**
	 * 命中退货单后，按退货单明细尚未入库缺口扣减推送量并生成《已审核-退货入库单》。
	 * <p>
	 * 仅缺口命中部分关联该退货单；超出数量与退货单不存在的 SKU 作为剩余明细返回，
	 * 由调用方继续销售订单匹配或预入库。售后状态仅在该退货单全部应退完成入库后改为已退货。
	 * </p>
	 *
	 * @return {@code null} 无法按退货单落库（订单/店铺缺失或无缺口可分配），交由调用方用原明细继续匹配；
	 *         空列表表示本批已处理完；非空列表为剩余推送明细
	 */
	private List<PlatformReturnInstockDTO.Detail> generateInstockByMatchedReturn(PlatformReturnInstockDTO dto, WarehouseEntity warehouseEntity,
																				SoB2cReturnEntity matchedReturn,
																				Map<String, SkuMappingDTO.MappingSkuViewDTO> skuMappingMap) {
		SoB2cEntity soB2cEntity = soB2cFeign.getById(matchedReturn.getSoId());
		if (Objects.isNull(soB2cEntity)) {
			log.warn("[海外仓退货入库] 匹配到退货单{}但对应销售订单不存在，回退到销售订单匹配：soId={}", matchedReturn.getCode(), matchedReturn.getSoId());
			return null;
		}
		ShopInfoEntity shopInfoEntity = shopInfoFeign.getShopInfoById(soB2cEntity.getShopId());
		if (Objects.isNull(shopInfoEntity)) {
			log.warn("[海外仓退货入库] 匹配到退货单{}但对应店铺不存在，回退到销售订单匹配：shopId={}", matchedReturn.getCode(), soB2cEntity.getShopId());
			return null;
		}
		List<ReturnInstockGapReconciler.B2cReturnGap> openGaps = returnInstockGapReconciler.loadOpenB2cReturnGaps(matchedReturn);
		if (CollectionUtils.isEmpty(openGaps)) {
			log.warn("[海外仓退货入库] 退货单{}已无未入库缺口，回退到销售订单匹配", matchedReturn.getCode());
			return null;
		}
		SkuSplitDetailResult splitResult = this.buildPlatformSoReturnInstockDetailSplit(dto, warehouseEntity, soB2cEntity, skuMappingMap);
		if (CollectionUtils.isEmpty(splitResult.matchedList)) {
			log.warn("[海外仓退货入库] 退货单{}对应销售订单明细中一个SKU都匹配不上，回退剩余明细继续匹配：soCode={}",
					matchedReturn.getCode(), soB2cEntity.getCode());
			return null;
		}
		ReturnInstockGapReconciler.ReturnGapAllocationResult allocation = returnInstockGapReconciler.allocateAgainstB2cReturnGaps(
				splitResult.matchedList, splitResult.unmatchedList, openGaps, dto.getReturnType());
		if (CollectionUtils.isEmpty(allocation.getReturnLinkedList())) {
			log.warn("[海外仓退货入库] 退货单{}与本次推送SKU无数量可分配缺口，回退到销售订单匹配", matchedReturn.getCode());
			return null;
		}

		SoReturnInstockEntity soReturnInstockEntity = this.buildPlatformSoReturnInstockEntity(dto, warehouseEntity, soB2cEntity, shopInfoEntity);
		if (StringUtils.isNotBlank(soB2cEntity.getId())) {
			soReturnInstockEntity.setSourceId(soB2cEntity.getId());
		}
		if (StringUtils.isNotBlank(matchedReturn.getSoCode())) {
			soReturnInstockEntity.setSourceCode(matchedReturn.getSoCode());
		}
		soReturnInstockEntity.setSoReturnId(matchedReturn.getId());
		soReturnInstockEntity.setSoReturnCode(matchedReturn.getCode());

		// 仅落关联退货单的缺口命中部分；剩余明细交调用方继续销售订单匹配/预入库（不再在此处直接预入库）
		this.persistMatchedInstockAndUnmatchedPrestock(soReturnInstockEntity, allocation.getReturnLinkedList(),
				dto, warehouseEntity, Collections.emptyList());
		if (allocation.isAllGapsClosed()
				&& SoB2cReturnStatusEnum.TO_BE_RETURNED.getCode().equals(matchedReturn.getStatus())) {
			matchedReturn.setStatus(SoB2cReturnStatusEnum.RETURNED.getCode());
			soB2cReturnFeign.updateBatch(Collections.singletonList(matchedReturn));
		}
		return allocation.getRemainingPushDetails();
	}

	/**
	 * 按 thirdCode 查找已落库退货入库单，补偿同步关联售后状态。
	 * 用于「推送明细已全部覆盖」幂等跳过路径。
	 *
	 * @param thirdCode 平台退货单号
	 */
	private void compensateReturnStatusByThirdCode(String thirdCode) {
		if (CharSequenceUtil.isBlank(thirdCode)) {
			return;
		}
		List<SoReturnInstockEntity> existList = soReturnInstockService.listByThirdCode(thirdCode);
		if (CollectionUtils.isEmpty(existList)) {
			return;
		}
		for (SoReturnInstockEntity exist : existList) {
			compensateReturnStatusIfNeeded(exist);
		}
	}

	/**
	 * 本地入库已落库但售后状态可能尚未同步（Feign 失败后 MQ 重试命中幂等跳过）时，
	 * 在退货单全部应退数量已入库完成后，补偿将关联退货单从待退货改为已退货。
	 * 补偿本身可幂等：已是已退货或仍有未入库缺口则 no-op。
	 *
	 * @param existEntity 已存在的退货入库单；无关联售后单时直接返回
	 */
	private void compensateReturnStatusIfNeeded(SoReturnInstockEntity existEntity) {
		if (Objects.isNull(existEntity) || CharSequenceUtil.isBlank(existEntity.getSoReturnId())) {
			return;
		}
		List<SoB2cReturnEntity> returns = soB2cReturnFeign.listByIds(Collections.singletonList(existEntity.getSoReturnId()));
		if (CollectionUtils.isEmpty(returns)) {
			log.warn("[海外仓退货入库] 补偿售后状态跳过：入库单{}关联的退货单{}不存在",
					existEntity.getCode(), existEntity.getSoReturnId());
			return;
		}
		SoB2cReturnEntity soReturn = returns.get(0);
		if (!SoB2cReturnStatusEnum.TO_BE_RETURNED.getCode().equals(soReturn.getStatus())) {
			return;
		}
		if (!returnInstockGapReconciler.isB2cReturnFullyInstocked(soReturn.getId())) {
			log.warn("[海外仓退货入库] 补偿售后状态跳过：退货单{}仍有未入库缺口（入库单：{}）",
					soReturn.getCode(), existEntity.getCode());
			return;
		}
		soReturn.setStatus(SoB2cReturnStatusEnum.RETURNED.getCode());
		try {
			soB2cReturnFeign.updateBatch(Collections.singletonList(soReturn));
			log.warn("[海外仓退货入库] 补偿将退货单{}由待退货更新为已退货（入库单已存在：{}）",
					soReturn.getCode(), existEntity.getCode());
		} catch (RuntimeException e) {
			log.warn("[海外仓退货入库] 补偿更新退货单{}状态失败，将触发MQ重试：{}", soReturn.getCode(), e.getMessage());
			throw e;
		}
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
		List<SoB2cEntity> validCandidates = candidates.stream()
				.filter(v -> !Boolean.TRUE.equals(v.getInvalidStatus()))
				.filter(v -> SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(v.getBillStatus()))
				.collect(Collectors.toList());
		Set<String> soIdsWithMatchingSku = soIdsWithMatchingB2cSku(validCandidates, incomingSkuIds);
		return validCandidates.stream()
				.filter(v -> soIdsWithMatchingSku.contains(v.getId()))
				.findFirst()
				.orElse(null);
	}

	/**
	 * 批量一次性取全部候选B2C销售订单的明细，返回其中存在命中ERP SKU的订单ID集合，
	 * 避免逐个候选订单发起Feign查询（此前 soB2cHasMatchingSku 在 Stream.filter 中被每个候选调用一次）
	 */
	private Set<String> soIdsWithMatchingB2cSku(List<SoB2cEntity> candidates, Set<String> incomingSkuIds) {
		if (CollectionUtils.isEmpty(candidates) || CollectionUtils.isEmpty(incomingSkuIds)) {
			return Collections.emptySet();
		}
		List<String> candidateIds = candidates.stream().map(SoB2cEntity::getId).collect(Collectors.toList());
		List<SoB2cDetailEntity> allDetails = soB2cFeign.listDetailByMainIds(candidateIds);
		if (CollectionUtils.isEmpty(allDetails)) {
			return Collections.emptySet();
		}
		return allDetails.stream()
				.filter(d -> StringUtils.isNotBlank(d.getSkuId()) && incomingSkuIds.contains(d.getSkuId()))
				.map(SoB2cDetailEntity::getMainId)
				.collect(Collectors.toSet());
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
		List<SoInfoEntity> validCandidates = candidates.stream()
				.filter(v -> !Boolean.TRUE.equals(v.getInvalidStatus()))
				.collect(Collectors.toList());
		Set<String> soInfoIdsWithMatchingSku = soInfoIdsWithMatchingSku(validCandidates, incomingSkuIds);
		return validCandidates.stream()
				.filter(v -> soInfoIdsWithMatchingSku.contains(v.getId()))
				.findFirst()
				.orElse(null);
	}

	/**
	 * 批量一次性取全部候选B2B销售订单的明细，返回其中存在命中ERP SKU的订单ID集合，
	 * 避免逐个候选订单发起Feign查询（此前 soInfoHasMatchingSku 在 Stream.filter 中被每个候选调用一次）
	 */
	private Set<String> soInfoIdsWithMatchingSku(List<SoInfoEntity> candidates, Set<String> incomingSkuIds) {
		if (CollectionUtils.isEmpty(candidates) || CollectionUtils.isEmpty(incomingSkuIds)) {
			return Collections.emptySet();
		}
		List<String> candidateIds = candidates.stream().map(SoInfoEntity::getId).collect(Collectors.toList());
		List<SoDetailEntity> allDetails = soInfoFeign.listSoDetailByMainIds(candidateIds);
		if (CollectionUtils.isEmpty(allDetails)) {
			return Collections.emptySet();
		}
		return allDetails.stream()
				.filter(d -> StringUtils.isNotBlank(d.getSkuId()) && incomingSkuIds.contains(d.getSkuId()))
				.map(SoDetailEntity::getMainId)
				.collect(Collectors.toSet());
	}

	/**
	 * 命中销售订单后生成《已审核-退货入库单》，字段映射与已存在销售订单场景（{@link #createByExistSoB2c}）保持一致，
	 * 仅仓库改用海外仓映射的 warehouseEntity
	 */
	private void generateInstockBySo(PlatformReturnInstockDTO dto, WarehouseEntity warehouseEntity, SoB2cEntity soB2cEntity,
									  Map<String, SkuMappingDTO.MappingSkuViewDTO> skuMappingMap) {
		ShopInfoEntity shopInfoEntity = shopInfoFeign.getShopInfoById(soB2cEntity.getShopId());
		if (Objects.isNull(shopInfoEntity)) {
			log.warn("[海外仓退货入库] 参考单号匹配到销售订单{}但对应店铺不存在，生成无关联预入库单：shopId={}", soB2cEntity.getCode(), soB2cEntity.getShopId());
			this.createSoReturnPrestockHeadless(dto, warehouseEntity);
			return;
		}
		SkuSplitDetailResult splitResult = this.buildPlatformSoReturnInstockDetailSplit(dto, warehouseEntity, soB2cEntity, skuMappingMap);
		if (CollectionUtils.isEmpty(splitResult.matchedList)) {
			// 整批SKU在销售订单里都对不上：不再整批抛错中断，全部明细改走预入库单，避免消息卡死重试
			log.warn("[海外仓退货入库] 参考单号匹配到销售订单{}但一个SKU都匹配不上，全部改为生成预入库单", soB2cEntity.getCode());
		}
		SoReturnInstockEntity soReturnInstockEntity = this.buildPlatformSoReturnInstockEntity(dto, warehouseEntity, soB2cEntity, shopInfoEntity);
		// 来源编号：该场景按参考单号直接匹配到销售订单（未命中具体退货单），取销售订单编号作为来源追溯
		if (StringUtils.isNotBlank(soB2cEntity.getId())) {
			soReturnInstockEntity.setSourceId(soB2cEntity.getId());
		}
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
	private void generateInstockBySoInfo(PlatformReturnInstockDTO dto, WarehouseEntity warehouseEntity, SoInfoEntity soInfoEntity,
										  Map<String, SkuMappingDTO.MappingSkuViewDTO> skuMappingMap) {
		SkuSplitDetailResult splitResult = this.buildPlatformSoReturnInstockDetailForSoInfoSplit(dto, warehouseEntity, soInfoEntity, skuMappingMap);
		if (CollectionUtils.isEmpty(splitResult.matchedList)) {
			// 整批SKU在B2B销售订单里都对不上：不再整批抛错中断，全部明细改走预入库单，避免消息卡死重试
			log.warn("[海外仓退货入库] 参考单号匹配到B2B销售订单{}但一个SKU都匹配不上，全部改为生成预入库单", soInfoEntity.getCode());
		}
		SoReturnInstockEntity soReturnInstockEntity = this.buildPlatformSoReturnInstockEntityForSoInfo(dto, warehouseEntity, soInfoEntity);
		// 来源编号：该场景按参考单号匹配到B2B销售订单，取销售订单编号作为来源追溯
		if (StringUtils.isNotBlank(soInfoEntity.getId())) {
			soReturnInstockEntity.setSourceId(soInfoEntity.getId());
		}
		if (StringUtils.isNotBlank(soInfoEntity.getCode())) {
			soReturnInstockEntity.setSourceCode(soInfoEntity.getCode());
		}

		// 已匹配到订单的SKU落退货入库单，订单里没有的SKU（unmatchedList）单独落预入库单，两者同一本地事务提交
		this.persistMatchedInstockAndUnmatchedPrestock(soReturnInstockEntity, splitResult.matchedList, dto, warehouseEntity, splitResult.unmatchedList);
	}

	/**
	 * B2C场景拆分版明细构建：逻辑与 {@link #buildPlatformSoReturnInstockDetail} 一致（按订单明细
	 * platformSkuNo/warehouseSkuNo 匹配），唯一区别是SKU在订单里彻底找不到时不抛错，而是收集进
	 * unmatchedList，交由调用方单独生成预入库单。已匹配行按平台推送的 mustQty/receiveQty/realQty
	 * 原样落库，不按订单自身库存数量截断。注意：不要修改共享的 {@link #buildPlatformSoReturnInstockDetail}，
	 * 该方法还被 Amazon 平台仓入库路径 {@link #createByExistSoB2c} 复用
	 */
	private SkuSplitDetailResult buildPlatformSoReturnInstockDetailSplit(PlatformReturnInstockDTO dto, WarehouseEntity warehouseEntity, SoB2cEntity soB2cEntity,
																		  Map<String, SkuMappingDTO.MappingSkuViewDTO> skuMappingMap) {
		List<PlatformReturnInstockDTO.Detail> details = dto.getProductDetailList();
		List<SoB2cDetailEntity> soDetailEntityList = soB2cFeign.listDetailByMainIds(Collections.singletonList(soB2cEntity.getId()));
		SkuSplitDetailResult result = new SkuSplitDetailResult();
		if (CollectionUtils.isEmpty(soDetailEntityList)) {
			log.warn("[海外仓退货入库] 订单{}明细为空，本次明细全部改为生成预入库单", dto.getPlatformOrderNo());
			result.unmatchedList.addAll(details);
			return result;
		}
		for (PlatformReturnInstockDTO.Detail detail : details) {
			// 优先按 listing/SKU映射转换后的 ERP skuId 匹配订单明细；映射不到或订单里没有该 skuId 时，
			// 再退回按平台原始 productSku 对 platformSkuNo（平台/销售渠道SKU）/warehouseSkuNo（仓库SKU）
			// 做字符串兜底匹配——部分海外仓来源平台回传的 productSku 实际是仓库侧SKU而非平台SKU
			String platformSkuNo = detail.getProductSku();
			SkuMappingDTO.MappingSkuViewDTO skuMapping = StringUtils.isBlank(platformSkuNo) ? null : skuMappingMap.get(platformSkuNo.toUpperCase());
			SoB2cDetailEntity detailEntity = null;
			if (Objects.nonNull(skuMapping)) {
				detailEntity = soDetailEntityList.stream()
						.filter(e -> skuMapping.getProductSkuId().equals(e.getSkuId()))
						.findFirst()
						.orElse(null);
			}
			if (Objects.isNull(detailEntity)) {
				detailEntity = soDetailEntityList.stream()
						.filter(e -> StringUtils.isNotBlank(e.getPlatformSkuNo()) && e.getPlatformSkuNo().equalsIgnoreCase(platformSkuNo))
						.findFirst()
						.orElseGet(() -> soDetailEntityList.stream()
								.filter(e -> StringUtils.isNotBlank(e.getWarehouseSkuNo()) && e.getWarehouseSkuNo().equalsIgnoreCase(platformSkuNo))
								.findFirst()
								.orElse(null));
			}
			if (Objects.isNull(detailEntity)) {
				// 订单里彻底找不到这个SKU：不再整包报错，收集进unmatchedList单独生成预入库单
				log.warn("[海外仓退货入库] 参考单号匹配到订单{}但订单明细中找不到SKU={}，该行改为生成预入库单", dto.getPlatformOrderNo(), detail.getProductSku());
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
			soReturnInstockDetailEntity.setDefectiveProductFlag(detail.getDefectiveProductFlag());
			result.matchedList.add(soReturnInstockDetailEntity);
		}
		return result;
	}

	/**
	 * B2B销售订单明细按平台SKU直接映射入库SKU（B2B无出库单就近匹配逻辑，字段映射与B2C保持一致）；
	 * SKU在订单里找不到时收集进 unmatchedList，不抛错
	 */
	private SkuSplitDetailResult buildPlatformSoReturnInstockDetailForSoInfoSplit(PlatformReturnInstockDTO dto, WarehouseEntity warehouseEntity, SoInfoEntity soInfoEntity,
																				   Map<String, SkuMappingDTO.MappingSkuViewDTO> skuMappingMap) {
		List<PlatformReturnInstockDTO.Detail> details = dto.getProductDetailList();
		List<SoDetailEntity> soDetailEntityList = soInfoFeign.listSoDetailByMainIds(Collections.singletonList(soInfoEntity.getId()));
		SkuSplitDetailResult result = new SkuSplitDetailResult();
		if (CollectionUtils.isEmpty(soDetailEntityList)) {
			log.warn("[海外仓退货入库] B2B订单{}明细为空，本次明细全部改为生成预入库单", dto.getPlatformOrderNo());
			result.unmatchedList.addAll(details);
			return result;
		}
		for (PlatformReturnInstockDTO.Detail detail : details) {
			// 优先按 ERP skuId 匹配，映射不到或订单里没有该 skuId 时再退回按平台原始 productSku
			// 对 platformSkuNo 做字符串兜底匹配，理由同B2C场景
			String platformSkuNo = detail.getProductSku();
			SkuMappingDTO.MappingSkuViewDTO skuMapping = StringUtils.isBlank(platformSkuNo) ? null : skuMappingMap.get(platformSkuNo.toUpperCase());
			SoDetailEntity detailEntity = null;
			if (Objects.nonNull(skuMapping)) {
				detailEntity = soDetailEntityList.stream()
						.filter(e -> skuMapping.getProductSkuId().equals(e.getSkuId()))
						.findFirst()
						.orElse(null);
			}
			if (Objects.isNull(detailEntity)) {
				detailEntity = soDetailEntityList.stream()
						.filter(e -> StringUtils.isNotBlank(e.getPlatformSkuNo()) && e.getPlatformSkuNo().equalsIgnoreCase(platformSkuNo))
						.findFirst().orElse(null);
			}
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
	 * 保证已匹配退货入库单和未匹配预入库单要么一起提交，要么一起回滚。
	 * 预入库单DTO组装（含SKU映射、公司信息等纯查询型Feign调用）在事务开启前先算好，
	 * 事务内只做落库，缩短本地事务与分布式锁的持有时间；addByThirdWarehouse/
	 * createFromOverseasWhHeadless 内部仍各自含有 Feign 调用，属已知遗留限制（见方法内日志）
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
		SoReturnPrestockDTO.Add prestockAdd = CollectionUtils.isNotEmpty(unmatchedList)
				? buildPrestockAddDtoForDetails(dto, warehouseEntity, unmatchedList) : null;
		new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
			// matchedList 可能整批为空（参考单号匹配到订单，但订单里一个SKU都对不上）：
			// 此时跳过退货入库单生成，全部明细改走预入库单，不写零明细的空退货入库单
			if (CollectionUtils.isNotEmpty(matchedList)) {
				soReturnInstockService.addByThirdWarehouse(soReturnInstockEntity, matchedList);
			}
			if (Objects.nonNull(prestockAdd)) {
				soReturnPrestockService.createFromOverseasWhHeadless(prestockAdd);
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
		soReturnInstockEntity.setSourceCode(dto.getUniqueId());
		soReturnInstockEntity.setSourceType(SourceTypeEnum.PLATFORM_RETURN_INSTOCK.getCode());
		soReturnInstockEntity.setThirdCode(dto.getPlatformReturnOrderNo());
		soReturnInstockEntity.setPlatformOrderCode(dto.getPlatformOrderNo());
		soReturnInstockEntity.setCreated(dto.getCreateTime());
		soReturnInstockEntity.setType(BillTypeEnum.B2B.getCode());
		soReturnInstockEntity.setReturnLogisticCode(dto.getReturnLogisticCode());
		soReturnInstockEntity.setSoId(soInfoEntity.getId());
		soReturnInstockEntity.setSoCode(soInfoEntity.getCode());
		soReturnInstockEntity.setCurrency(soInfoEntity.getCurrency());
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
		addDTO.setDictReturnType(dto.getReturnType());
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
			Integer receiveQty = detail.getReceiveQty();
			if (Objects.isNull(receiveQty) || receiveQty <= 0) {
				receiveQty = detail.getRealQty();
			}
			if (Objects.isNull(receiveQty) || receiveQty <= 0) {
				receiveQty = detail.getMustQty();
			}
			detailDTO.setReceivedQty(receiveQty);
			// 平台订单号、平台字典值均为【关联】相关字段，不代表本行数据来源渠道，此处无头件尚未关联，不写入
			detailDTO.setRemark(dto.getReason());
			detailDTO.setDefectiveProductFlag(Boolean.TRUE.equals(detail.getDefectiveProductFlag()));
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
	 * 众包(ZHONG_BAO_WAREHOUSE)/极兔(JI_TU_WAREHOUSE)/艾姆勒(IML)/通邮(TONG_YOU_WAREHOUSE)：
	 * 历史遗留专属仓库解析、来源匹配（B2B三方发货单/B2C发货单号）与金额、含税金额、汇率计算逻辑，
	 * 与下方统一匹配流程（{@link #handleReferenceNoAfterSaleMatch}）相互独立，不进入统一匹配
	 */
	private boolean isLegacySpecialWarehousePlatform(String platform) {
		return PlatformDictEnum.ZHONG_BAO_WAREHOUSE.getCode().equalsIgnoreCase(platform)
				|| PlatformDictEnum.JI_TU_WAREHOUSE.getCode().equalsIgnoreCase(platform)
				|| PlatformDictEnum.IML.getCode().equalsIgnoreCase(platform)
				|| PlatformDictEnum.TONG_YOU_WAREHOUSE.getCode().equalsIgnoreCase(platform);
	}

	/**
	 * 众包/极兔/艾姆勒/通邮专属处理：来源单号解析（含WFHD三方仓发货单）、众包极兔的B2B三方发货单/B2C发货单号匹配、
	 * 仓库解析（艾姆勒/通邮取订单仓库，众包/极兔走海外仓服务商映射）、按平台构建对应实体明细并落库
	 */
	private void handleLegacySpecialWarehouse(PlatformReturnInstockDTO dto) {
		SoB2cEntity soB2cEntity = null;
		SoInfoEntity soInfoEntity = null;
		SoOutstockEntity soOutstock = null;
		SoReturnInstockEntity soReturnInstockEntity;
		List<SoReturnInstockDetailEntity> detailEntityList = new ArrayList<>();

		if (CharSequenceUtil.isNotBlank(dto.getOrderReferenceNo()) && dto.getOrderReferenceNo().contains(BusinessNoConstant.WFHD)) {
			// 参考单号为三方仓发货单号：查三方仓发货单取对应销售订单
			ThirdWarehouseDeliveryEntity thirdWarehouseDeliveryEntity = thirdWarehouseDeliveryService.getLatestByCode(dto.getOrderReferenceNo());
			if (Objects.nonNull(thirdWarehouseDeliveryEntity)) {
				soB2cEntity = soB2cFeign.getSoCode(thirdWarehouseDeliveryEntity.getSoCode());
			}
			if (Objects.nonNull(soB2cEntity)) {
				soOutstock = soOutstockService.getBySoId(soB2cEntity.getId());
			}
		}

		if ((Objects.equals(dto.getPlatform(), PlatformDictEnum.ZHONG_BAO_WAREHOUSE.getCode())
				|| Objects.equals(dto.getPlatform(), PlatformDictEnum.JI_TU_WAREHOUSE.getCode())
				&& CharSequenceUtil.isNotBlank(dto.getPlatformOrderNo()))) {
			List<B2bThirdDeliveryEntity> list = b2bThirdDeliveryService.lambdaQuery()
					.eq(B2bThirdDeliveryEntity::getPlatformOrderCode, dto.getPlatformOrderNo())
					.list();
			if (!list.isEmpty()) {
				SoInfoEntity soInfo = soInfoFeign.getSoInfoById(list.get(0).getSoId());
				if (Objects.nonNull(soInfo)) {
					soInfoEntity = soInfo;
				}
			}

			List<SoB2cEntity> soB2CList = soB2cFeign.getByShippingOrderNo(dto.getPlatformOrderNo());
			if (!soB2CList.isEmpty()) {
				soB2cEntity = soB2CList.get(0);
			}
		}

		WarehouseEntity warehouseEntity = new WarehouseEntity();
		// 艾姆勒/通邮没有仓库映射，拿订单的仓库
		if (PlatformDictEnum.IML.getCode().equalsIgnoreCase(dto.getPlatform()) || PlatformDictEnum.TONG_YOU_WAREHOUSE.getCode().equalsIgnoreCase(dto.getPlatform())) {
			if (Objects.nonNull(soB2cEntity)) {
				soOutstock = soOutstockService.getBySoId(soB2cEntity.getId());
				List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cFeign.listDetailByMainIds(Collections.singletonList(soB2cEntity.getId()));
				if (CollectionUtils.isNotEmpty(soB2cDetailEntityList)) {
					String warehouseId = soB2cDetailEntityList.get(0).getWarehouseId();
					if (StringUtils.isNotBlank(warehouseId)) {
						warehouseEntity = warehouseService.getById(warehouseId);
					}
				}
			}
		} else {
			OverseasProviderWarehouseEntity overseasProviderWarehouseEntity = overseasProviderWarehouseService.getByPlatform(dto.getAuthId(), dto.getWarehouseCode());
			if (Objects.isNull(overseasProviderWarehouseEntity) || CharSequenceUtil.isBlank(overseasProviderWarehouseEntity.getWarehouseId())) {
				throw new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC, "仓库信息");
			}
			warehouseEntity = warehouseService.getById(overseasProviderWarehouseEntity.getWarehouseId());
		}

		if (Objects.equals(dto.getPlatform(), PlatformDictEnum.ZHONG_BAO_WAREHOUSE.getCode())) {
			soReturnInstockEntity = this.buildZhongBaoSoReturnInstockEntity(dto, warehouseEntity, soB2cEntity, soInfoEntity, soOutstock);
			detailEntityList.addAll(this.buildZhongBaoSoReturnInstockDetail(dto, soB2cEntity, soInfoEntity, warehouseEntity));
		} else if (Objects.equals(dto.getPlatform(), PlatformDictEnum.JI_TU_WAREHOUSE.getCode())) {
			soReturnInstockEntity = this.buildJiTuSoReturnInstockEntity(dto, warehouseEntity, soB2cEntity, soInfoEntity, soOutstock);
			detailEntityList.addAll(this.buildJiTuSoReturnInstockDetail(dto, soB2cEntity, soInfoEntity, warehouseEntity));
		} else {
			soReturnInstockEntity = this.buildSoReturnInstockEntity(dto, warehouseEntity, soB2cEntity, soOutstock);
			detailEntityList.addAll(this.buildSoReturnInstockDetail(dto, soReturnInstockEntity, warehouseEntity));
		}

		if (CollectionUtils.isEmpty(detailEntityList)) {
			throw new ServiceException("没有映射");
		}

		this.matchSoReturn(soReturnInstockEntity, detailEntityList, dto, soB2cEntity);

		soReturnInstockService.addByThirdWarehouse(soReturnInstockEntity, detailEntityList);
	}

	/**
	 * 众包/极兔/艾姆勒/通邮 旧版默认明细构建（艾姆勒/通邮走此分支）：按平台SKU全局映射（不区分订单来源），
	 * 命中来源单据时补充 sourceDetailId/createUserId/platformSkuNo/defectiveProductFlag
	 */
	private List<SoReturnInstockDetailEntity> buildSoReturnInstockDetail(PlatformReturnInstockDTO dto, SoReturnInstockEntity soReturnInstockEntity, WarehouseEntity warehouseEntity) {
		List<PlatformReturnInstockDTO.Detail> details = dto.getProductDetailList();
		if (CollectionUtils.isEmpty(details)) {
			throw new ServiceException("明细为空");
		}
		List<String> platformSkuNoList = dto.getProductDetailList().stream().map(v -> v.getProductSku()).collect(Collectors.toList());
		ListingInfoParamDTO listingInfoParamDTO = new ListingInfoParamDTO();
		listingInfoParamDTO.setPlatformSkuNoList(platformSkuNoList);
		listingInfoParamDTO.setAuthId(dto.getAuthId());
		listingInfoParamDTO.setMatchResult(ListingMatchResultEnum.TRUE.getCode());
		List<SkuMappingDTO.MappingSkuViewDTO> mappingSkuViewDTOList = skuMappingFeign.listByPlatformSkuNoAndPlatform(listingInfoParamDTO);
		List<SoReturnInstockDetailEntity> detailEntityList = new ArrayList<>();
		for (PlatformReturnInstockDTO.Detail detail : details) {
			SkuMappingDTO.MappingSkuViewDTO skuViewDTO = mappingSkuViewDTOList.stream().filter(v -> v.getPlatformSkuNo().equals(detail.getProductSku())).findFirst().orElse(null);
			if (Objects.isNull(skuViewDTO)) {
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
			soReturnInstockDetailEntity.setDefectiveProductFlag(detail.getDefectiveProductFlag());
			detailEntityList.add(soReturnInstockDetailEntity);
		}
		return detailEntityList;
	}

	/**
	 * 众包退货入库明细：按销售订单/退货订单明细价格计算退货金额与含税退货金额。
	 * <p>
	 * 产品需求（众包海外仓对接 §七）：B2C 仅要求【退货金额】【含税退货金额】（真实售价×签收数量），
	 * 未要求 returnAmount、退货金额（本位币）等字段；与极兔分支字段范围不同，勿按极兔补齐。
	 */
	private List<SoReturnInstockDetailEntity> buildZhongBaoSoReturnInstockDetail(PlatformReturnInstockDTO dto, SoB2cEntity soB2cEntity, SoInfoEntity soInfoEntity, WarehouseEntity warehouseEntity) {
		List<PlatformReturnInstockDTO.Detail> details = dto.getProductDetailList();
		List<SoB2cDetailEntity> soB2cDetails = new ArrayList<>();
		List<SoDetailEntity> soDetails = new ArrayList<>();
		if (CollectionUtils.isEmpty(details)) {
			throw new ServiceException("明细为空");
		}
		List<String> platformSkuNoList = dto.getProductDetailList().stream().map(v -> v.getProductSku()).collect(Collectors.toList());
		ListingInfoParamDTO listingInfoParamDTO = new ListingInfoParamDTO();
		listingInfoParamDTO.setPlatformSkuNoList(platformSkuNoList);
		listingInfoParamDTO.setAuthId(dto.getAuthId());
		listingInfoParamDTO.setMatchResult(ListingMatchResultEnum.TRUE.getCode());
		List<SkuMappingDTO.MappingSkuViewDTO> mappingSkuViewDTOList = skuMappingFeign.listByPlatformSkuNoAndPlatform(listingInfoParamDTO);
		if (CollectionUtils.isEmpty(mappingSkuViewDTOList)) {
			throw new ServiceException("没有找到sku映射");
		}
		Map<String, SkuMappingDTO.MappingSkuViewDTO> skuMappingMap = mappingSkuViewDTOList.stream()
				.filter(v -> StringUtils.isNotBlank(v.getPlatformSkuNo()))
				.collect(Collectors.toMap(SkuMappingDTO.MappingSkuViewDTO::getPlatformSkuNo, v -> v, (existing, duplicate) -> existing));
		List<String> unmappedSkuList = details.stream()
				.map(PlatformReturnInstockDTO.Detail::getProductSku)
				.filter(sku -> StringUtils.isBlank(sku) || !skuMappingMap.containsKey(sku))
				.distinct()
				.collect(Collectors.toList());
		if (CollectionUtils.isNotEmpty(unmappedSkuList)) {
			throw new ServiceException("众包退货入库SKU映射缺失:" + String.join(",", unmappedSkuList));
		}
		List<SoReturnInstockDetailEntity> detailEntityList = new ArrayList<>();
		if (Objects.nonNull(soB2cEntity)) {
			soB2cDetails.addAll(soB2cFeign.listDetailByMainIds(Collections.singletonList(soB2cEntity.getId())));
		} else if (Objects.nonNull(soInfoEntity)) {
			soDetails.addAll(soInfoFeign.listSoDetailByMainId(soInfoEntity.getId()));
		}
		for (PlatformReturnInstockDTO.Detail detail : details) {
			SkuMappingDTO.MappingSkuViewDTO skuViewDTO = skuMappingMap.get(detail.getProductSku());
			SoReturnInstockDetailEntity soReturnInstockDetailEntity = new SoReturnInstockDetailEntity();
			soReturnInstockDetailEntity.setSkuId(skuViewDTO.getProductSkuId());
			soReturnInstockDetailEntity.setSkuNo(skuViewDTO.getProductSkuNo());
			soReturnInstockDetailEntity.setMustQty(0);
			soReturnInstockDetailEntity.setReceiveQty(detail.getReceiveQty());
			soReturnInstockDetailEntity.setRealQty(detail.getRealQty());
			int actualQty = detail.getReceiveQty() != null ? detail.getReceiveQty() : 0;
			if (!soB2cDetails.isEmpty()) {
				SoB2cDetailEntity soB2cDetailEntity = soB2cDetails.stream()
						.filter(item -> Objects.equals(item.getSkuId(), skuViewDTO.getProductSkuId()))
						.findFirst()
						.orElse(null);
				if (Objects.nonNull(soB2cDetailEntity)) {
					// 产品需求：众包 B2C 只落 amount / taxReturnAmount，不填 returnAmount 及本位币字段
					BigDecimal amount = MathUtil.multiplyWithSix(soB2cDetailEntity.getPrice(), BigDecimal.valueOf(actualQty));
					soReturnInstockDetailEntity.setAmount(amount);
					soReturnInstockDetailEntity.setTaxReturnAmount(amount);
				} else {
					soReturnInstockDetailEntity.setAmount(BigDecimal.ZERO);
					soReturnInstockDetailEntity.setTaxReturnAmount(BigDecimal.ZERO);
				}
			} else if (!soDetails.isEmpty()) {
				SoDetailEntity soDetailEntity = soDetails.stream()
						.filter(item -> Objects.equals(item.getSkuId(), skuViewDTO.getProductSkuId()))
						.findFirst()
						.orElse(null);
				if (Objects.nonNull(soDetailEntity)) {
					soReturnInstockDetailEntity.setReturnAmount(MathUtil.multiplyWithSix(soDetailEntity.getPrice(), BigDecimal.valueOf(actualQty)));
					soReturnInstockDetailEntity.setTaxReturnAmount(MathUtil.multiplyWithSix(soDetailEntity.getTaxPrice(), BigDecimal.valueOf(actualQty)));
				} else {
					soReturnInstockDetailEntity.setAmount(BigDecimal.ZERO);
					soReturnInstockDetailEntity.setTaxReturnAmount(BigDecimal.ZERO);
				}
			} else {
				soReturnInstockDetailEntity.setAmount(BigDecimal.ZERO);
				soReturnInstockDetailEntity.setTaxReturnAmount(BigDecimal.ZERO);
			}
			soReturnInstockDetailEntity.setWarehouseId(warehouseEntity.getId());
			soReturnInstockDetailEntity.setWarehouseName(warehouseEntity.getName());
			soReturnInstockDetailEntity.setRemark(dto.getReason());
			soReturnInstockDetailEntity.setReturnTypeDict(dto.getReturnType());
			soReturnInstockDetailEntity.setSourceDetailId(detail.getThirdId());
			soReturnInstockDetailEntity.setCreateUserId(dto.getAuthId());
			soReturnInstockDetailEntity.setPlatformSkuNo(detail.getProductSku());
			soReturnInstockDetailEntity.setDefectiveProductFlag(detail.getDefectiveProductFlag());
			detailEntityList.add(soReturnInstockDetailEntity);
		}
		return detailEntityList;
	}

	/**
	 * 旧版默认退货入库单实体构建（艾姆勒/通邮走此分支）
	 */
	private SoReturnInstockEntity buildSoReturnInstockEntity(PlatformReturnInstockDTO dto, WarehouseEntity warehouseEntity, SoB2cEntity soB2cEntity, SoOutstockEntity soOutstock) {
		SoReturnInstockEntity soReturnInstockEntity = new SoReturnInstockEntity();
		if (StringUtils.isNotBlank(warehouseEntity.getId())) {
			soReturnInstockEntity.setApproveTime(LocalDateTime.now());
			soReturnInstockEntity.setApproveStatus(ApproveStatusEnum.APPROVE_ING.getStatus());
		}
		soReturnInstockEntity.setBillDate(dto.getPutawayLocalDate());
		soReturnInstockEntity.setInventoryOrgId(warehouseEntity.getOrgId());
		soReturnInstockEntity.setReturnLogisticCode(dto.getReturnLogisticCode());
		soReturnInstockEntity.setSourceId(dto.getSourceId());
		if (StringUtils.isNotBlank(warehouseEntity.getId())) {
			SysAccountingCompanyEntity company = sysUserFeign.getCompanyById(warehouseEntity.getOrgId());
			soReturnInstockEntity.setInventoryOrgName(company.getCompanyName());
		}
		soReturnInstockEntity.setWarehouseKeeperId(warehouseEntity.getChargeId());
		soReturnInstockEntity.setApproveUserName("system");
		soReturnInstockEntity.setSourceCode(Objects.nonNull(soB2cEntity) ? soB2cEntity.getCode() : dto.getOrderReferenceNo());
		soReturnInstockEntity.setSourceType(SourceTypeEnum.THIRD_WAREHOUSE_RETURN_INSTOCK.getCode());
		soReturnInstockEntity.setThirdCode(dto.getPlatformReturnOrderNo());
		soReturnInstockEntity.setCreated(dto.getCreateTime());
		soReturnInstockEntity.setType("B2C");
		if (Objects.nonNull(soB2cEntity)) {
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
		if (Objects.nonNull(soOutstock)) {
			if (StringUtils.isNotBlank(soOutstock.getSalesDeptId())) {
				SysDepartmentDTO department = sysUserFeign.getUserDeptById(soOutstock.getSalesDeptId());
				if (null != department) {
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
	 * 众包退货入库单实体构建
	 */
	private SoReturnInstockEntity buildZhongBaoSoReturnInstockEntity(PlatformReturnInstockDTO dto, WarehouseEntity warehouseEntity, SoB2cEntity soB2cEntity, SoInfoEntity soInfoEntity, SoOutstockEntity soOutstock) {
		SoReturnInstockEntity soReturnInstockEntity = new SoReturnInstockEntity();
		CustomerInfoEntity customerInfo = null;
		if (StringUtils.isNotBlank(warehouseEntity.getId())) {
			soReturnInstockEntity.setApproveTime(LocalDateTime.now());
			soReturnInstockEntity.setApproveStatus(ApproveStatusEnum.APPROVE_ING.getStatus());
		}
		soReturnInstockEntity.setBillDate(dto.getPutawayLocalDate());
		soReturnInstockEntity.setInventoryOrgId(warehouseEntity.getOrgId());
		soReturnInstockEntity.setReturnLogisticCode(dto.getReturnLogisticCode());
		soReturnInstockEntity.setSourceId(dto.getSourceId());
		if (StringUtils.isNotBlank(warehouseEntity.getId())) {
			SysAccountingCompanyEntity company = sysUserFeign.getCompanyById(warehouseEntity.getOrgId());
			soReturnInstockEntity.setInventoryOrgName(company.getCompanyName());
		}
		soReturnInstockEntity.setWarehouseKeeperId(warehouseEntity.getChargeId());
		soReturnInstockEntity.setApproveUserName("system");
		soReturnInstockEntity.setSourceType(SourceTypeEnum.THIRD_WAREHOUSE_RETURN_INSTOCK.getCode());
		soReturnInstockEntity.setThirdCode(dto.getPlatformReturnOrderNo());
		soReturnInstockEntity.setCreated(dto.getCreateTime());
		if (Objects.nonNull(soB2cEntity)) {
			soReturnInstockEntity.setSourceCode(soB2cEntity.getCode());
			soReturnInstockEntity.setType("B2C");
			soReturnInstockEntity.setSalesOrgId(soB2cEntity.getOrgId());
			soReturnInstockEntity.setSalesOrgName(soB2cEntity.getOrgName());
			ShopInfoEntity shopInfoEntity = shopInfoFeign.getShopInfoById(soB2cEntity.getShopId());
			customerInfo = customerFeign.getCustomerById(shopInfoEntity.getCustomerId());
			soReturnInstockEntity.setCustomerId(shopInfoEntity.getCustomerId());
			soReturnInstockEntity.setCustomerName(customerInfo.getName());
			soReturnInstockEntity.setSoCode(soB2cEntity.getCode());
			soReturnInstockEntity.setSourceCode(soB2cEntity.getCode());
			soReturnInstockEntity.setSoId(soB2cEntity.getId());
			soReturnInstockEntity.setShopId(soB2cEntity.getShopId());
			soReturnInstockEntity.setCurrency(soB2cEntity.getCurrency());
		} else if (Objects.nonNull(soInfoEntity)) {
			soReturnInstockEntity.setSourceCode(soInfoEntity.getCode());
			soReturnInstockEntity.setType("B2B");
			soReturnInstockEntity.setSalesOrgId(soInfoEntity.getSalesOrgId());
			soReturnInstockEntity.setSalesOrgName(soInfoEntity.getSalesOrgName());
			customerInfo = customerFeign.getCustomerById(soInfoEntity.getCustomerId());
			soReturnInstockEntity.setCustomerId(soInfoEntity.getCustomerId());
			soReturnInstockEntity.setCustomerName(customerInfo.getName());
			soReturnInstockEntity.setSoCode(soInfoEntity.getCode());
			soReturnInstockEntity.setSourceCode(soInfoEntity.getCode());
			soReturnInstockEntity.setSoId(soInfoEntity.getId());
			soReturnInstockEntity.setCurrency(soInfoEntity.getCurrency());
		} else {
			soReturnInstockEntity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
			soReturnInstockEntity.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
			soReturnInstockEntity.setCurrencySymbol("¥");
		}
		if (Objects.nonNull(soOutstock)) {
			if (StringUtils.isNotBlank(soOutstock.getSalesDeptId())) {
				SysDepartmentDTO department = sysUserFeign.getUserDeptById(soOutstock.getSalesDeptId());
				if (null != department) {
					soReturnInstockEntity.setSalesDeptId(soOutstock.getSalesDeptId());
					soReturnInstockEntity.setSalesDeptName(department.getName());
				}
			}
		}
		if (Objects.nonNull(customerInfo)) {
			soReturnInstockEntity.setSellerId(customerInfo.getSellerId());
			soReturnInstockEntity.setSellerName(customerInfo.getSellerName());
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
		List<ShopInfoEntity> shopList = shopInfoFeign.listByParams(new ShopInfoDTO.ListParamDTO(AuthStatusEnum.ALREADY.getCode(),dto.getPlatform(), null));
		List<String> shopIds ;
		//京东取的是店铺ID
		if (CharSequenceUtil.equals(dto.getPlatform(),PlatformDictEnum.NASDAQ_JD.getCode()) ) {
			shopIds = shopList.stream()
					.map(BaseEntity::getId)
					.filter(id -> id.equals(platformShopCode))
					.collect(Collectors.toList());
		} else {
			shopIds = shopList.stream()
					.filter(e -> e.getPlatformShopCode().equalsIgnoreCase(platformShopCode))
					.map(BaseEntity::getId)
					.collect(Collectors.toList());
		}

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
			// 优先匹配 platformSkuNo（平台/销售渠道SKU），未命中时按 warehouseSkuNo（仓库SKU）兜底——
			// 部分海外仓来源平台回传的 productSku 实际是仓库侧SKU而非平台SKU
			SoB2cDetailEntity detailEntity = soDetailEntityList.stream()
					.filter(e -> StringUtils.isNotBlank(e.getPlatformSkuNo()) && e.getPlatformSkuNo().equalsIgnoreCase(detail.getProductSku()))
					.findFirst()
					.orElseGet(() -> soDetailEntityList.stream()
							.filter(e -> StringUtils.isNotBlank(e.getWarehouseSkuNo()) && e.getWarehouseSkuNo().equalsIgnoreCase(detail.getProductSku()))
							.findFirst()
							.orElse(null));
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
			soReturnInstockDetailEntity.setDefectiveProductFlag(detail.getDefectiveProductFlag());
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
		soReturnInstockEntity.setBillDate(dto.getPutawayLocalDate());
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
		soReturnInstockEntity.setReturnLogisticCode(dto.getReturnLogisticCode());
		if(Objects.nonNull(soB2cEntity)) {
			soReturnInstockEntity.setSoId(soB2cEntity.getId());
			soReturnInstockEntity.setSoCode(soB2cEntity.getCode());
			soReturnInstockEntity.setCurrency(soB2cEntity.getCurrency());
		} else {
			soReturnInstockEntity.setCurrency(CharSequenceUtil.isBlank(shopInfoEntity.getSettlementCurrency()) ? CurrencyEnum.CNY.getCurrencyCode() : shopInfoEntity.getSettlementCurrency());
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
		LocalDate closedLocalDate = inventoryClosedRecordService.checkClosed(warehouseEntity.getOrgId(), dto.getPutawayLocalDate());
		if (null != closedLocalDate) {
			// 已关账
			log.warn("[退货入库单消费]:当前退货入库单消费日期【{}】因关账【{}】停止生成：单号={}", dto.getPutawayLocalDate(), closedLocalDate, dto.getPlatformOrderNo());
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
		paramDTO.setPlatformSkuNoList(platformSkuList);
		paramDTO.setMatchResult(ListingMatchResultEnum.TRUE.getCode());
		paramDTO.setIsExpire(false);
		//传参调整
		if  (CharSequenceUtil.equals(dto.getPlatform(), PlatformDictEnum.NASDAQ_JD.getCode())) {
			paramDTO.setType(RuleTypeEnum.WAREHOUSE.getCode());
			paramDTO.setWarehouseIdList(Collections.singletonList(warehouseEntity.getId()));
		} else {
			paramDTO.setPlatform(dto.getPlatform());
			paramDTO.setShopIdList(shopIds);
			paramDTO.setType(RuleTypeEnum.B2C_PLATFORM.getCode());
		}
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
			soReturnInstockDetailEntity.setPlatformSkuNo(detail.getProductSku());
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

	/**
	 * 根据产品条码查询sku
	 * @author will
	 * @date 2025/12/4 11:50
	 * @param dto
	 * @return void
	 */
	private void handleThirdBarcode (PlatformReturnInstockDTO dto) {
		List<String> thirdBarcodeList = dto.getProductDetailList().stream().map(PlatformReturnInstockDTO.Detail::getThirdBarcode).distinct().collect(Collectors.toList());
		if (CollUtil.isEmpty(thirdBarcodeList) || !CharSequenceUtil.equals(dto.getPlatform(), PlatformDictEnum.TONG_YOU_WAREHOUSE.getCode())) {
			return;
		}
		List<ListingInfoEntity> list = FeignQuery.create(ListingInfoEntity.class)
				.eq(ListingInfoEntity::getPlatform, dto.getPlatform())
				.eq(ListingInfoEntity::getAuthId, dto.getAuthId())
				.in(ListingInfoEntity::getThirdBarcode, thirdBarcodeList)
				.list();
		if (CollUtil.isEmpty(list)) {
			return;
		}
		for (PlatformReturnInstockDTO.Detail item : dto.getProductDetailList()) {
			list.stream().filter(obj -> CharSequenceUtil.equals(dto.getAuthId(),obj.getAuthId()) && CharSequenceUtil.equals(dto.getPlatform(),obj.getPlatform()) && CharSequenceUtil.equals(item.getThirdBarcode(),obj.getThirdBarcode()))
					.findFirst()
					.ifPresent(obj -> item.setProductSku(obj.getPlatformSkuNo()));
		}
	}

	/**
	 * 构建极兔退货入库单实体
	 */
	private SoReturnInstockEntity buildJiTuSoReturnInstockEntity(PlatformReturnInstockDTO dto,WarehouseEntity warehouseEntity,SoB2cEntity soB2cEntity,SoInfoEntity soInfoEntity,SoOutstockEntity soOutstock){
		SoReturnInstockEntity soReturnInstockEntity = new SoReturnInstockEntity();
		CustomerInfoEntity customerInfo = null;
		
		// 默认自动提交并审核通过
		if(StringUtils.isNotBlank(warehouseEntity.getId())){
			soReturnInstockEntity.setApproveTime(LocalDateTime.now());
			soReturnInstockEntity.setApproveStatus(ApproveStatusEnum.APPROVE_ING.getStatus());
		}
		// 入库日期取关单时间，为空兜底当前日期
		soReturnInstockEntity.setBillDate(dto.getPutawayLocalDate());
		// 库存组织
		soReturnInstockEntity.setInventoryOrgId(warehouseEntity.getOrgId());
		// 组织信息
		if(StringUtils.isNotBlank(warehouseEntity.getId())){
			SysAccountingCompanyEntity company = sysUserFeign.getCompanyById(warehouseEntity.getOrgId());
			if (Objects.isNull(company)) {
				ServiceException.runError("库存组织对应公司信息不存在:orgId={}", warehouseEntity.getOrgId());
			}
			soReturnInstockEntity.setInventoryOrgName(company.getCompanyName());
		}
		// 退货物流单号
		soReturnInstockEntity.setReturnLogisticCode(dto.getReturnLogisticCode());
		// 第三方单据编号
		soReturnInstockEntity.setThirdCode(dto.getPlatformReturnOrderNo());
		// 来源类型
		soReturnInstockEntity.setSourceType(SourceTypeEnum.THIRD_WAREHOUSE_RETURN_INSTOCK.getCode());
		// 创建时间
		soReturnInstockEntity.setCreated(dto.getCreateTime());
		// 仓库信息
		soReturnInstockEntity.setWarehouseKeeperId(warehouseEntity.getChargeId());
		if (Objects.nonNull(soB2cEntity)) {
			// B2C订单
			soReturnInstockEntity.setType("B2C");
			soReturnInstockEntity.setSourceId(soB2cEntity.getId());
			soReturnInstockEntity.setSourceCode(soB2cEntity.getCode());
			soReturnInstockEntity.setSalesOrgId(soB2cEntity.getOrgId());
			soReturnInstockEntity.setSalesOrgName(soB2cEntity.getOrgName());
			// 退货客户
			ShopInfoEntity shopInfoEntity = shopInfoFeign.getShopInfoById(soB2cEntity.getShopId());
			if (Objects.isNull(shopInfoEntity)) {
				ServiceException.runError("B2C销售订单对应店铺不存在:shopId={}", soB2cEntity.getShopId());
			}
			if (StringUtils.isBlank(shopInfoEntity.getCustomerId())) {
				ServiceException.runError("店铺对应客户信息为空:{}", shopInfoEntity.getName());
			}
			customerInfo = customerFeign.getCustomerById(shopInfoEntity.getCustomerId());
			if (Objects.isNull(customerInfo)) {
				ServiceException.runError("店铺对应客户信息不存在:客户ID={}", shopInfoEntity.getCustomerId());
			}
			soReturnInstockEntity.setCustomerId(shopInfoEntity.getCustomerId());
			soReturnInstockEntity.setCustomerName(customerInfo.getName());
			// 销售订单信息
			soReturnInstockEntity.setSoCode(soB2cEntity.getCode());
			soReturnInstockEntity.setSoId(soB2cEntity.getId());
			soReturnInstockEntity.setShopId(soB2cEntity.getShopId());
			// 平台订单编号：销售订单平台单号为空时用平台消息单号兜底
			soReturnInstockEntity.setPlatformOrderCode(CharSequenceUtil.isNotBlank(soB2cEntity.getPlatformCode()) ? soB2cEntity.getPlatformCode() : dto.getPlatformOrderNo());
			// 币种
			soReturnInstockEntity.setCurrency(soB2cEntity.getCurrency());
		} else if (Objects.nonNull(soInfoEntity)){
			// B2B订单
			soReturnInstockEntity.setType("B2B");
			//未匹配到时匹配B2B三方发货单的三方仓订单号，匹配到后将发货单的销售单号作为退货入库单的来源订单号
			if (StringUtils.isNotBlank(dto.getOrderReferenceNo())) {
				B2bThirdDeliveryEntity b2bThirdDelivery = b2bThirdDeliveryService.lambdaQuery()
						.eq(B2bThirdDeliveryEntity::getPlatformOrderCode, dto.getOrderReferenceNo())
						.one();
				if (Objects.nonNull(b2bThirdDelivery)) {
					soReturnInstockEntity.setSourceId(b2bThirdDelivery.getId());
					soReturnInstockEntity.setSourceCode(b2bThirdDelivery.getSoCode());
				}
			}
			if (CharSequenceUtil.isBlank(soReturnInstockEntity.getSourceCode())) {
				soReturnInstockEntity.setSourceId(soInfoEntity.getId());
				soReturnInstockEntity.setSourceCode(soInfoEntity.getCode());
			}
			soReturnInstockEntity.setSalesOrgId(soInfoEntity.getSalesOrgId());
			soReturnInstockEntity.setSalesOrgName(soInfoEntity.getSalesOrgName());
			// 退货客户
			if (StringUtils.isBlank(soInfoEntity.getCustomerId())) {
				ServiceException.runError("B2B销售订单对应客户信息为空:{}", soInfoEntity.getCode());
			}
			customerInfo = customerFeign.getCustomerById(soInfoEntity.getCustomerId());
			if (Objects.isNull(customerInfo)) {
				ServiceException.runError("B2B销售订单对应客户信息不存在:客户ID={}", soInfoEntity.getCustomerId());
			}
			soReturnInstockEntity.setCustomerId(soInfoEntity.getCustomerId());
			soReturnInstockEntity.setCustomerName(customerInfo.getName());
			// 销售订单信息
			soReturnInstockEntity.setSoCode(soInfoEntity.getCode());
			soReturnInstockEntity.setSoId(soInfoEntity.getId());
			// 平台订单编号，与B2C分支保持一致：销售订单平台单号为空时用平台消息单号兜底
			soReturnInstockEntity.setPlatformOrderCode(CharSequenceUtil.isNotBlank(soInfoEntity.getPlatformOrderCode()) ? soInfoEntity.getPlatformOrderCode() : dto.getPlatformOrderNo());
			// 币种
			soReturnInstockEntity.setCurrency(soInfoEntity.getCurrency());
			soReturnInstockEntity.setCurrencySymbol(soInfoEntity.getCurrencySymbol());
		} else {
			// 未关联到订单
			soReturnInstockEntity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
			soReturnInstockEntity.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
			soReturnInstockEntity.setCurrencySymbol("¥");
		}
		
		// 销售部门和销售员
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

		if (Objects.nonNull(customerInfo)) {
			soReturnInstockEntity.setSellerId(customerInfo.getSellerId());
			soReturnInstockEntity.setSellerName(customerInfo.getSellerName());
		}
		
		return soReturnInstockEntity;
	}
	
	/**
	 * 构建极兔退货入库单明细
	 */
	private List<SoReturnInstockDetailEntity> buildJiTuSoReturnInstockDetail(PlatformReturnInstockDTO dto, SoB2cEntity soB2cEntity, SoInfoEntity soInfoEntity, WarehouseEntity warehouseEntity) {
		List<PlatformReturnInstockDTO.Detail> details = dto.getProductDetailList();
		List<SoB2cDetailEntity> soB2cDetails = new ArrayList<>();
		List<SoDetailEntity> soDetails = new ArrayList<>();
		List<SoB2cReturnDetailEntity> soB2cReturnDetails = new ArrayList<>();
		List<SoReturnDetailEntity> soReturnDetails = new ArrayList<>();
		
		if(CollectionUtils.isEmpty(details)){
			throw new ServiceException("明细为空");
		}
		
		// 获取SKU映射
		List<String> platformSkuNoList = dto.getProductDetailList().stream().map(v->v.getProductSku()).collect(Collectors.toList());
		ListingInfoParamDTO listingInfoParamDTO = new ListingInfoParamDTO();
		listingInfoParamDTO.setPlatformSkuNoList(platformSkuNoList);
		listingInfoParamDTO.setAuthId(dto.getAuthId());
		listingInfoParamDTO.setMatchResult(ListingMatchResultEnum.TRUE.getCode());
		List<SkuMappingDTO.MappingSkuViewDTO> mappingSkuViewDTOList = skuMappingFeign.listByPlatformSkuNoAndPlatform(listingInfoParamDTO);

		if (CollectionUtils.isEmpty(mappingSkuViewDTOList)) {
			throw new ServiceException("没有找到sku映射");
		}
		Map<String, SkuMappingDTO.MappingSkuViewDTO> skuMappingMap = mappingSkuViewDTOList.stream()
				.filter(v -> StringUtils.isNotBlank(v.getPlatformSkuNo()))
				.collect(Collectors.toMap(SkuMappingDTO.MappingSkuViewDTO::getPlatformSkuNo, v -> v, (existing, duplicate) -> existing));
		List<String> unmappedSkuList = details.stream()
				.map(PlatformReturnInstockDTO.Detail::getProductSku)
				.filter(sku -> StringUtils.isBlank(sku) || !skuMappingMap.containsKey(sku))
				.distinct()
				.collect(Collectors.toList());
		if (CollectionUtils.isNotEmpty(unmappedSkuList)) {
			throw new ServiceException("极兔退货入库SKU映射缺失:" + String.join(",", unmappedSkuList));
		}

		// 获取订单明细和关联数据
		BigDecimal rate = new BigDecimal("1");
		if (Objects.nonNull(soB2cEntity)) {
			try {
				rate = dmpTaskFeign.getRate(soB2cEntity.getBillDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), soB2cEntity.getCurrency());
			}catch (Exception e){
				throw new ServiceException(soB2cEntity.getCurrency() + "获取汇率失败");
			}
			if (Objects.isNull(rate)) {
				throw new ServiceException(soB2cEntity.getCurrency() + "获取汇率失败");
			}
			// B2C订单
			soB2cDetails.addAll(soB2cFeign.listDetailByMainIds(Collections.singletonList(soB2cEntity.getId())));
			
			// 获取退货订单明细
			List<SoB2cReturnEntity> soB2cReturnEntities = FeignQuery.create(SoB2cReturnEntity.class)
					.eq(SoB2cReturnEntity::getSoId, soB2cEntity.getId())
					.list();
			if (CollectionUtils.isNotEmpty(soB2cReturnEntities)) {
				List<String> returnIds = soB2cReturnEntities.stream().map(SoB2cReturnEntity::getId).collect(Collectors.toList());
				soB2cReturnDetails.addAll(FeignQuery.create(SoB2cReturnDetailEntity.class)
						.in(SoB2cReturnDetailEntity::getMainId, returnIds)
						.list());
			}
		} else if (Objects.nonNull(soInfoEntity)){
			try {
				rate = dmpTaskFeign.getRate(soInfoEntity.getBillDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), soInfoEntity.getCurrency());
			}catch (Exception e){
				throw new ServiceException(soInfoEntity.getCurrency() + "获取汇率失败");
			}
			if (Objects.isNull(rate)) {
				throw new ServiceException(soInfoEntity.getCurrency() + "获取汇率失败");
			}
			// B2B订单
			soDetails.addAll(soInfoFeign.listSoDetailByMainId(soInfoEntity.getId()));
			
			// 获取退货订单明细
			List<SoReturnEntity> soReturnEntities = FeignQuery.create(SoReturnEntity.class)
					.eq(SoReturnEntity::getSourceId, soInfoEntity.getId())
					.list();
			if (CollectionUtils.isNotEmpty(soReturnEntities)) {
				List<String> returnIds = soReturnEntities.stream().map(SoReturnEntity::getId).collect(Collectors.toList());
				soReturnDetails.addAll(FeignQuery.create(SoReturnDetailEntity.class)
						.in(SoReturnDetailEntity::getMainId, returnIds)
						.list());
			}
		}
		
		List<SoReturnInstockDetailEntity> detailEntityList = new ArrayList<>();
		
		for (PlatformReturnInstockDTO.Detail detail : details) {
			SkuMappingDTO.MappingSkuViewDTO skuViewDTO = skuMappingMap.get(detail.getProductSku());
			
			SoReturnInstockDetailEntity soReturnInstockDetailEntity = new SoReturnInstockDetailEntity();
			soReturnInstockDetailEntity.setSkuId(skuViewDTO.getProductSkuId());
			soReturnInstockDetailEntity.setSkuNo(skuViewDTO.getProductSkuNo());
			// 仓库信息
			soReturnInstockDetailEntity.setWarehouseId(warehouseEntity.getId());
			soReturnInstockDetailEntity.setWarehouseName(warehouseEntity.getName());
			// 备注和退货类型
			soReturnInstockDetailEntity.setRemark(dto.getReason());
			soReturnInstockDetailEntity.setReturnTypeDict(dto.getReturnType());
			// 来源明细ID
			soReturnInstockDetailEntity.setSourceDetailId(detail.getThirdId());
			soReturnInstockDetailEntity.setCreateUserId(dto.getAuthId());
			soReturnInstockDetailEntity.setPlatformSkuNo(detail.getProductSku());
			
			// 计算数量
			int shouldReturnQty = 0; // 应退数量
			int actualQty = detail.getReceiveQty() != null ? detail.getReceiveQty() : 0;

			if (!soB2cDetails.isEmpty()) {
				// B2C订单
				SoB2cDetailEntity soB2cDetailEntity = soB2cDetails.stream()
						.filter(item -> Objects.equals(item.getSkuId(), skuViewDTO.getProductSkuId()))
						.findFirst()
						.orElse(null);
				if (Objects.nonNull(soB2cDetailEntity)) {
					
					// 应退数量
					shouldReturnQty = soB2cReturnDetails.stream()
							.filter(item -> Objects.equals(item.getSkuId(), skuViewDTO.getProductSkuId()))
							.mapToInt(item -> item.getReturnQty() != null ? item.getReturnQty() : 0)
							.sum();
					
					// 计算退货金额和含税退货金额
					// 取销售订单的订单金额*订单明细的真实售价占比*（上架数量/销售数量）
					BigDecimal amount = MathUtil.multiplyWithSix(soB2cDetailEntity.getPrice(), BigDecimal.valueOf(actualQty));
					soReturnInstockDetailEntity.setAmount(amount);
					soReturnInstockDetailEntity.setReturnAmount(amount);
					soReturnInstockDetailEntity.setTaxReturnAmount(amount);
					soReturnInstockDetailEntity.setReturnAmountLocalCurrency(MathUtil.multiplyWithSix(amount, rate, BigDecimal.ROUND_DOWN));
					soReturnInstockDetailEntity.setTaxReturnAmountLocalCurrency(MathUtil.multiplyWithSix(amount, rate, BigDecimal.ROUND_DOWN));
				} else {
					soReturnInstockDetailEntity.setAmount(BigDecimal.ZERO);
					soReturnInstockDetailEntity.setReturnAmount(BigDecimal.ZERO);
					soReturnInstockDetailEntity.setTaxReturnAmount(BigDecimal.ZERO);
					soReturnInstockDetailEntity.setReturnAmountLocalCurrency(BigDecimal.ZERO);
					soReturnInstockDetailEntity.setTaxReturnAmountLocalCurrency(BigDecimal.ZERO);
				}
			} else if (!soDetails.isEmpty()){
				// B2B订单
				SoDetailEntity soDetailEntity = soDetails.stream()
						.filter(item -> Objects.equals(item.getSkuId(), skuViewDTO.getProductSkuId()))
						.findFirst()
						.orElse(null);
				if (Objects.nonNull(soDetailEntity)) {
					
					// 应退数量
					shouldReturnQty = soReturnDetails.stream()
							.filter(item -> Objects.equals(item.getSkuId(), skuViewDTO.getProductSkuId()))
							.mapToInt(item -> item.getReturnQty() != null ? item.getReturnQty() : 0)
							.sum();
					
					BigDecimal returnAmount = MathUtil.multiplyWithSix(soDetailEntity.getPrice(), BigDecimal.valueOf(actualQty));
					soReturnInstockDetailEntity.setReturnAmount(returnAmount);
					// 取销售订单的含税单价*退货数量
					BigDecimal amount = MathUtil.multiplyWithSix(soDetailEntity.getTaxPrice(), BigDecimal.valueOf(actualQty));
					soReturnInstockDetailEntity.setTaxReturnAmount(amount);
					soReturnInstockDetailEntity.setReturnAmountLocalCurrency(MathUtil.multiplyWithSix(returnAmount, rate, BigDecimal.ROUND_DOWN));
					soReturnInstockDetailEntity.setTaxReturnAmountLocalCurrency(MathUtil.multiplyWithSix(amount, rate, BigDecimal.ROUND_DOWN));
				} else {
					soReturnInstockDetailEntity.setAmount(BigDecimal.ZERO);
					soReturnInstockDetailEntity.setReturnAmount(BigDecimal.ZERO);
					soReturnInstockDetailEntity.setTaxReturnAmount(BigDecimal.ZERO);
					soReturnInstockDetailEntity.setReturnAmountLocalCurrency(BigDecimal.ZERO);
					soReturnInstockDetailEntity.setTaxReturnAmountLocalCurrency(BigDecimal.ZERO);
				}
			} else {
				// 未匹配到订单
				soReturnInstockDetailEntity.setAmount(BigDecimal.ZERO);
				soReturnInstockDetailEntity.setReturnAmount(BigDecimal.ZERO);
				soReturnInstockDetailEntity.setTaxReturnAmount(BigDecimal.ZERO);
				soReturnInstockDetailEntity.setReturnAmountLocalCurrency(BigDecimal.ZERO);
				soReturnInstockDetailEntity.setTaxReturnAmountLocalCurrency(BigDecimal.ZERO);
			}
			
			soReturnInstockDetailEntity.setMustQty(shouldReturnQty);
			soReturnInstockDetailEntity.setReceiveQty(actualQty);
			soReturnInstockDetailEntity.setRealQty(actualQty);
			soReturnInstockDetailEntity.setDefectiveProductFlag(detail.getDefectiveProductFlag());
			detailEntityList.add(soReturnInstockDetailEntity);
		}
		
		return detailEntityList;
	}

	/**
	 * WEGO 退货入库：按参考单号从 so_b2c_return 中查找匹配记录。
	 * <p>
	 * 单次 Feign 调用，DB 层 SQL 以 OR 条件匹配 code / platform_return_no / platform_order_no / so_code，
	 * 并按以上优先级排序取首条，避免多次远程调用。
	 */
	private SoB2cReturnEntity findSoB2cReturnByRef(String referenceNo) {
		if (CharSequenceUtil.isBlank(referenceNo)) {
			return null;
		}
		SoB2cReturnEntity result = soB2cReturnFeign.findFirstByReferenceNo(referenceNo);
		if (Objects.nonNull(result)) {
			log.info("[WEGO退货入库] 参考单号 {} 命中 so_b2c_return[{}]", referenceNo, result.getId());
		} else {
			log.info("[WEGO退货入库] 参考单号 {} 在 so_b2c_return 中未查到匹配记录", referenceNo);
		}
		return result;
	}
}
