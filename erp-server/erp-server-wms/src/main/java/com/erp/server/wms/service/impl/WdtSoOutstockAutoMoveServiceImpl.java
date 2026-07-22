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
 * 旺店通销售出库同步前，按出库配置推荐仓位并完成必要的自动移仓。
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

            List<CfgRulePickingDTO.OutStockItemDTO> items = needResolveDetails.stream()
                    .map(detail -> new CfgRulePickingDTO.OutStockItemDTO(
                            detail.getId(), detail.getSkuId(), detail.getSkuNo(), detail.getActualQty()))
                    .collect(Collectors.toList());
            List<CfgRulePickingDTO.OutStockLocationSuggestDTO> suggests =
                    cfgRulePickingService.resolveOutStockLocations(executionData, warehouseId, items);
            applySuggests(warehouseId, needResolveDetails, inOutStockList, suggests, sourceId, sourceCode);
        }
    }

    /**
     * 若 sourceId + 仓库已存在有效移仓单，按移仓明细回写出库仓位（通常为空仓位），返回仍需重新推荐的明细。
     * <p>明细 ID 由旺店通单号+业务键稳定生成，优先按 sourceDetailId 精确匹配；其次仅在 skuId+qty 唯一时匹配。</p>
     */
    private List<SoOutstockDetailEntity> restoreMovedLocationsIfPresent(String sourceId,
                                                                        String warehouseId,
                                                                        List<SoOutstockDetailEntity> warehouseDetails,
                                                                        List<InOutStockDTO> inOutStockList) {
        WarehouseLocationMoveEntity moved = findValidMovedBySource(sourceId, warehouseId);
        if (moved == null) {
            return warehouseDetails;
        }
        List<WarehouseLocationMoveDetailEntity> moveDetails =
                warehouseLocationMoveDetailService.listByMainIds(Collections.singletonList(moved.getId()));
        if (CollUtil.isEmpty(moveDetails)) {
            throw new ServiceException(ApiError.WH_OUT_STOCK_MOVE_FAILED, "",
                    "已存在移仓单但无明细，请人工处理");
        }
        List<WarehouseLocationMoveDetailEntity> unmatched = new ArrayList<>(moveDetails);
        List<SoOutstockDetailEntity> needResolve = new ArrayList<>();
        for (SoOutstockDetailEntity detail : warehouseDetails) {
            WarehouseLocationMoveDetailEntity matched = pollMatchedMoveDetail(unmatched, detail);
            if (matched == null) {
                needResolve.add(detail);
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

    private WarehouseLocationMoveEntity findValidMovedBySource(String sourceId, String warehouseId) {
        if (CharSequenceUtil.isBlank(sourceId) || CharSequenceUtil.isBlank(warehouseId)) {
            return null;
        }
        return warehouseLocationMoveService.lambdaQuery()
                .eq(WarehouseLocationMoveEntity::getSourceType, SourceTypeEnum.SO_OUTSTOCK.getCode())
                .eq(WarehouseLocationMoveEntity::getSourceId, sourceId)
                .eq(WarehouseLocationMoveEntity::getWarehouseId, warehouseId)
                .eq(WarehouseLocationMoveEntity::getApproveStatus, ApproveStatusEnum.APPROVE)
                .and(w -> w.isNull(WarehouseLocationMoveEntity::getInvalidStatus)
                        .or()
                        .eq(WarehouseLocationMoveEntity::getInvalidStatus, false))
                .orderByDesc(WarehouseLocationMoveEntity::getCreateTime)
                .last("limit 1")
                .one();
    }

    private CfgRulePickingDTO.CfgExecutionDataDTO buildExecutionData(
            SoOutstockEntity soOutstock, List<SoOutstockDetailEntity> details) {
        CfgRulePickingDTO.CfgExecutionDataDTO executionData =
                new CfgRulePickingDTO.CfgExecutionDataDTO();
        executionData.setBillType(PickingBillTypeEnum.B2C.getCode());
        executionData.setCustomerId(soOutstock.getCustomerId());
        executionData.setCountryCode(soOutstock.getCountry());
        executionData.setDeliveryWarehouseId(soOutstock.getWarehouseId());
        executionData.setSourceCode(soOutstock.getCode());
        executionData.setDetails(details.stream()
                .map(detail -> new CfgRulePickingDTO.CfgExecutionDataDetailDTO(
                        CharSequenceUtil.isNotBlank(detail.getWarehouseId())
                                ? detail.getWarehouseId() : soOutstock.getWarehouseId(),
                        detail.getSkuId(), detail.getSkuNo(), "",
                        detail.getActualQty(), detail.getId()))
                .collect(Collectors.toList()));
        return executionData;
    }

    private void rewriteLocation(List<SoOutstockDetailEntity> details,
                                 List<InOutStockDTO> inOutStockList,
                                 String warehouseLocation) {
        Set<String> detailIds = details.stream()
                .map(SoOutstockDetailEntity::getId)
                .collect(Collectors.toSet());
        details.forEach(detail -> detail.setWarehouseLocation(warehouseLocation));
        inOutStockList.stream()
                .filter(item -> detailIds.contains(item.getSourceDetailId()))
                .forEach(item -> item.setWarehouseLocation(warehouseLocation));
    }
}
