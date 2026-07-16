package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.WarehouseLocationDTO;
import com.erp.model.wms.dto.WarehouseLocationDTO.LocationListDTO;
import com.erp.model.wms.dto.WarehouseLocationMoveDTO;
import com.erp.model.wms.dto.WarehouseLocationMoveDetailDTO;
import com.erp.model.wms.dto.inventory.InOutStockDTO;
import com.erp.model.wms.entity.InventoryEntity;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.entity.WarehouseLocationMoveEntity;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.server.wms.service.InventoryService;
import com.erp.server.wms.service.WarehouseLocationMoveService;
import com.erp.server.wms.service.WarehouseLocationService;
import com.erp.server.wms.service.WarehouseService;
import com.erp.server.wms.service.WdtSoOutstockAutoMoveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 旺店通销售出库同步前预检：当前库位不足则改空仓位并全量移入；sourceId 幂等防重试重复移仓。
 */
@Slf4j
@Service
public class WdtSoOutstockAutoMoveServiceImpl implements WdtSoOutstockAutoMoveService {

    private static final String EMPTY_LOCATION = "";

    @Resource
    private WarehouseService warehouseService;
    @Resource
    private InventoryService inventoryService;
    @Resource
    private WarehouseLocationService warehouseLocationService;
    @Resource
    private WarehouseLocationMoveService warehouseLocationMoveService;

    @Override
    public void preCheckAndAutoMove(List<SoOutstockDetailEntity> detailList,
                                    List<InOutStockDTO> inOutStockList,
                                    String sourceId,
                                    String sourceCode) {
        if (CollUtil.isEmpty(inOutStockList)) {
            return;
        }
        Map<String, NeedStock> needMap = aggregateNeed(inOutStockList);
        if (needMap.isEmpty()) {
            return;
        }

        Map<String, List<NeedStock>> byWarehouse = needMap.values().stream()
                .collect(Collectors.groupingBy(NeedStock::getWarehouseId, LinkedHashMap::new, Collectors.toList()));

        for (Map.Entry<String, List<NeedStock>> entry : byWarehouse.entrySet()) {
            String warehouseId = entry.getKey();
            WarehouseEntity warehouse = warehouseService.getById(warehouseId);
            if (warehouse == null) {
                log.warn("旺店通出库预检移仓跳过：仓库不存在 warehouseId={}", warehouseId);
                continue;
            }
            if (Boolean.FALSE.equals(warehouse.getIsEnableLocation())) {
                continue;
            }

            boolean alreadyMoved = CharSequenceUtil.isNotBlank(sourceId) && existsMovedBySource(sourceId, warehouseId);

            List<WarehouseLocationMoveDetailDTO.AddDTO> moveDetailList = new ArrayList<>();
            for (NeedStock need : entry.getValue()) {
                planMoveForNeed(warehouse, need, detailList, inOutStockList, moveDetailList, alreadyMoved);
            }
            moveDetailList.removeIf(d -> Objects.equals(d.getInWarehouseLocation(), d.getOutWarehouseLocation())
                    || d.getQty() == null || d.getQty() <= 0);
            if (CollUtil.isEmpty(moveDetailList) || alreadyMoved) {
                if (alreadyMoved) {
                    log.warn("旺店通出库预检已存在移仓单，跳过移仓 sourceId={} warehouseId={}", sourceId, warehouseId);
                }
                continue;
            }

            WarehouseLocationMoveDTO.PcAddDTO addDto = new WarehouseLocationMoveDTO.PcAddDTO();
            addDto.setBillDate(LocalDate.now());
            addDto.setWarehouseId(warehouseId);
            addDto.setDetailList(moveDetailList);
            addDto.setSourceId(sourceId);
            addDto.setSourceCode(CharSequenceUtil.blankToDefault(sourceCode, sourceId));
            addDto.setSourceType(SourceTypeEnum.SO_OUTSTOCK.getCode());
            try {
                warehouseLocationMoveService.wdtAutoAddNewTx(addDto);
                log.warn("旺店通出库预检自动移仓成功 warehouseId={} detailSize={} sourceId={}",
                        warehouseId, moveDetailList.size(), sourceId);
            } catch (Exception e) {
                log.error("旺店通出库预检自动移仓失败 warehouseId={} sourceId={}", warehouseId, sourceId, e);
                if (e instanceof ServiceException) {
                    throw (ServiceException) e;
                }
                throw new ServiceException("旺店通出库库存不足自动移仓失败：" + e.getMessage());
            }
        }
    }

    private boolean existsMovedBySource(String sourceId, String warehouseId) {
        return warehouseLocationMoveService.lambdaQuery()
                .eq(WarehouseLocationMoveEntity::getSourceType, SourceTypeEnum.SO_OUTSTOCK.getCode())
                .eq(WarehouseLocationMoveEntity::getSourceId, sourceId)
                .eq(WarehouseLocationMoveEntity::getWarehouseId, warehouseId)
                .and(w -> w.isNull(WarehouseLocationMoveEntity::getInvalidStatus)
                        .or()
                        .eq(WarehouseLocationMoveEntity::getInvalidStatus, false))
                .last("limit 1")
                .one() != null;
    }

    private void planMoveForNeed(WarehouseEntity warehouse, NeedStock need,
                                 List<SoOutstockDetailEntity> detailList,
                                 List<InOutStockDTO> inOutStockList,
                                 List<WarehouseLocationMoveDetailDTO.AddDTO> moveDetailList,
                                 boolean alreadyMoved) {
        String orgId = warehouse.getOrgId();
        String currentLocation = CharSequenceUtil.nullToEmpty(need.getWarehouseLocation());
        InventoryEntity currentInv = inventoryService.findInventory(
                orgId, need.getWarehouseId(), need.getSkuId(), currentLocation, InventoryStatusEnum.USABLE.getCode());
        int haveQty = currentInv == null || currentInv.getQty() == null ? 0 : currentInv.getQty();
        if (haveQty >= need.getQty()) {
            return;
        }

        // 重试场景：移仓已完成，仅改写出库库位为空仓位，不再移仓
        if (alreadyMoved) {
            rewriteToEmptyLocation(detailList, inOutStockList, need.getWarehouseId(), need.getSkuId(), currentLocation);
            return;
        }

        // 全量移入空仓位，不再用空仓位现有量计算缺口
        int moveQty = need.getQty();
        PagingDTO<WarehouseLocationDTO.SelectDTO> searchDTO = new PagingDTO<>();
        searchDTO.setPageSize(-1);
        searchDTO.setCurrPage(1);
        WarehouseLocationDTO.SelectDTO params = new WarehouseLocationDTO.SelectDTO();
        params.setWarehouseId(need.getWarehouseId());
        params.setFilterZero(true);
        params.setSkuNo(need.getSkuNo());
        searchDTO.setParams(params);

        PagingVO<LocationListDTO> pagingVO = warehouseLocationService.pagingSelect(searchDTO);
        List<LocationListDTO> locationList = pagingVO == null ? null : pagingVO.getList();
        if (CollUtil.isEmpty(locationList)) {
            return;
        }

        Map<String, Integer> plannedOutMaps = new HashMap<>();
        for (WarehouseLocationMoveDetailDTO.AddDTO planned : moveDetailList) {
            if (!need.getSkuNo().equals(planned.getSkuNo())) {
                continue;
            }
            plannedOutMaps.merge(planned.getOutWarehouseLocation(), planned.getQty(), Integer::sum);
        }
        locationList.forEach(l -> {
            Integer planned = plannedOutMaps.get(l.getCode());
            if (planned != null) {
                l.setUsableQty((l.getUsableQty() == null ? 0 : l.getUsableQty()) - planned);
            }
        });
        // 源仓排除空仓位自身，避免同仓位自转
        locationList.removeIf(l -> l.getUsableQty() == null || l.getUsableQty() <= 0
                || Objects.equals(CharSequenceUtil.nullToEmpty(l.getCode()), EMPTY_LOCATION));

        Map<String, Integer> addNumMaps = new HashMap<>();
        int remain = moveQty;
        List<Predicate<? super LocationListDTO>> predicateList = buildPriorityPredicates();
        for (Predicate<? super LocationListDTO> predicate : predicateList) {
            remain = allocateMoveQty(remain, addNumMaps, locationList, predicate);
        }

        if (remain > 0 || addNumMaps.isEmpty()) {
            // 凑不满：不改库位、不移仓，保留原推荐位交给后续扣库存报不足
            log.warn("旺店通出库预检仓位可用量凑不满，跳过移仓 skuNo={} currentLocation={} need={} have={} remain={}",
                    need.getSkuNo(), currentLocation, need.getQty(), haveQty, remain);
            return;
        }

        // 凑满后再改库位，避免移仓未执行时明细已指向空仓位
        rewriteToEmptyLocation(detailList, inOutStockList, need.getWarehouseId(), need.getSkuId(), currentLocation);

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
     * 源仓位选取优先级：code 以 3 开头 → 以 4 开头 → 任意 → code 等于 2
     */
    private static List<Predicate<? super LocationListDTO>> buildPriorityPredicates() {
        List<Predicate<? super LocationListDTO>> predicateList = new ArrayList<>();
        predicateList.add(l -> CharSequenceUtil.startWith(l.getCode(), "3"));
        predicateList.add(l -> CharSequenceUtil.startWith(l.getCode(), "4"));
        predicateList.add(l -> CharSequenceUtil.startWith(CharSequenceUtil.nullToEmpty(l.getCode()), ""));
        predicateList.add(l -> Objects.equals(l.getCode(), "2"));
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
