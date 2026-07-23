package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.dto.PlatformReturnInstockDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.dto.SoB2cReturnDetailDTO;
import com.erp.model.oms.entity.SoB2cReturnEntity;
import com.erp.model.wms.entity.SoReturnInstockDetailEntity;
import com.erp.model.wms.entity.SoReturnInstockEntity;
import com.erp.model.wms.entity.SoReturnPrestockDetailEntity;
import com.erp.model.wms.entity.SoReturnPrestockEntity;
import com.erp.rpc.oms.feign.SoB2cReturnFeign;
import com.erp.server.wms.service.SoReturnInstockDetailService;
import com.erp.server.wms.service.SoReturnInstockService;
import com.erp.server.wms.service.SoReturnPrestockDetailService;
import com.erp.server.wms.service.SoReturnPrestockService;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 海外仓退货入库消息缺口对账：按 thirdCode 汇总已落库退货入库单 + 预入库单数量，
 * 从平台推送明细中扣减，得到尚未处理的缺口明细。
 * <p>
 * Platform / RestCloud 两套 Consumer 共用，避免对账口径漂移。
 * </p>
 */
@Slf4j
@Component
public class ReturnInstockGapReconciler {

	@Resource
	private SoReturnInstockService soReturnInstockService;

	@Resource
	private SoReturnInstockDetailService soReturnInstockDetailService;

	@Resource
	private SoReturnPrestockService soReturnPrestockService;

	@Resource
	private SoReturnPrestockDetailService soReturnPrestockDetailService;

	@Resource
	private SoB2cReturnFeign soB2cReturnFeign;

	/**
	 * B2C 退货单明细尚未入库缺口（应退数量 - 已审核入库实退）。
	 */
	public static class B2cReturnGap {
		private String detailId;
		private String skuId;
		private int gapQty;
		private String returnTypeDict;
		private String returnReasonDict;

		public String getDetailId() {
			return detailId;
		}

		public String getSkuId() {
			return skuId;
		}

		public int getGapQty() {
			return gapQty;
		}

		public String getReturnTypeDict() {
			return returnTypeDict;
		}

		public String getReturnReasonDict() {
			return returnReasonDict;
		}
	}

	/**
	 * 销售订单已匹配明细按退货单缺口分配后的结果。
	 */
	public static class ReturnGapAllocationResult {
		/** 关联退货单明细的入库行（含 soReturnDetailId） */
		private final List<SoReturnInstockDetailEntity> returnLinkedList = new ArrayList<>();
		/** 超出缺口或退货单不存在的 SKU，继续销售订单匹配/预入库 */
		private final List<PlatformReturnInstockDTO.Detail> remainingPushDetails = new ArrayList<>();
		/** 分配后该退货单是否已无未入库缺口（可用于完结售后） */
		private boolean allGapsClosed;

		public List<SoReturnInstockDetailEntity> getReturnLinkedList() {
			return returnLinkedList;
		}

		public List<PlatformReturnInstockDTO.Detail> getRemainingPushDetails() {
			return remainingPushDetails;
		}

		public boolean isAllGapsClosed() {
			return allGapsClosed;
		}
	}

	/**
	 * 按 thirdCode 汇总已落库的退货入库单 + 预入库单数量，从平台推送明细中扣减，得到尚未处理的缺口明细。
	 * <p>
	 * 退货入库明细按「平台SKU + 不良品标志」对齐；预入库明细经 SKU 映射（productSku → skuId/skuNo）
	 * 或未映射时的原始 platformSku=skuNo 对齐，键同样带不良品标志。映射对不齐的预入库行
	 * 不会误扣推送量（宁可不扣减也不误吞），并打 warn。
	 * </p>
	 * <p>
	 * SKU 映射仅在存在预入库已处理量时通过 {@code skuMappingResolver} 拉取，避免仅入库消耗场景多打一次 Feign。
	 * </p>
	 *
	 * @param dto                平台退货入库消息
	 * @param skuMappingResolver 平台SKU→ERP SKU 映射解析（由调用方提供，便于复用 Consumer 内封装）
	 * @return 缺口明细（数量已扣减）；全部已覆盖时返回空列表
	 */
	public List<PlatformReturnInstockDTO.Detail> resolveUnprocessedDetails(
			PlatformReturnInstockDTO dto,
			Function<PlatformReturnInstockDTO, Map<String, SkuMappingDTO.MappingSkuViewDTO>> skuMappingResolver) {
		List<PlatformReturnInstockDTO.Detail> pushDetails = dto.getProductDetailList();
		if (CollectionUtils.isEmpty(pushDetails)) {
			return Collections.emptyList();
		}
		String thirdCode = dto.getPlatformReturnOrderNo();
		if (CharSequenceUtil.isBlank(thirdCode)) {
			return copyDetails(pushDetails);
		}

		Map<String, Integer> consumedByPlatformSku = new HashMap<>();
		accumulateInstockConsumedByPlatformSku(thirdCode, consumedByPlatformSku);
		Map<String, Integer> prestockBySkuId = new HashMap<>();
		Map<String, Integer> prestockBySkuNo = new HashMap<>();
		accumulatePrestockConsumed(thirdCode, prestockBySkuId, prestockBySkuNo);

		boolean hasAnyConsumed = !consumedByPlatformSku.isEmpty() || !prestockBySkuId.isEmpty() || !prestockBySkuNo.isEmpty();
		if (!hasAnyConsumed) {
			return copyDetails(pushDetails);
		}

		// 仅预入库扣减需要平台SKU→ERP SKU 映射；纯入库消耗场景跳过 Feign
		boolean needSkuMapping = !prestockBySkuId.isEmpty() || !prestockBySkuNo.isEmpty();
		Map<String, SkuMappingDTO.MappingSkuViewDTO> skuMappingMap = needSkuMapping && Objects.nonNull(skuMappingResolver)
				? skuMappingResolver.apply(dto)
				: Collections.emptyMap();
		if (Objects.isNull(skuMappingMap)) {
			skuMappingMap = Collections.emptyMap();
		}

		List<PlatformReturnInstockDTO.Detail> remaining = new ArrayList<>();
		for (PlatformReturnInstockDTO.Detail detail : pushDetails) {
			int pushQty = resolveDetailQty(detail);
			if (pushQty <= 0) {
				continue;
			}
			String platformSkuUpper = CharSequenceUtil.blankToDefault(detail.getProductSku(), "").toUpperCase();
			String reconcileKey = reconcileKey(platformSkuUpper, detail.getDefectiveProductFlag());
			String defectiveSuffix = defectiveKeySuffix(detail.getDefectiveProductFlag());
			int left = pushQty;
			left -= takeConsumed(consumedByPlatformSku, reconcileKey, left);

			SkuMappingDTO.MappingSkuViewDTO mapping = skuMappingMap.get(platformSkuUpper);
			if (left > 0 && Objects.nonNull(mapping) && CharSequenceUtil.isNotBlank(mapping.getProductSkuId())) {
				left -= takeConsumed(prestockBySkuId, mapping.getProductSkuId() + defectiveSuffix, left);
			}
			if (left > 0 && Objects.nonNull(mapping) && CharSequenceUtil.isNotBlank(mapping.getProductSkuNo())) {
				left -= takeConsumed(prestockBySkuNo, mapping.getProductSkuNo().toUpperCase() + defectiveSuffix, left);
			}
			if (left > 0 && CharSequenceUtil.isNotBlank(detail.getProductSku())) {
				left -= takeConsumed(prestockBySkuNo, detail.getProductSku().toUpperCase() + defectiveSuffix, left);
			}
			if (left > 0) {
				remaining.add(copyDetailWithQty(detail, left, pushQty));
			}
		}

		int leftoverPrestockQty = prestockBySkuId.values().stream().mapToInt(Integer::intValue).sum()
				+ prestockBySkuNo.values().stream().mapToInt(Integer::intValue).sum();
		if (leftoverPrestockQty > 0) {
			log.warn("[海外仓退货入库] thirdCode={} 有预入库数量={} 未能对齐到本次推送平台SKU（映射缺失或编码不一致），未计入扣减，请人工核对",
					thirdCode, leftoverPrestockQty);
		}
		return remaining;
	}

	/**
	 * 推送明细数量合计（实收优先口径，同 {@link #resolveDetailQty}）。
	 */
	public int sumPushQty(List<PlatformReturnInstockDTO.Detail> details) {
		if (CollectionUtils.isEmpty(details)) {
			return 0;
		}
		return details.stream().mapToInt(this::resolveDetailQty).sum();
	}

	/**
	 * 加载指定 B2C 退货单仍有未入库缺口的明细（gap = returnQty - 已审核入库实退，仅保留 gap&gt;0）。
	 *
	 * @param matchedReturn B2C 退货单主表
	 * @return 有缺口的明细列表；无明细或已全部入库时返回空列表
	 */
	public List<B2cReturnGap> loadOpenB2cReturnGaps(SoB2cReturnEntity matchedReturn) {
		if (Objects.isNull(matchedReturn) || CharSequenceUtil.isBlank(matchedReturn.getId())) {
			return Collections.emptyList();
		}
		return loadOpenB2cReturnGapsByMains(Collections.singletonList(matchedReturn))
				.getOrDefault(matchedReturn.getId(), Collections.emptyList());
	}

	/**
	 * 批量加载多张 B2C 退货单的未入库缺口，避免候选挑选时逐单 Feign。
	 *
	 * @param returns 退货单主表列表
	 * @return mainId → 该单开放缺口列表
	 */
	public Map<String, List<B2cReturnGap>> loadOpenB2cReturnGapsByMains(List<SoB2cReturnEntity> returns) {
		if (CollUtil.isEmpty(returns)) {
			return Collections.emptyMap();
		}
		Map<String, SoB2cReturnEntity> returnMap = returns.stream()
				.filter(r -> Objects.nonNull(r) && CharSequenceUtil.isNotBlank(r.getId()))
				.collect(Collectors.toMap(SoB2cReturnEntity::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));
		if (returnMap.isEmpty()) {
			return Collections.emptyMap();
		}
		List<SoB2cReturnDetailDTO.ViewDTO> detailList = soB2cReturnFeign.listDetailByMainIds(new ArrayList<>(returnMap.keySet()));
		if (CollUtil.isEmpty(detailList)) {
			return Collections.emptyMap();
		}
		Map<String, Integer> instockQtyMap = sumApprovedInstockQtyByReturnDetailId(
				detailList.stream().map(SoB2cReturnDetailDTO.ViewDTO::getId).collect(Collectors.toList()));
		Map<String, List<B2cReturnGap>> result = new LinkedHashMap<>();
		for (SoB2cReturnDetailDTO.ViewDTO detail : detailList) {
			if (CharSequenceUtil.isBlank(detail.getSkuId()) || CharSequenceUtil.isBlank(detail.getMainId())) {
				continue;
			}
			int returnQty = Objects.nonNull(detail.getReturnQty()) ? detail.getReturnQty() : 0;
			int gapQty = returnQty - instockQtyMap.getOrDefault(detail.getId(), 0);
			if (gapQty <= 0) {
				continue;
			}
			SoB2cReturnEntity main = returnMap.get(detail.getMainId());
			B2cReturnGap gap = new B2cReturnGap();
			gap.detailId = detail.getId();
			gap.skuId = detail.getSkuId();
			gap.gapQty = gapQty;
			if (Objects.nonNull(main)) {
				gap.returnTypeDict = main.getType();
				gap.returnReasonDict = main.getReason();
			}
			result.computeIfAbsent(detail.getMainId(), k -> new ArrayList<>()).add(gap);
		}
		return result;
	}

	/**
	 * 判断 B2C 退货单是否已全部完成入库（所有明细 gap&lt;=0）。
	 * <p>
	 * 与 {@link #loadOpenB2cReturnGaps} 同一口径：应退数量对比已审核退货入库实退。
	 * </p>
	 *
	 * @param returnMainId 退货单主表 id
	 * @return true=无未入库缺口（含无明细）；false=仍有缺口或主表 id 为空
	 */
	public boolean isB2cReturnFullyInstocked(String returnMainId) {
		if (CharSequenceUtil.isBlank(returnMainId)) {
			return false;
		}
		SoB2cReturnEntity stub = new SoB2cReturnEntity();
		stub.setId(returnMainId);
		return CollUtil.isEmpty(loadOpenB2cReturnGaps(stub));
	}

	/**
	 * 将销售订单已匹配的入库明细，按退货单尚未入库缺口分配：
	 * 命中部分写入关联退货明细的入库行；超出数量与订单未匹配行转入剩余推送明细。
	 *
	 * @param soMatchedLines   已按销售订单匹配出的入库明细
	 * @param soUnmatchedLines 销售订单未匹配的平台推送明细
	 * @param gaps             退货单开放缺口（会被原地扣减）
	 * @param defaultReturnType 缺口上退货类型为空时的兜底
	 * @return 分配结果
	 */
	public ReturnGapAllocationResult allocateAgainstB2cReturnGaps(
			List<SoReturnInstockDetailEntity> soMatchedLines,
			List<PlatformReturnInstockDTO.Detail> soUnmatchedLines,
			List<B2cReturnGap> gaps,
			String defaultReturnType) {
		ReturnGapAllocationResult result = new ReturnGapAllocationResult();
		Map<String, List<B2cReturnGap>> gapsBySkuId = new LinkedHashMap<>();
		if (CollUtil.isNotEmpty(gaps)) {
			for (B2cReturnGap gap : gaps) {
				gapsBySkuId.computeIfAbsent(gap.getSkuId(), k -> new ArrayList<>()).add(gap);
			}
		}
		if (CollUtil.isNotEmpty(soMatchedLines)) {
			for (SoReturnInstockDetailEntity line : soMatchedLines) {
				int mustQty = Objects.nonNull(line.getMustQty()) ? line.getMustQty() : 0;
				int receiveQty = Objects.nonNull(line.getReceiveQty()) ? line.getReceiveQty() : 0;
				int realQty = Objects.nonNull(line.getRealQty()) ? line.getRealQty() : 0;
				if (mustQty <= 0) {
					continue;
				}
				List<B2cReturnGap> skuGaps = gapsBySkuId.getOrDefault(line.getSkuId(), Collections.emptyList());
				int mustLeft = mustQty;
				int receiveLeft = receiveQty;
				int realLeft = realQty;
				SoReturnInstockDetailEntity lastAllocated = null;
				for (B2cReturnGap gap : skuGaps) {
					if (mustLeft <= 0 || gap.gapQty <= 0) {
						continue;
					}
					int allocateQty = Math.min(mustLeft, gap.gapQty);
					int allocateReceiveQty = Math.min(receiveLeft, proportionalFloor(receiveQty, allocateQty, mustQty));
					int allocateRealQty = Math.min(realLeft, proportionalFloor(realQty, allocateQty, mustQty));

					SoReturnInstockDetailEntity part = copyInstockDetail(line);
					part.setMustQty(allocateQty);
					part.setReceiveQty(allocateReceiveQty);
					part.setRealQty(allocateRealQty);
					part.setSoReturnDetailId(gap.detailId);
					part.setReturnTypeDict(CharSequenceUtil.emptyToDefault(gap.returnTypeDict, defaultReturnType));
					part.setReturnReasonDict(CharSequenceUtil.nullToDefault(gap.returnReasonDict, ""));
					result.returnLinkedList.add(part);

					gap.gapQty -= allocateQty;
					mustLeft -= allocateQty;
					receiveLeft -= allocateReceiveQty;
					realLeft -= allocateRealQty;
					lastAllocated = part;
				}
				// 应退已分完时，签收/实退按比例向下取整的余数补到最后一次分配行，保证总量守恒
				if (mustLeft <= 0 && lastAllocated != null && (receiveLeft > 0 || realLeft > 0)) {
					lastAllocated.setReceiveQty(lastAllocated.getReceiveQty() + receiveLeft);
					lastAllocated.setRealQty(lastAllocated.getRealQty() + realLeft);
					receiveLeft = 0;
					realLeft = 0;
				}
				if (mustLeft > 0) {
					result.remainingPushDetails.add(toPlatformDetail(line, mustLeft, receiveLeft, realLeft));
				}
			}
		}
		if (CollUtil.isNotEmpty(soUnmatchedLines)) {
			result.remainingPushDetails.addAll(soUnmatchedLines);
		}
		result.allGapsClosed = CollUtil.isEmpty(gaps) || gaps.stream().allMatch(g -> g.gapQty <= 0);
		return result;
	}

	/**
	 * 按售后单明细 id 统计已审核入库单的实退数量之和。
	 *
	 * @param soReturnDetailIds 售后/退货单明细 id 列表
	 * @return detailId → 已审核实退合计
	 */
	public Map<String, Integer> sumApprovedInstockQtyByReturnDetailId(List<String> soReturnDetailIds) {
		if (CollUtil.isEmpty(soReturnDetailIds)) {
			return Collections.emptyMap();
		}
		List<SoReturnInstockDetailEntity> instockDetailList = soReturnInstockDetailService.lambdaQuery()
				.in(SoReturnInstockDetailEntity::getSoReturnDetailId, soReturnDetailIds)
				.list();
		if (CollUtil.isEmpty(instockDetailList)) {
			return Collections.emptyMap();
		}
		List<String> mainIds = instockDetailList.stream().map(SoReturnInstockDetailEntity::getMainId)
				.filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
		if (CollUtil.isEmpty(mainIds)) {
			return Collections.emptyMap();
		}
		java.util.Set<String> approvedMainIds = soReturnInstockService.lambdaQuery()
				.in(SoReturnInstockEntity::getId, mainIds)
				.eq(SoReturnInstockEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getStatus())
				.list().stream().map(SoReturnInstockEntity::getId).collect(Collectors.toSet());
		return instockDetailList.stream()
				.filter(d -> approvedMainIds.contains(d.getMainId()))
				.collect(Collectors.groupingBy(SoReturnInstockDetailEntity::getSoReturnDetailId,
						Collectors.summingInt(d -> Objects.nonNull(d.getRealQty()) ? d.getRealQty() : 0)));
	}

	/**
	 * 判断两批明细的「平台SKU + 不良品标志 + 数量」指纹是否一致（忽略顺序）。
	 */
	public boolean sameDetailQtyFingerprint(List<PlatformReturnInstockDTO.Detail> a, List<PlatformReturnInstockDTO.Detail> b) {
		if (CollectionUtils.isEmpty(a) && CollectionUtils.isEmpty(b)) {
			return true;
		}
		if (CollectionUtils.isEmpty(a) || CollectionUtils.isEmpty(b) || a.size() != b.size()) {
			return false;
		}
		Map<String, Integer> mapA = new HashMap<>();
		Map<String, Integer> mapB = new HashMap<>();
		for (PlatformReturnInstockDTO.Detail detail : a) {
			mapA.merge(reconcileKey(detail.getProductSku(), detail.getDefectiveProductFlag()), resolveDetailQty(detail), Integer::sum);
		}
		for (PlatformReturnInstockDTO.Detail detail : b) {
			mapB.merge(reconcileKey(detail.getProductSku(), detail.getDefectiveProductFlag()), resolveDetailQty(detail), Integer::sum);
		}
		return mapA.equals(mapB);
	}

	/**
	 * 平台推送明细数量：与预入库落库口径一致，优先 receiveQty，空或&lt;=0 时兜底 realQty、mustQty。
	 */
	public int resolveDetailQty(PlatformReturnInstockDTO.Detail detail) {
		if (Objects.isNull(detail)) {
			return 0;
		}
		if (Objects.nonNull(detail.getReceiveQty()) && detail.getReceiveQty() > 0) {
			return detail.getReceiveQty();
		}
		if (Objects.nonNull(detail.getRealQty()) && detail.getRealQty() > 0) {
			return detail.getRealQty();
		}
		if (Objects.nonNull(detail.getMustQty()) && detail.getMustQty() > 0) {
			return detail.getMustQty();
		}
		return 0;
	}

	/**
	 * 按应退分配比例向下取整分摊签收/实退数量。
	 *
	 * @param totalQty     待分摊总量
	 * @param allocateBase 本次分配的应退数量
	 * @param totalBase    原应退总量
	 * @return 分摊结果（向下取整）
	 */
	private int proportionalFloor(int totalQty, int allocateBase, int totalBase) {
		if (totalBase <= 0 || totalQty <= 0 || allocateBase <= 0) {
			return 0;
		}
		return (int) Math.floor(totalQty * (double) allocateBase / totalBase);
	}

	/**
	 * 浅拷贝入库明细关键业务字段，供按缺口拆行使用。
	 *
	 * @param source 原入库明细
	 * @return 新明细（未设置数量与售后明细 id）
	 */
	private SoReturnInstockDetailEntity copyInstockDetail(SoReturnInstockDetailEntity source) {
		SoReturnInstockDetailEntity copy = new SoReturnInstockDetailEntity();
		copy.setSkuId(source.getSkuId());
		copy.setSkuNo(source.getSkuNo());
		copy.setPlatformSkuNo(source.getPlatformSkuNo());
		copy.setWarehouseId(source.getWarehouseId());
		copy.setWarehouseName(source.getWarehouseName());
		copy.setRemark(source.getRemark());
		copy.setReturnTypeDict(source.getReturnTypeDict());
		copy.setReturnReasonDict(source.getReturnReasonDict());
		copy.setDefectiveProductFlag(source.getDefectiveProductFlag());
		return copy;
	}

	/**
	 * 将超出退货缺口的入库明细还原为平台推送明细，供后续销售订单匹配/预入库。
	 *
	 * @param line        原入库明细（取平台 SKU、不良品等）
	 * @param mustLeft    剩余应退
	 * @param receiveLeft 剩余签收
	 * @param realLeft    剩余实退
	 * @return 平台推送明细
	 */
	private PlatformReturnInstockDTO.Detail toPlatformDetail(SoReturnInstockDetailEntity line,
															 int mustLeft, int receiveLeft, int realLeft) {
		PlatformReturnInstockDTO.Detail detail = new PlatformReturnInstockDTO.Detail();
		detail.setProductSku(line.getPlatformSkuNo());
		detail.setMustQty(mustLeft);
		detail.setReceiveQty(receiveLeft);
		detail.setRealQty(realLeft);
		detail.setDefectiveProductFlag(line.getDefectiveProductFlag());
		return detail;
	}

	/**
	 * 汇总同 thirdCode 退货入库单明细已处理数量（键=平台SKU#不良品，数量口径同 {@link #resolveInstockDetailQty}）。
	 */
	private void accumulateInstockConsumedByPlatformSku(String thirdCode, Map<String, Integer> consumedByPlatformSku) {
		List<SoReturnInstockEntity> instockList = soReturnInstockService.listByThirdCode(thirdCode);
		if (CollectionUtils.isEmpty(instockList)) {
			return;
		}
		List<String> mainIds = instockList.stream().map(SoReturnInstockEntity::getId).collect(Collectors.toList());
		List<SoReturnInstockDetailEntity> details = soReturnInstockDetailService.listDetailByMainIds(mainIds);
		if (CollectionUtils.isEmpty(details)) {
			return;
		}
		for (SoReturnInstockDetailEntity detail : details) {
			if (CharSequenceUtil.isBlank(detail.getPlatformSkuNo())) {
				continue;
			}
			int qty = resolveInstockDetailQty(detail);
			if (qty <= 0) {
				continue;
			}
			String key = reconcileKey(detail.getPlatformSkuNo(), detail.getDefectiveProductFlag());
			consumedByPlatformSku.merge(key, qty, Integer::sum);
		}
	}

	/**
	 * 汇总同 thirdCode 预入库单明细数量：按 skuId/skuNo + 不良品标志入池。
	 */
	private void accumulatePrestockConsumed(String thirdCode, Map<String, Integer> bySkuId, Map<String, Integer> bySkuNo) {
		List<SoReturnPrestockEntity> prestockList = soReturnPrestockService.listByThirdCode(thirdCode);
		if (CollectionUtils.isEmpty(prestockList)) {
			return;
		}
		List<String> mainIds = prestockList.stream().map(SoReturnPrestockEntity::getId).collect(Collectors.toList());
		List<SoReturnPrestockDetailEntity> details = soReturnPrestockDetailService.listByMainIds(mainIds);
		if (CollectionUtils.isEmpty(details)) {
			return;
		}
		for (SoReturnPrestockDetailEntity detail : details) {
			int qty = Objects.nonNull(detail.getReceivedQty()) ? detail.getReceivedQty() : 0;
			if (qty <= 0) {
				continue;
			}
			String defectiveSuffix = defectiveKeySuffix(detail.getDefectiveProductFlag());
			if (CharSequenceUtil.isNotBlank(detail.getSkuId())) {
				bySkuId.merge(detail.getSkuId() + defectiveSuffix, qty, Integer::sum);
			} else if (CharSequenceUtil.isNotBlank(detail.getSkuNo())) {
				bySkuNo.merge(detail.getSkuNo().toUpperCase() + defectiveSuffix, qty, Integer::sum);
			}
		}
	}

	/**
	 * 退货入库明细已处理数量：与推送/预入库对账口径一致，优先 receiveQty，空或&lt;=0 时兜底 realQty、mustQty。
	 */
	private int resolveInstockDetailQty(SoReturnInstockDetailEntity detail) {
		if (Objects.isNull(detail)) {
			return 0;
		}
		if (Objects.nonNull(detail.getReceiveQty()) && detail.getReceiveQty() > 0) {
			return detail.getReceiveQty();
		}
		if (Objects.nonNull(detail.getRealQty()) && detail.getRealQty() > 0) {
			return detail.getRealQty();
		}
		if (Objects.nonNull(detail.getMustQty()) && detail.getMustQty() > 0) {
			return detail.getMustQty();
		}
		return 0;
	}

	private int takeConsumed(Map<String, Integer> pool, String key, int maxTake) {
		if (maxTake <= 0 || CharSequenceUtil.isBlank(key) || !pool.containsKey(key)) {
			return 0;
		}
		int available = pool.get(key);
		int take = Math.min(available, maxTake);
		if (take <= 0) {
			return 0;
		}
		int left = available - take;
		if (left <= 0) {
			pool.remove(key);
		} else {
			pool.put(key, left);
		}
		return take;
	}

	private List<PlatformReturnInstockDTO.Detail> copyDetails(List<PlatformReturnInstockDTO.Detail> source) {
		if (CollectionUtils.isEmpty(source)) {
			return Collections.emptyList();
		}
		List<PlatformReturnInstockDTO.Detail> copy = new ArrayList<>(source.size());
		for (PlatformReturnInstockDTO.Detail detail : source) {
			copy.add(copyDetailWithQty(detail, resolveDetailQty(detail), resolveDetailQty(detail)));
		}
		return copy;
	}

	/**
	 * 复制推送明细行并按缺口重写数量。
	 * <p>
	 * 部分缺口：{@code receiveQty=leftQty}（与预入库落库口径一致），
	 * {@code mustQty=leftQty}（匹配驱动量，避免 floor 成 0 导致物流匹配跳过），
	 * {@code realQty} 按比例向下取整。
	 * 全量复制：保留原字段；若原 mustQty&lt;=0 但缺口&gt;0，则补 mustQty=leftQty，避免仅有实收时匹配被跳过。
	 * </p>
	 *
	 * @param source      原明细
	 * @param leftQty     缺口数量（实收口径）
	 * @param originalQty 原推送用于分摊的基准数量（实收口径）
	 */
	private PlatformReturnInstockDTO.Detail copyDetailWithQty(PlatformReturnInstockDTO.Detail source, int leftQty, int originalQty) {
		PlatformReturnInstockDTO.Detail copy = new PlatformReturnInstockDTO.Detail();
		copy.setThirdBarcode(source.getThirdBarcode());
		copy.setProductSku(source.getProductSku());
		copy.setThirdId(source.getThirdId());
		copy.setDefectiveProductFlag(source.getDefectiveProductFlag());
		int mustQty = Objects.nonNull(source.getMustQty()) ? source.getMustQty() : 0;
		int receiveQty = Objects.nonNull(source.getReceiveQty()) ? source.getReceiveQty() : 0;
		int realQty = Objects.nonNull(source.getRealQty()) ? source.getRealQty() : 0;
		if (originalQty > 0 && leftQty < originalQty) {
			copy.setReceiveQty(leftQty);
			// 与旧 RestCloud setMustQty(leftQty) 对齐：匹配驱动量等于缺口，禁止 floor 成 0
			copy.setMustQty(leftQty);
			copy.setRealQty((int) Math.floor(realQty * (double) leftQty / originalQty));
		} else {
			copy.setReceiveQty(receiveQty);
			copy.setRealQty(realQty);
			// 原 must=0 且只有实收时，补齐 must 以便下游物流匹配（mustQty>0）可进入
			copy.setMustQty(mustQty > 0 ? mustQty : leftQty);
		}
		return copy;
	}

	/**
	 * 对账键：平台SKU（大写）+ 不良品标志，避免同 SKU 良品/不良品互相扣减。
	 */
	private String reconcileKey(String platformSku, Boolean defectiveProductFlag) {
		return CharSequenceUtil.blankToDefault(platformSku, "").toUpperCase() + defectiveKeySuffix(defectiveProductFlag);
	}

	private String defectiveKeySuffix(Boolean defectiveProductFlag) {
		return "#" + Boolean.TRUE.equals(defectiveProductFlag);
	}
}
