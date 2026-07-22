package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.WarehouseLocationMoveDTO;
import com.erp.model.wms.dto.WarehouseLocationMoveDetailDTO;
import com.erp.model.wms.dto.inventory.InOutStockDTO;
import com.erp.model.wms.dto.pickingstrategy.CfgRulePickingDTO;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.entity.WarehouseLocationMoveDetailEntity;
import com.erp.model.wms.entity.WarehouseLocationMoveEntity;
import com.erp.model.wms.enums.PickingBillTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.server.wms.service.CfgRulePickingService;
import com.erp.server.wms.service.WarehouseLocationMoveDetailService;
import com.erp.server.wms.service.WarehouseLocationMoveService;
import com.erp.server.wms.service.WarehouseService;
import com.erp.server.wms.service.WdtSoOutstockAutoMoveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 旺店通销售出库同步前预检：当前库位不足则改空仓位；
 * 移仓数量 = 出库数量 - 空仓位已有，源仓排除空仓位与当前库位。
 */
@Slf4j
@Service
public class WdtSoOutstockAutoMoveServiceImpl implements WdtSoOutstockAutoMoveService {

    private static final String EMPTY_LOCATION = "";

    @Resource
    private CfgRulePickingService cfgRulePickingService;
    @Resource
    private WarehouseService warehouseService;
    @Resource
    private WarehouseLocationMoveService warehouseLocationMoveService;
    @Resource
    private WarehouseLocationMoveDetailService warehouseLocationMoveDetailService;

    @Override
    public void preCheckAndAutoMove(SoOutstockEntity soOutstock,
                                    List<SoOutstockDetailEntity> detailList,
                                    List<InOutStockDTO> inOutStockList,
                                    String sourceId,
                                    String sourceCode) {
        if (soOutstock == null || CollUtil.isEmpty(detailList) || CollUtil.isEmpty(inOutStockList)) {
            return;
        }

        // inOutStockList 只包含需要直接扣减可用库存的 SKU，无需管理库存的 SKU 不参与推荐。
        Set<String> deductionDetailIds = inOutStockList.stream()
                .map(InOutStockDTO::getSourceDetailId)
                .filter(CharSequenceUtil::isNotBlank)
                .collect(Collectors.toSet());
        List<SoOutstockDetailEntity> deductionDetails = detailList.stream()
                .filter(Objects::nonNull)
                .filter(detail -> deductionDetailIds.contains(detail.getId()))
                .filter(detail -> detail.getActualQty() != null && detail.getActualQty() > 0)
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(deductionDetails)) {
            return;
        }

        try {
            applyOutStockRule(soOutstock, deductionDetails, inOutStockList, sourceId, sourceCode);
        } catch (ServiceException e) {
            log.warn("旺店通出库仓位推荐失败 sourceId={} code={} msg={}",
                    sourceId, sourceCode, e.getMsg());
            throw e;
        } catch (Exception e) {
            String skuNo = deductionDetails.get(0).getSkuNo();
            log.error("旺店通出库仓位推荐异常 sourceId={} code={} skuNo={}",
                    sourceId, sourceCode, skuNo, e);
            throw new ServiceException(ApiError.WH_OUT_STOCK_MOVE_FAILED, skuNo, "系统异常");
        }
    }

    private void applyOutStockRule(SoOutstockEntity soOutstock,
                                   List<SoOutstockDetailEntity> deductionDetails,
                                   List<InOutStockDTO> inOutStockList,
                                   String sourceId,
                                   String sourceCode) {
        CfgRulePickingDTO.CfgExecutionDataDTO executionData =
                buildExecutionData(soOutstock, deductionDetails);
        Map<String, List<SoOutstockDetailEntity>> detailsByWarehouse = deductionDetails.stream()
                .collect(Collectors.groupingBy(
                        detail -> CharSequenceUtil.isNotBlank(detail.getWarehouseId())
                                ? detail.getWarehouseId() : soOutstock.getWarehouseId(),
                        LinkedHashMap::new, Collectors.toList()));

        Map<String, WarehouseEntity> warehouseMap = warehouseService.listByIds(detailsByWarehouse.keySet()).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(WarehouseEntity::getId, Function.identity(), (left, right) -> left));

        for (Map.Entry<String, List<SoOutstockDetailEntity>> entry : detailsByWarehouse.entrySet()) {
            String warehouseId = entry.getKey();
            WarehouseEntity warehouse = warehouseMap.get(warehouseId);
            if (warehouse != null && Boolean.FALSE.equals(warehouse.getIsEnableLocation())) {
                rewriteLocation(entry.getValue(), inOutStockList, EMPTY_LOCATION);
                continue;
            }

            // 重试恢复：若已有有效移仓单，先按移仓明细回写目标仓位，再仅对剩余明细做推荐
            List<SoOutstockDetailEntity> needResolveDetails = restoreMovedLocationsIfPresent(
                    sourceId, warehouseId, entry.getValue(), inOutStockList);
            if (CollUtil.isEmpty(needResolveDetails)) {
                continue;
            }

            List<NeedStock> needList = entry.getValue();
            // 按仓批量加载本单涉及 SKU 的可用库存：同时用于当前库位预检 + 候选源仓分配
            WarehouseInventoryCache inventoryCache = loadWarehouseInventoryCache(warehouse, needList);

            List<WarehouseLocationMoveDetailDTO.AddDTO> moveDetailList = new ArrayList<>();
            for (NeedStock need : needList) {
                planMoveForNeed(need, needList, detailList, inOutStockList, moveDetailList, inventoryCache);
            }
            moveDetailList.removeIf(d -> Objects.equals(d.getInWarehouseLocation(), d.getOutWarehouseLocation())
                    || d.getQty() == null || d.getQty() <= 0);
            if (CollUtil.isEmpty(moveDetailList)) {
                continue;
            }
            String target = matched.getInWarehouseLocation() == null ? EMPTY_LOCATION : matched.getInWarehouseLocation();
            rewriteLocation(Collections.singletonList(detail), inOutStockList, target);
        }
        if (CollUtil.isNotEmpty(unmatched)) {
            String skuNo = unmatched.get(0).getSkuNo();
            throw new ServiceException(ApiError.WH_OUT_STOCK_MOVE_FAILED, skuNo,
                    "已有移仓明细无法匹配到出库明细，请人工处理");
        }
        log.warn("旺店通出库预检按已有移仓单恢复仓位 sourceId={} warehouseId={} restored={} remain={}",
                sourceId, warehouseId, warehouseDetails.size() - needResolve.size(), needResolve.size());
        return needResolve;
    }

    private WarehouseLocationMoveDetailEntity pollMatchedMoveDetail(List<WarehouseLocationMoveDetailEntity> candidates,
                                                                    SoOutstockDetailEntity detail) {
        if (CollUtil.isEmpty(candidates) || detail == null) {
            return null;
        }
        // 1) 稳定来源明细 ID（重试可命中）
        for (Iterator<WarehouseLocationMoveDetailEntity> it = candidates.iterator(); it.hasNext(); ) {
            WarehouseLocationMoveDetailEntity candidate = it.next();
            if (CharSequenceUtil.isNotBlank(detail.getId())
                    && Objects.equals(detail.getId(), candidate.getSourceDetailId())) {
                it.remove();
                return candidate;
            }
        }
        // 2) SKU + 数量：仅在候选中唯一时匹配，禁止同 SKU 多行猜测绑定
        List<WarehouseLocationMoveDetailEntity> qtyMatches = candidates.stream()
                .filter(candidate -> Objects.equals(detail.getSkuId(), candidate.getSkuId())
                        && Objects.equals(detail.getActualQty(), candidate.getQty()))
                .collect(Collectors.toList());
        if (qtyMatches.size() == 1) {
            WarehouseLocationMoveDetailEntity matched = qtyMatches.get(0);
            candidates.remove(matched);
            return matched;
        }
        if (qtyMatches.size() > 1) {
            throw new ServiceException(ApiError.WH_OUT_STOCK_MOVE_FAILED, detail.getSkuNo(),
                    "同SKU多行移仓明细无法唯一匹配，请人工处理");
        }
        return null;
    }

    private void applySuggests(String warehouseId,
                               List<SoOutstockDetailEntity> warehouseDetails,
                               List<InOutStockDTO> inOutStockList,
                               List<CfgRulePickingDTO.OutStockLocationSuggestDTO> suggests,
                               String sourceId,
                               String sourceCode) {
        Map<String, SoOutstockDetailEntity> detailMap = warehouseDetails.stream()
                .collect(Collectors.toMap(BaseEntity::getId, Function.identity(), (left, right) -> left));
        Map<String, List<InOutStockDTO>> stockByDetailId = inOutStockList.stream()
                .filter(item -> CharSequenceUtil.isNotBlank(item.getSourceDetailId()))
                .collect(Collectors.groupingBy(InOutStockDTO::getSourceDetailId));
        List<WarehouseLocationMoveDetailDTO.AddDTO> moveDetails = new ArrayList<>();
        for (CfgRulePickingDTO.OutStockLocationSuggestDTO suggest : suggests) {
            if (Boolean.TRUE.equals(suggest.getNeedMove())) {
                WarehouseLocationMoveDetailDTO.AddDTO moveDetail =
                        WarehouseLocationMoveDetailDTO.AddDTO.getLocationMoveDTO(
                                suggest.getSkuId(), suggest.getSkuNo(), suggest.getStockLocation(),
                                EMPTY_LOCATION, suggest.getQty(), warehouseId, suggest.getDetailId());
                moveDetail.setOutInventoryStatus(InventoryStatusEnum.USABLE.getCode());
                moveDetail.setInInventoryStatus(InventoryStatusEnum.USABLE.getCode());
                moveDetail.setRemark("旺店通销售出库按出库配置自动移仓");
                moveDetails.add(moveDetail);
            }
        }
        // 已有移仓却仍解析出需移仓明细：先失败，避免改写仓位后静默跳过
        if (CollUtil.isNotEmpty(moveDetails) && findValidMovedBySource(sourceId, warehouseId) != null) {
            String skuNo = moveDetails.get(0).getSkuNo();
            throw new ServiceException(ApiError.WH_OUT_STOCK_MOVE_FAILED, skuNo,
                    "已存在移仓单但仍有未覆盖的移仓明细，请人工处理");
        }

        for (CfgRulePickingDTO.OutStockLocationSuggestDTO suggest : suggests) {
            SoOutstockDetailEntity detail = detailMap.get(suggest.getDetailId());
            if (detail == null) {
                continue;
            }
            detail.setWarehouseLocation(suggest.getTargetLocation());
            stockByDetailId.getOrDefault(suggest.getDetailId(), Collections.emptyList())
                    .forEach(item -> item.setWarehouseLocation(suggest.getTargetLocation()));
        }

        if (CollUtil.isEmpty(moveDetails)) {
            return;
        }
        WarehouseLocationMoveDTO.PcAddDTO addDTO = new WarehouseLocationMoveDTO.PcAddDTO();
        addDTO.setBillDate(LocalDate.now());
        addDTO.setWarehouseId(warehouseId);
        addDTO.setSourceId(sourceId);
        addDTO.setSourceCode(CharSequenceUtil.blankToDefault(sourceCode, sourceId));
        addDTO.setSourceType(SourceTypeEnum.SO_OUTSTOCK.getCode());
        addDTO.setDetailList(moveDetails);
        warehouseLocationMoveService.wdtAutoAddNewTx(addDTO);
    }

    /**
     * 按仓库一次查出本单 SKU 的全部可用库存，构建当前库位数量 Map 与候选源仓列表。
     */
    private WarehouseInventoryCache loadWarehouseInventoryCache(WarehouseEntity warehouse, List<NeedStock> needList) {
        Set<String> skuIds = needList.stream()
                .map(NeedStock::getSkuId)
                .filter(CharSequenceUtil::isNotBlank)
                .collect(Collectors.toSet());
        WarehouseInventoryCache cache = new WarehouseInventoryCache();
        if (CollUtil.isEmpty(skuIds)) {
            return cache;
        }
        List<InventoryEntity> inventoryList = inventoryService.lambdaQuery()
                .eq(InventoryEntity::getOrgId, warehouse.getOrgId())
                .eq(InventoryEntity::getWarehouseId, warehouse.getId())
                .eq(InventoryEntity::getDictInventoryStatus, InventoryStatusEnum.USABLE.getCode())
                .in(InventoryEntity::getSkuId, skuIds)
                .list();
        if (CollUtil.isEmpty(inventoryList)) {
            return cache;
        }
        for (InventoryEntity inv : inventoryList) {
            if (inv == null || CharSequenceUtil.isBlank(inv.getSkuId())) {
                continue;
            }
            String location = CharSequenceUtil.nullToEmpty(inv.getWarehouseLocation());
            int qty = inv.getQty() == null ? 0 : inv.getQty();
            cache.qtyBySkuLocation.put(buildSkuLocationKey(inv.getSkuId(), location), qty);
            if (qty <= 0) {
                continue;
            }
            LocationListDTO locationDto = new LocationListDTO();
            locationDto.setCode(location);
            locationDto.setUsableQty(qty);
            cache.locationsBySkuId
                    .computeIfAbsent(inv.getSkuId(), k -> new ArrayList<>())
                    .add(locationDto);
        }
        return cache;
    }

    private void planMoveForNeed(NeedStock need,
                                 List<NeedStock> warehouseNeedList,
                                 List<SoOutstockDetailEntity> detailList,
                                 List<InOutStockDTO> inOutStockList,
                                 List<WarehouseLocationMoveDetailDTO.AddDTO> moveDetailList,
                                 WarehouseInventoryCache inventoryCache) {
        String currentLocation = CharSequenceUtil.nullToEmpty(need.getWarehouseLocation());
        int haveQty = inventoryCache.qtyBySkuLocation.getOrDefault(
                buildSkuLocationKey(need.getSkuId(), currentLocation), 0);
        // 当前库位足够：不改位、不移仓
        if (haveQty >= need.getQty()) {
            return;
        }

        // 当前库位不足：改为空仓位扣减；移仓数量 = 出库数量 - 空仓位已有（不含当前库位存量）
        // emptyHave 含本单前面已规划移入空仓位的数量（见下方 cache 回写）
        int emptyHaveQty = inventoryCache.qtyBySkuLocation.getOrDefault(
                buildSkuLocationKey(need.getSkuId(), EMPTY_LOCATION), 0);
        int moveQty = need.getQty() - emptyHaveQty;
        if (moveQty <= 0) {
            // 空仓位已够出库，只改写扣减仓位
            rewriteToEmptyLocation(detailList, inOutStockList, need.getWarehouseId(), need.getSkuId(), currentLocation);
            log.warn("旺店通出库预检空仓位已够，仅改写库位 skuNo={} currentLocation={} need={} emptyHave={}",
                    need.getSkuNo(), currentLocation, need.getQty(), emptyHaveQty);
            return;
        }

        List<LocationListDTO> sourceLocations = inventoryCache.locationsBySkuId.get(need.getSkuId());
        if (CollUtil.isEmpty(sourceLocations)) {
            log.warn("旺店通出库预检无可用源仓位，跳过移仓 skuNo={} currentLocation={} need={} emptyHave={}",
                    need.getSkuNo(), currentLocation, need.getQty(), emptyHaveQty);
            return;
        }
        // 拷贝一份，避免本单多 SKU/多行互相扣减 usableQty 时污染缓存
        List<LocationListDTO> locationList = sourceLocations.stream().map(src -> {
            LocationListDTO copy = new LocationListDTO();
            copy.setCode(src.getCode());
            copy.setUsableQty(src.getUsableQty());
            return copy;
        }).collect(Collectors.toList());

        // 本单同仓同 SKU 各出库库位预留量（仍需从原库位扣减的数量），源仓分配时不可挪用
        Map<String, Integer> reservedOutstockMaps = buildReservedOutstockQty(warehouseNeedList, need.getSkuId());
        Map<String, Integer> plannedOutMaps = new HashMap<>();
        for (WarehouseLocationMoveDetailDTO.AddDTO planned : moveDetailList) {
            if (!Objects.equals(need.getSkuId(), planned.getSkuId())
                    && !Objects.equals(need.getSkuNo(), planned.getSkuNo())) {
                continue;
            }
            plannedOutMaps.merge(planned.getOutWarehouseLocation(), planned.getQty(), Integer::sum);
        }
        locationList.forEach(l -> {
            String code = CharSequenceUtil.nullToEmpty(l.getCode());
            int usable = l.getUsableQty() == null ? 0 : l.getUsableQty();
            Integer reserved = reservedOutstockMaps.get(code);
            if (reserved != null) {
                usable -= reserved;
            }
            Integer planned = plannedOutMaps.get(code);
            if (planned != null) {
                usable -= planned;
            }
            l.setUsableQty(usable);
        });
        // 源仓排除：空仓位、当前库位；预留后可用量<=0 的库位
        locationList.removeIf(l -> {
            String code = CharSequenceUtil.nullToEmpty(l.getCode());
            return l.getUsableQty() == null || l.getUsableQty() <= 0
                    || Objects.equals(code, EMPTY_LOCATION)
                    || Objects.equals(code, currentLocation);
        });

        Map<String, Integer> addNumMaps = new HashMap<>();
        int remain = moveQty;
        List<Predicate<? super LocationListDTO>> predicateList = buildPriorityPredicates();
        for (Predicate<? super LocationListDTO> predicate : predicateList) {
            remain = allocateMoveQty(remain, addNumMaps, locationList, predicate);
        }

        if (remain > 0 || addNumMaps.isEmpty()) {
            // 凑不满：不改库位、不移仓，保留原推荐位交给后续扣库存报不足
            log.warn("旺店通出库预检仓位可用量凑不满，跳过移仓 skuNo={} currentLocation={} need={} currentHave={} emptyHave={} moveQty={} remain={}",
                    need.getSkuNo(), currentLocation, need.getQty(), haveQty, emptyHaveQty, moveQty, remain);
            return;
        }

        // 凑满后再改库位，避免移仓未执行时明细已指向空仓位
        rewriteToEmptyLocation(detailList, inOutStockList, need.getWarehouseId(), need.getSkuId(), currentLocation);

        int movedTotal = 0;
        for (Map.Entry<String, Integer> addEntry : addNumMaps.entrySet()) {
            WarehouseLocationMoveDetailDTO.AddDTO detail = new WarehouseLocationMoveDetailDTO.AddDTO();
            detail.setSkuId(need.getSkuId());
            detail.setSkuNo(need.getSkuNo());
            detail.setQty(addEntry.getValue());
            detail.setOutWarehouseLocation(addEntry.getKey());
            detail.setInWarehouseLocation(EMPTY_LOCATION);
            detail.setOutInventoryStatus(InventoryStatusEnum.USABLE.getCode());
            detail.setInInventoryStatus(InventoryStatusEnum.USABLE.getCode());
            detail.setWarehouseId(need.getWarehouseId());
            detail.setRemark("旺店通同步销售出库单库存不足自动仓位移动");
            moveDetailList.add(detail);
            movedTotal += addEntry.getValue() == null ? 0 : addEntry.getValue();
        }
        // 回写缓存，供同仓后续 SKU/明细累计空仓位已有量
        if (movedTotal > 0) {
            inventoryCache.qtyBySkuLocation.merge(
                    buildSkuLocationKey(need.getSkuId(), EMPTY_LOCATION), movedTotal, Integer::sum);
        }
    }

    private void rewriteToEmptyLocation(List<SoOutstockDetailEntity> detailList,
                                        List<InOutStockDTO> inOutStockList,
                                        String warehouseId,
                                        String skuId,
                                        String fromLocation) {
        String from = CharSequenceUtil.nullToEmpty(fromLocation);
        if (CollUtil.isNotEmpty(detailList)) {
            for (SoOutstockDetailEntity detail : detailList) {
                if (detail == null) {
                    continue;
                }
                if (!Objects.equals(warehouseId, detail.getWarehouseId())
                        || !Objects.equals(skuId, detail.getSkuId())) {
                    continue;
                }
                if (!Objects.equals(from, CharSequenceUtil.nullToEmpty(detail.getWarehouseLocation()))) {
                    continue;
                }
                detail.setWarehouseLocation(EMPTY_LOCATION);
            }
        }
        if (CollUtil.isNotEmpty(inOutStockList)) {
            for (InOutStockDTO item : inOutStockList) {
                if (item == null) {
                    continue;
                }
                if (!Objects.equals(warehouseId, item.getWarehouseId())
                        || !Objects.equals(skuId, item.getSkuId())) {
                    continue;
                }
                if (!Objects.equals(from, CharSequenceUtil.nullToEmpty(item.getWarehouseLocation()))) {
                    continue;
                }
                item.setWarehouseLocation(EMPTY_LOCATION);
            }
        }
    }

    /**
     * 源仓位选取优先级：code 以 3 开头 → 以 4 开头 → 任意
     */
    private static List<Predicate<? super LocationListDTO>> buildPriorityPredicates() {
        List<Predicate<? super LocationListDTO>> predicateList = new ArrayList<>();
        predicateList.add(l -> CharSequenceUtil.startWith(l.getCode(), "3"));
        predicateList.add(l -> CharSequenceUtil.startWith(l.getCode(), "4"));
        predicateList.add(l -> CharSequenceUtil.startWith(CharSequenceUtil.nullToEmpty(l.getCode()), ""));
        return predicateList;
    }

    private Integer allocateMoveQty(Integer currNum, Map<String, Integer> addNumMaps,
                                    List<LocationListDTO> locationList,
                                    Predicate<? super LocationListDTO> paramPredicate) {
        if (currNum == null || currNum <= 0) {
            return currNum == null ? 0 : currNum;
        }
        List<LocationListDTO> currDtoList = locationList.stream()
                .filter(paramPredicate)
                .filter(l -> !addNumMaps.containsKey(l.getCode()))
                .sorted((l1, l2) -> Integer.compare(
                        l2.getUsableQty() == null ? 0 : l2.getUsableQty(),
                        l1.getUsableQty() == null ? 0 : l1.getUsableQty()))
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(currDtoList)) {
            return currNum;
        }
        for (LocationListDTO dto : currDtoList) {
            String code = dto.getCode();
            Integer usableQty = dto.getUsableQty() == null ? 0 : dto.getUsableQty();
            if (usableQty >= currNum) {
                addNumMaps.put(code, currNum);
                return 0;
            }
            addNumMaps.put(code, usableQty);
            currNum = currNum - usableQty;
        }
        return currNum;
    }

    /**
     * 汇总本仓本 SKU 各出库库位仍需扣减的预留数量（含当前处理行自身）。
     */
    private Map<String, Integer> buildReservedOutstockQty(List<NeedStock> warehouseNeedList, String skuId) {
        Map<String, Integer> reserved = new HashMap<>();
        if (CollUtil.isEmpty(warehouseNeedList) || CharSequenceUtil.isBlank(skuId)) {
            return reserved;
        }
        for (NeedStock item : warehouseNeedList) {
            if (item == null || !Objects.equals(skuId, item.getSkuId())
                    || item.getQty() == null || item.getQty() <= 0) {
                continue;
            }
            reserved.merge(CharSequenceUtil.nullToEmpty(item.getWarehouseLocation()), item.getQty(), Integer::sum);
        }
        return reserved;
    }

    private Map<String, NeedStock> aggregateNeed(List<InOutStockDTO> inOutStockList) {
        Map<String, NeedStock> map = new LinkedHashMap<>();
        for (InOutStockDTO item : inOutStockList) {
            if (item == null || item.getQty() == null || item.getQty() <= 0) {
                continue;
            }
            if (CharSequenceUtil.isBlank(item.getWarehouseId()) || CharSequenceUtil.isBlank(item.getSkuId())) {
                continue;
            }
            String location = CharSequenceUtil.nullToEmpty(item.getWarehouseLocation());
            String key = item.getWarehouseId() + "_" + location + "_" + item.getSkuId();
            NeedStock need = map.get(key);
            if (need == null) {
                need = new NeedStock();
                need.setWarehouseId(item.getWarehouseId());
                need.setWarehouseLocation(location);
                need.setSkuId(item.getSkuId());
                need.setSkuNo(item.getSkuNo());
                need.setSourceCode(item.getSourceCode());
                need.setQty(0);
                map.put(key, need);
            }
            need.setQty(need.getQty() + item.getQty());
        }
        return map;
    }

    private static String buildSkuLocationKey(String skuId, String location) {
        return skuId + "_" + CharSequenceUtil.nullToEmpty(location);
    }

    private static class WarehouseInventoryCache {
        /** key = skuId_location */
        private final Map<String, Integer> qtyBySkuLocation = new HashMap<>();
        /** key = skuId，value = 该 SKU 有可用库存的仓位列表 */
        private final Map<String, List<LocationListDTO>> locationsBySkuId = new HashMap<>();
    }

    @lombok.Data
    private static class NeedStock {
        private String warehouseId;
        private String warehouseLocation;
        private String skuId;
        private String skuNo;
        private String sourceCode;
        private Integer qty;
    }
}
