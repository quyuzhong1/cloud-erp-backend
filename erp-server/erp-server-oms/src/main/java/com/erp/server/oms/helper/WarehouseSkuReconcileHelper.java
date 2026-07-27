package com.erp.server.oms.helper;

import cn.hutool.core.collection.CollUtil;
import com.erp.model.oms.entity.ListingInfoEntity;
import com.erp.model.oms.entity.SkuMappingEntity;
import com.erp.model.oms.enums.ListingMatchResultEnum;
import com.erp.model.oms.enums.ListingSourceTypeEnum;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.oms.enums.SkuMappingStatusEnum;
import com.erp.server.oms.service.ListingInfoService;
import com.erp.server.oms.service.SkuMappingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 三方仓（WAREHOUSE）SKU 对照启停回收：
 * 源端停用且已映射 → mapping 置禁用；源端停用且未匹配占位 → 软删 listing/mapping。
 * 供旧链路 MQ 消费与（过渡期）Feign 同步共用，避免两套规则分叉。
 */
@Slf4j
@Component
public class WarehouseSkuReconcileHelper {

    private static final String STATUS_ACTIVE = "active";

    @Resource
    private SkuMappingService skuMappingService;
    @Lazy
    @Resource
    private ListingInfoService listingInfoService;

    /**
     * 源端状态是否为停用：非空且非 Active（ignoreCase）；未传状态不参与启停回收。
     */
    public static boolean isInactivePlatformStatus(String platformStatus) {
        return StringUtils.isNotBlank(platformStatus) && !STATUS_ACTIVE.equalsIgnoreCase(platformStatus);
    }

    /**
     * 对单条已存在的仓库 listing 执行停用回收（MQ 单条消息场景）。
     *
     * @return 是否执行了禁用或删除
     */
    public boolean reconcileInactiveIfNeeded(ListingInfoEntity listing, String platformStatus) {
        if (listing == null || !isInactivePlatformStatus(platformStatus)) {
            return false;
        }
        if (!RuleTypeEnum.WAREHOUSE.getCode().equalsIgnoreCase(listing.getType())) {
            return false;
        }
        ReconcileCounts counts = reconcileInactiveListings(Collections.singletonList(listing));
        return counts.disabledCount > 0 || counts.deletedCount > 0;
    }

    /**
     * 对一批「源端已停用」的仓库 listing 执行禁用/软删。
     * 仅处理 {@link ListingSourceTypeEnum#THIRD}；{@code matchResult=not} 不参与软删。
     */
    public ReconcileCounts reconcileInactiveListings(List<ListingInfoEntity> inactiveListings) {
        ReconcileCounts counts = new ReconcileCounts();
        if (CollectionUtils.isEmpty(inactiveListings)) {
            return counts;
        }
        List<ListingInfoEntity> thirdListings = inactiveListings.stream()
                .filter(this::isReconcileEligibleSource)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(thirdListings)) {
            return counts;
        }

        List<ListingInfoEntity> matchedListing = thirdListings.stream()
                .filter(li -> ListingMatchResultEnum.TRUE.getCode().equals(li.getMatchResult()))
                .collect(Collectors.toList());
        List<ListingInfoEntity> unmatchedListing = thirdListings.stream()
                .filter(li -> ListingMatchResultEnum.FALSE.getCode().equals(li.getMatchResult()))
                .collect(Collectors.toList());

        List<String> listingIdsToDelete = new ArrayList<>();
        List<String> mappingIdsToDelete = new ArrayList<>();
        List<SkuMappingEntity> toDisableMappings = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(matchedListing)) {
            List<String> matchedListingIds = matchedListing.stream().map(ListingInfoEntity::getId).collect(Collectors.toList());
            List<SkuMappingEntity> enabledMappings = skuMappingService.listByListingIds(matchedListingIds).stream()
                    .filter(sm -> RuleTypeEnum.WAREHOUSE.equals(sm.getType()))
                    .filter(sm -> Objects.isNull(sm.getStatus()) || SkuMappingStatusEnum.ENABLE.equals(sm.getStatus()))
                    .collect(Collectors.toList());
            toDisableMappings.addAll(enabledMappings);
        }

        if (CollectionUtils.isNotEmpty(unmatchedListing)) {
            List<String> unmatchedListingIds = unmatchedListing.stream().map(ListingInfoEntity::getId).collect(Collectors.toList());
            List<SkuMappingEntity> warehouseMappings = skuMappingService.listByListingIds(unmatchedListingIds).stream()
                    .filter(sm -> RuleTypeEnum.WAREHOUSE.equals(sm.getType()))
                    .collect(Collectors.toList());
            Map<String, List<SkuMappingEntity>> mappingsByListingId = warehouseMappings.stream()
                    .collect(Collectors.groupingBy(SkuMappingEntity::getListingId));

            for (ListingInfoEntity listing : unmatchedListing) {
                List<SkuMappingEntity> mappings = mappingsByListingId.getOrDefault(listing.getId(), Collections.emptyList());
                boolean hasRealMapping = mappings.stream().anyMatch(sm -> StringUtils.isNotBlank(sm.getProductSkuId()));
                if (hasRealMapping) {
                    mappings.stream()
                            .filter(sm -> Objects.isNull(sm.getStatus()) || SkuMappingStatusEnum.ENABLE.equals(sm.getStatus()))
                            .forEach(toDisableMappings::add);
                    continue;
                }
                listingIdsToDelete.add(listing.getId());
                mappings.forEach(sm -> mappingIdsToDelete.add(sm.getId()));
            }
        }

        if (CollectionUtils.isNotEmpty(toDisableMappings)) {
            Map<String, SkuMappingEntity> disableMap = toDisableMappings.stream()
                    .collect(Collectors.toMap(SkuMappingEntity::getId, Function.identity(), (a, b) -> a));
            List<SkuMappingEntity> distinctToDisable = new ArrayList<>(disableMap.values());
            distinctToDisable.forEach(sm -> sm.setStatus(SkuMappingStatusEnum.DISABLE));
            CollUtil.split(distinctToDisable, 500).forEach(batch -> skuMappingService.updateBatchById(batch));
            counts.disabledCount = distinctToDisable.size();
            log.warn("[三方仓SKU回收] 映射关系置为禁用 {}条（源端已停用，需人工确认后手动重新启用）", counts.disabledCount);
        }

        if (CollectionUtils.isNotEmpty(listingIdsToDelete)) {
            if (CollectionUtils.isNotEmpty(mappingIdsToDelete)) {
                CollUtil.split(mappingIdsToDelete, 500).forEach(batch -> skuMappingService.removeByIds(batch));
            }
            CollUtil.split(listingIdsToDelete, 500).forEach(batch -> listingInfoService.removeByIds(batch));
            counts.deletedCount = listingIdsToDelete.size();
            log.warn("[三方仓SKU回收] 未匹配且源端已停用，软删 listing {}条", counts.deletedCount);
        }
        return counts;
    }

    /**
     * 参与启停回收的来源：明确 third；或历史 MQ 漏写导致 sourceType 为空（排除 selfAdd 手工新增）。
     */
    private boolean isReconcileEligibleSource(ListingInfoEntity listing) {
        if (listing == null) {
            return false;
        }
        String sourceType = listing.getSourceType();
        if (ListingSourceTypeEnum.SELF_ADD.getCode().equals(sourceType)) {
            return false;
        }
        return StringUtils.isBlank(sourceType)
                || ListingSourceTypeEnum.THIRD.getCode().equals(sourceType);
    }

    public static class ReconcileCounts {
        private int disabledCount;
        private int deletedCount;

        public int getDisabledCount() {
            return disabledCount;
        }

        public int getDeletedCount() {
            return deletedCount;
        }
    }
}
