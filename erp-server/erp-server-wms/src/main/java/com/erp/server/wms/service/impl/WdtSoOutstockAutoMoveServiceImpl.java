package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.WarehouseLocationDTO;
import com.erp.model.wms.dto.WarehouseLocationDTO.LocationListDTO;
import com.erp.model.wms.dto.WarehouseLocationMoveDTO;
import com.erp.model.wms.dto.WarehouseLocationMoveDetailDTO;
import com.erp.model.wms.dto.inventory.InOutStockDTO;
import com.erp.model.wms.entity.InventoryEntity;
import com.erp.model.wms.entity.WarehouseEntity;
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
 * 对齐历史 Job {@code getWdtInsufficientInventory} 的仓位优先级，改为同步前结构化预检。
 */
@Slf4j
@Service
public class WdtSoOutstockAutoMoveServiceImpl implements WdtSoOutstockAutoMoveService {

    @Resource
    private WarehouseService warehouseService;
    @Resource
    private InventoryService inventoryService;
    @Resource
    private WarehouseLocationService warehouseLocationService;
    @Resource
    private WarehouseLocationMoveService warehouseLocationMoveService;

    @Override
    public void preCheckAndAutoMove(List<InOutStockDTO> inOutStockList) {
        if (CollUtil.isEmpty(inOutStockList)) {
            return;
        }
        // 按仓库 + 目标仓位 + SKU 汇总本单需求
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
            // 未启用仓位时库存都在空仓位，移仓无意义
            if (Boolean.FALSE.equals(warehouse.getIsEnableLocation())) {
                continue;
            }

            List<WarehouseLocationMoveDetailDTO.AddDTO> detailList = new ArrayList<>();
            for (NeedStock need : entry.getValue()) {
                planMoveForNeed(warehouse, need, detailList);
            }
            detailList.removeIf(d -> Objects.equals(d.getInWarehouseLocation(), d.getOutWarehouseLocation())
                    || d.getQty() == null || d.getQty() <= 0);
            if (CollUtil.isEmpty(detailList)) {
                continue;
            }

            WarehouseLocationMoveDTO.PcAddDTO addDto = new WarehouseLocationMoveDTO.PcAddDTO();
            addDto.setBillDate(LocalDate.now());
            addDto.setWarehouseId(warehouseId);
            addDto.setDetailList(detailList);
            try {
                warehouseLocationMoveService.wdtAutoAddNewTx(addDto);
                log.warn("旺店通出库预检自动移仓成功 warehouseId={} detailSize={} sourceCode={}",
                        warehouseId, detailList.size(), entry.getValue().get(0).getSourceCode());
            } catch (Exception e) {
                // 移仓失败不吞掉：直接抛出，避免带着错误库存去扣减；与原先失败进 DMP 重试一致
                log.error("旺店通出库预检自动移仓失败 warehouseId={} sourceCode={}",
                        warehouseId, entry.getValue().get(0).getSourceCode(), e);
                if (e instanceof ServiceException) {
                    throw (ServiceException) e;
                }
                throw new ServiceException("旺店通出库库存不足自动移仓失败：" + e.getMessage());
            }
        }
    }

    private void planMoveForNeed(WarehouseEntity warehouse, NeedStock need,
                                 List<WarehouseLocationMoveDetailDTO.AddDTO> detailList) {
        String orgId = warehouse.getOrgId();
        String targetLocation = CharSequenceUtil.nullToEmpty(need.getWarehouseLocation());
        InventoryEntity targetInv = inventoryService.findInventory(
                orgId, need.getWarehouseId(), need.getSkuId(), targetLocation, InventoryStatusEnum.USABLE.getCode());
        int haveQty = targetInv == null || targetInv.getQty() == null ? 0 : targetInv.getQty();
        int shortfall = need.getQty() - haveQty;
        if (shortfall <= 0) {
            return;
        }

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

        // 扣掉本单已规划占用的源仓位可用量，避免同一批明细重复占用
        Map<String, Integer> plannedOutMaps = new HashMap<>();
        for (WarehouseLocationMoveDetailDTO.AddDTO planned : detailList) {
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
        locationList.removeIf(l -> l.getUsableQty() == null || l.getUsableQty() <= 0
                // 缺口已按目标仓位现有量计算，源仓位不再包含目标自身，避免无效的同仓位自转
                || Objects.equals(CharSequenceUtil.nullToEmpty(l.getCode()), targetLocation));

        Map<String, Integer> addNumMaps = new HashMap<>();
        int remain = shortfall;
        List<Predicate<? super LocationListDTO>> predicateList = buildPriorityPredicates();
        for (Predicate<? super LocationListDTO> predicate : predicateList) {
            remain = allocateMoveQty(remain, addNumMaps, locationList, predicate);
        }

        // 与 Job 一致：只有凑满缺口才落移仓明细
        if (remain > 0 || addNumMaps.isEmpty()) {
            log.warn("旺店通出库预检仓位可用量凑不满，跳过移仓 skuNo={} targetLocation={} need={} have={} shortfall={} remain={}",
                    need.getSkuNo(), targetLocation, need.getQty(), haveQty, shortfall, remain);
            return;
        }

        for (Map.Entry<String, Integer> addEntry : addNumMaps.entrySet()) {
            WarehouseLocationMoveDetailDTO.AddDTO detail = new WarehouseLocationMoveDetailDTO.AddDTO();
            detail.setSkuId(need.getSkuId());
            detail.setSkuNo(need.getSkuNo());
            detail.setQty(addEntry.getValue());
            detail.setOutWarehouseLocation(addEntry.getKey());
            detail.setInWarehouseLocation(targetLocation);
            detail.setOutInventoryStatus(InventoryStatusEnum.USABLE.getCode());
            detail.setInInventoryStatus(InventoryStatusEnum.USABLE.getCode());
            detail.setWarehouseId(need.getWarehouseId());
            detail.setRemark("旺店通同步销售出库单库存不足自动仓位移动");
            detailList.add(detail);
        }
    }

    /**
     * 源仓位选取优先级：code 以 3 开头 → 以 4 开头 → 任意 → code 等于 2
     * （对齐 DmpInoutController#getWdtInsufficientInventory；目标仓位已在候选中排除）
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
