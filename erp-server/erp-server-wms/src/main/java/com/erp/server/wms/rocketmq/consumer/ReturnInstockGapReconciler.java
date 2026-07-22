package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.dto.PlatformReturnInstockDTO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.wms.entity.SoReturnInstockDetailEntity;
import com.erp.model.wms.entity.SoReturnInstockEntity;
import com.erp.model.wms.entity.SoReturnPrestockDetailEntity;
import com.erp.model.wms.entity.SoReturnPrestockEntity;
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
