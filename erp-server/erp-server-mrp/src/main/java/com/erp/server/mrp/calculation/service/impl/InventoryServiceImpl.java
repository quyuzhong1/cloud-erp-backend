package com.erp.server.mrp.calculation.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.mrp.dto.*;
import com.erp.model.mrp.enums.*;
import com.erp.model.scm.dto.PurchaseApplicationRefPoDTO;
import com.erp.model.scm.entity.SubcontractOrderDetailEntity;
import com.erp.model.scm.enums.CreatePoTypeEnum;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.entity.FbaInventoryEntity;
import com.erp.model.wms.entity.InventoryEntity;
import com.erp.model.wms.entity.OverseasInventoryEntity;
import com.erp.model.wms.entity.VirtualInventoryEntity;
import com.erp.model.wms.enums.DeliveryPlanTypeEnum;
import com.erp.model.wms.enums.VitualWarehouseChannelTypeEnum;
import com.erp.server.mrp.calculation.service.InventoryService;
import com.erp.server.mrp.calculation.service.ShopInfoService;
import com.erp.server.mrp.mapper.InventoryMapper;
import com.erp.server.mrp.service.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.erp.model.mrp.enums.SnapshotTableEnum.*;

@Service
public class InventoryServiceImpl implements InventoryService {
    @Resource
    private InventoryMapper inventoryMapper;
    @Resource
    private ShopInfoService shopInfoService;

    @Resource
    private ReplenishmentSuggestionService replenishmentSuggestionService;

    @Resource
    private FbaHistoryInventoryService fbaHistoryInventoryService;
    @Resource
    private OverseasHistoryInventoryService overseasHistoryInventoryService;
    @Resource
    private LocalHistoryInventoryService localHistoryInventoryService;
    @Resource
    private VirtualInventoryHistoryService virtualInventoryHistoryService;
    @Resource
    private CfgRuleCommonService cfgRuleCommonService;

    @Override
    public int getFbaUsable(ReplenishmentResultDTO replenishmentResultDTO, Set<String> codes) {
        String code = String.join("+", codes);
        String calcDate = replenishmentResultDTO.getReplenishmentDetail().getCalcDate();
        return inventoryMapper.getFbaUsable(replenishmentResultDTO, code, SnapshotTableEnum.getTableName(SnapshotTableEnum.FBA_INVENTORY, calcDate));
    }

    @Override
    public int getFbaInTransit(ReplenishmentResultDTO replenishmentResultDTO, String code, CfgRuleStockUpDTO.StrategyResultDTO stockUpResult) {
        String calcDate = replenishmentResultDTO.getReplenishmentDetail().getCalcDate();
        List<ReplenishmentResultDTO.FbaInTransitDetailDTO> inTransitDetails = new ArrayList<>();
        //1、若已生成头程物流单，货件--发货单--头程物流单：
        //已生成头程物流单，且已下单，则预计到货日期 = 头程物流单的下单时间 + 头程物流单上的预计时效
        //已生成头程物流单，但未下单，则预计到货时间 = 发货单的发货时间 + 本地发FBA时效 + FBA入库时间
        //2、若未生成头程物流单，已生成发货单，则预计到货时间 = 发货单的发货时间 + 本地发FBA时效 + FBA入库时间
        if (CfgRuleInventoryNodeEnum.FBA_DELIVERY.getCode().equals(code)) {
            //发FBA，签收数量取对应货件的签收数量 在途数量 = 发货单上的实发数量 - 签收数量；
            inTransitDetails = inventoryMapper.getFbaDelivery(replenishmentResultDTO,
                    SnapshotTableEnum.getTableName(FIRST_MILE_DELIVERY, calcDate),
                    SnapshotTableEnum.getTableName(FIRST_MILE_DELIVERY_DETAIL, calcDate),
                    SnapshotTableEnum.getTableName(FBA_SHIPMENT, calcDate),
                    SnapshotTableEnum.getTableName(FBA_SHIPMENT_DETAIL, calcDate)

            );
            if (CollectionUtils.isEmpty(inTransitDetails)) {
                return 0;
            }
            List<String> firstMileDeliveryIds = inTransitDetails.stream().map(ReplenishmentResultDTO.FbaInTransitDetailDTO::getSourceId).collect(Collectors.toList());
            //查询头程物流单
            List<LogisticsBillEntity> logisticsBills = inventoryMapper.getLogisticsBillBySourceIds(firstMileDeliveryIds, SnapshotTableEnum.getTableName(LOGISTICS_BILL, calcDate));
            for (ReplenishmentResultDTO.FbaInTransitDetailDTO detail : inTransitDetails) {
                LogisticsBillEntity logisticsBill = logisticsBills.stream().filter(v -> v.getSourceId().equals(detail.getSourceId()))
                        .filter(v -> !ObjectUtils.isEmpty(v.getOrderTime()))
                        .findFirst()
                        .orElse(null);
                if (ObjectUtils.isEmpty(logisticsBill)) {
                    detail.setEstimateSalesDate(detail.getDeliveryDate().plusDays(stockUpResult.getLogisticsResult().getLogisticsDays()).plusDays(stockUpResult.getInstockDays()));
                } else {
                    detail.setEstimateSalesDate(logisticsBill.getOrderTime().toLocalDate().plusDays(stockUpResult.getLogisticsResult().getLogisticsDays()).plusDays(stockUpResult.getInstockDays()));
                }
            }
        } else if (CfgRuleInventoryNodeEnum.FBA_SHIPMENT.getCode().equals(code)) {
            //FBA在途 =发货数量 - 签收数量
            inTransitDetails = inventoryMapper.getFbaShipment(replenishmentResultDTO,
                    SnapshotTableEnum.getTableName(FBA_SHIPMENT, calcDate),
                    SnapshotTableEnum.getTableName(FBA_SHIPMENT_DETAIL, calcDate)
            );
            if (CollectionUtils.isEmpty(inTransitDetails)) {
                return 0;
            }
            List<String> codes = inTransitDetails.stream().map(ReplenishmentResultDTO.FbaInTransitDetailDTO::getSourceCode).collect(Collectors.toList());
            List<FirstMileDeliveryDTO.FbaShipmentDTO> firstMileDeliveryList = inventoryMapper.listFirstMileDelivery(codes, SnapshotTableEnum.getTableName(FIRST_MILE_DELIVERY, calcDate),
                    SnapshotTableEnum.getTableName(FIRST_MILE_DELIVERY_DETAIL, calcDate));
            List<String> firstMileDeliveryIds = firstMileDeliveryList.stream().map(FirstMileDeliveryDTO.FbaShipmentDTO::getId).collect(Collectors.toList());
            List<LogisticsBillEntity> logisticsBills = inventoryMapper.getLogisticsBillBySourceIds(firstMileDeliveryIds, SnapshotTableEnum.getTableName(LOGISTICS_BILL, calcDate));
            for (ReplenishmentResultDTO.FbaInTransitDetailDTO detail : inTransitDetails) {
                FirstMileDeliveryDTO.FbaShipmentDTO fbaShipmentDTO = firstMileDeliveryList.stream()
                        .filter(v -> v.getFbaShipmentCode().equals(detail.getSourceCode()))
                        .findFirst()
                        .orElse(new FirstMileDeliveryDTO.FbaShipmentDTO());
                LogisticsBillEntity logisticsBill = logisticsBills.stream().filter(v -> v.getSourceId().equals(fbaShipmentDTO.getId()))
                        .filter(v -> !ObjectUtils.isEmpty(v.getOrderTime()))
                        .findFirst()
                        .orElse(null);
                if (ObjectUtils.isEmpty(logisticsBill)) {
                    detail.setEstimateSalesDate(fbaShipmentDTO.getApproveTime().toLocalDate().plusDays(stockUpResult.getLogisticsResult().getLogisticsDays()).plusDays(stockUpResult.getInstockDays()));
                } else {
                    detail.setEstimateSalesDate(logisticsBill.getOrderTime().toLocalDate().plusDays(stockUpResult.getLogisticsResult().getLogisticsDays()).plusDays(stockUpResult.getInstockDays()));
                }
            }

        }
        replenishmentResultDTO.setFbaInTransitDetails(inTransitDetails);
        return inTransitDetails.stream().map(ReplenishmentResultDTO.FbaInTransitDetailDTO::getInTransitQty)
                .reduce(0, Math::addExact);
    }

    @Override
    public List<ReplenishmentResultDTO.EstimatedDeliveryDetailDTO> getFbaPlanDelivery(ReplenishmentResultDTO replenishmentResultDTO, Set<String> strategyCodes, CfgRuleStockUpDTO.StrategyResultDTO stockUpResult) {
        String calcDate = replenishmentResultDTO.getReplenishmentDetail().getCalcDate();
        List<ReplenishmentResultDTO.EstimatedDeliveryDetailDTO> estimatedDeliveryDetails = inventoryMapper.getPlanDelivery(DeliveryPlanTypeEnum.FBA.getCode(), strategyCodes, replenishmentResultDTO, SnapshotTableEnum.getTableName(WMS_DELIVERY_PLAN, calcDate), SnapshotTableEnum.getTableName(WMS_DELIVERY_PLAN_DETAIL, calcDate));
        for (ReplenishmentResultDTO.EstimatedDeliveryDetailDTO detail : estimatedDeliveryDetails) {
            detail.setType(ReplenishmentInventoryTypeEnum.FBA_ESTIMATED_DELIVERY.getCode());
            detail.setEstimateSalesDate(detail.getEstimateSalesDate().plusDays(stockUpResult.getInstockDays()).plusDays(stockUpResult.getLogisticsResult().getLogisticsDays()).plusDays(stockUpResult.getLogisticsResult().getLogisticsCycleDays()));
            detail.setSourceType(SourceTypeEnum.DELIVERY_PLAN.getCode());
        }
        return estimatedDeliveryDetails;
    }

    @Override
    public int getOverseasUsable(ReplenishmentResultDTO replenishmentResultDTO, Set<String> codes, CfgRuleStrategyDTO cfgRuleStrategyDTO) {
        String code = String.join("+", codes);
        String calcDate = replenishmentResultDTO.getReplenishmentDetail().getCalcDate();
        int overseasUsable = inventoryMapper.getOverseasUsable(replenishmentResultDTO, code, getTableName(SnapshotTableEnum.OVERSEAS_INVENTORY, calcDate));
        replenishmentSuggestionService.listSalesBySkuId(replenishmentResultDTO.getReplenishment().getSkuId());
        CfgRuleWarehouseDTO.StrategyResultDTO warehouseResult = cfgRuleStrategyDTO.getWarehouseResult();
        List<CfgRuleWarehouseDTO.StrategyDetailResultDTO> allPlatformWarehouse = warehouseResult.getOverseasWarehouseList().
                stream().filter(v -> VitualWarehouseChannelTypeEnum.PLATFORM.getCode().equals(v.getChannelType()))
                .collect(Collectors.toList());

        List<CfgRuleWarehouseDTO.StrategyDetailResultDTO> warehouse = warehouseResult.getOverseasWarehouseList().
                stream().filter(v -> v.getChannelIdJson().contains(replenishmentResultDTO.getReplenishment().getShopId()))
                .collect(Collectors.toList());

        return 0;
    }

    @Override
    public int getLocalUsable(ReplenishmentResultDTO replenishmentResultDTO, Set<String> codes, CfgRuleStrategyDTO cfgRuleStrategyDTO) {
        int qty = 0;
        CfgRuleWarehouseDTO.StrategyResultDTO warehouseResult = cfgRuleStrategyDTO.getWarehouseResult();
        String calcDate = replenishmentResultDTO.getReplenishmentDetail().getCalcDate();
        List<ReplenishmentResultDTO.ReplenishmentInventoryDetailDTO> localUsableDetail = new ArrayList<>();
        if (Boolean.TRUE.equals(warehouseResult.getIsEnableVirtual())) {
            List<LocalInventoryDTO> invetoryList = inventoryMapper.getVirtualUsable(replenishmentResultDTO.getReplenishment().getSkuId(), codes, getTableName(VIRTUAL_INVENTORY, calcDate));
            for (LocalInventoryDTO dto : invetoryList) {
                for (CfgRuleWarehouseDTO.StrategyDetailResultDTO result : warehouseResult.getLocalWarehouseList()) {
                    if (!result.getVirtualWarehouseId().equals(dto.getWarehouseId()) || (VitualWarehouseChannelTypeEnum.SHOP.getCode().equals(result.getChannelType())
                            && !result.getChannelIdJson().contains(replenishmentResultDTO.getReplenishment().getShopId()))) {
                        continue;
                    }
                    //根据库存分配配置
                    qty = getInventoryQty(replenishmentResultDTO, qty, localUsableDetail, dto, result, ReplenishmentInventoryTypeEnum.LOCAL_USABLE);
                }
            }
        } else {
            List<LocalInventoryDTO> invetoryList = inventoryMapper.getLocalUsable(replenishmentResultDTO.getReplenishment().getSkuId(), codes, getTableName(INVENTORY, calcDate));
            for (LocalInventoryDTO dto : invetoryList) {
                for (CfgRuleWarehouseDTO.StrategyDetailResultDTO result : warehouseResult.getLocalWarehouseList()) {
                    if (!result.getWarehouseId().equals(dto.getWarehouseId()) || (VitualWarehouseChannelTypeEnum.SHOP.getCode().equals(result.getChannelType())
                            && !result.getChannelIdJson().contains(replenishmentResultDTO.getReplenishment().getShopId()))) {
                        continue;
                    }
                    //根据库存分配配置
                    qty = getInventoryQty(replenishmentResultDTO, qty, localUsableDetail, dto, result, ReplenishmentInventoryTypeEnum.LOCAL_USABLE);
                }
            }
        }
        replenishmentResultDTO.setLocalUsableDetail(localUsableDetail);
        return qty;
    }

    @Override
    public int getLocalInTransit(ReplenishmentResultDTO replenishmentResultDTO, Set<String> codes, CfgRuleStrategyDTO cfgRuleStrategyDTO) {
        int qty = 0;
        List<ReplenishmentResultDTO.ReplenishmentInventoryDetailDTO> localInTransitDetail = new ArrayList<>();
        CfgRuleWarehouseDTO.StrategyResultDTO warehouseResult = cfgRuleStrategyDTO.getWarehouseResult();
        List<LocalInventoryDTO> invetoryList = getLocalInTransitInventory(replenishmentResultDTO, codes, cfgRuleStrategyDTO.getStockUpResult());
        for (LocalInventoryDTO dto : invetoryList) {
            for (CfgRuleWarehouseDTO.StrategyDetailResultDTO result : warehouseResult.getLocalWarehouseList()) {
                if (!result.getWarehouseId().equals(dto.getWarehouseId()) || (VitualWarehouseChannelTypeEnum.SHOP.getCode().equals(result.getChannelType())
                        && !result.getChannelIdJson().contains(replenishmentResultDTO.getReplenishment().getShopId()))) {
                    continue;
                }
                //根据库存分配配置
                qty = getInventoryQty(replenishmentResultDTO, qty, localInTransitDetail, dto, result, ReplenishmentInventoryTypeEnum.LOCAL_IN_TRANSIT);
            }
        }
        replenishmentResultDTO.setLocalInTransitDetail(localInTransitDetail);
        return qty;
    }

    private List<LocalInventoryDTO> getLocalInTransitInventory(ReplenishmentResultDTO replenishmentResultDTO, Set<String> codes, CfgRuleStockUpDTO.StrategyResultDTO stockUpResult) {
        String calcDate = replenishmentResultDTO.getReplenishmentDetail().getCalcDate();
        String platformType = replenishmentResultDTO.getReplenishment().getPlatformType();
        boolean isPurchase = codes.contains(CfgRuleInventoryNodeEnum.LOCAL_IN_TRANSIT_PURCHASE.getCode());
        boolean isTransfer = codes.contains(CfgRuleInventoryNodeEnum.LOCAL_IN_TRANSIT_TRANSFER.getCode());
        List<String> localWarehouseIds = replenishmentResultDTO.getLocalWarehouseId();
        if (CollectionUtils.isEmpty(localWarehouseIds)) {
            return Collections.emptyList();
        }
        List<ReplenishmentResultDTO.LocalInTransitDetailDTO> localInTransitDetails = inventoryMapper.getLocalInTransitDetail(isPurchase, isTransfer, replenishmentResultDTO.getReplenishment().getSkuId(), localWarehouseIds,getTableName(TRANSACTION_FLOW, calcDate),
                getTableName(INSTOCK_FORCAST, calcDate),getTableName(PO_RECEIVE, calcDate),getTableName(PO_INSTOCK, calcDate),getTableName(PO_RETURN, calcDate),getTableName(TRANSFER_OUT, calcDate) ,getTableName(TRANSFER_IN, calcDate));
        for (ReplenishmentResultDTO.LocalInTransitDetailDTO dto : localInTransitDetails) {
                //预计入库日期 = 采购订单的审核日期 + 生产周期 + 供应商发货时长 + 质检入库时长
                dto.setEstimatedPutAwayDate(dto.getEstimatedPutAwayDate().plusDays(stockUpResult.getPurchaseApproveDays()).plusDays(stockUpResult.getProductionDays()).plusDays(stockUpResult.getSupplierDeliveryDays()).plusDays(stockUpResult.getQcDays()));
                if (CfgRulePlatformTypeEnum.AMAZON.getCode().equals(platformType) || CfgRulePlatformTypeEnum.OVERSEAS.getCode().equals(platformType)) {
                    //预计到货日期（Amazon） = 预计入库日期 + 本地发FBA时效 + FBA入库时间
                    //预计到货日期（海外） = 预计入库日期 + 本地发海外时效 + 海外仓入库时间
                    dto.setEstimateSalesDate(dto.getEstimatedPutAwayDate().plusDays(stockUpResult.getLogisticsResult().getLogisticsDays()).plusDays(stockUpResult.getInstockDays()));
                }else if (CfgRulePlatformTypeEnum.B2B.getCode().equals(platformType) || CfgRulePlatformTypeEnum.INTERNAL.getCode().equals(platformType)) {
                    //预计到货日期（本地）= 预计入库日期
                    dto.setEstimateSalesDate(dto.getEstimatedPutAwayDate());
                }
        }
        replenishmentResultDTO.setLocalInTransitDetails(localInTransitDetails);
        return new ArrayList<>(localInTransitDetails.parallelStream()
                .map(v -> new LocalInventoryDTO(v.getWarehouseId(), v.getQty()))
                .collect(Collectors.toMap(
                        LocalInventoryDTO::getWarehouseId,
                        v -> new LocalInventoryDTO(v.getWarehouseId(), v.getQty()),
                        (existing, replacement) -> {
                            // 合并 qty
                            existing.setQty(existing.getQty() + replacement.getQty());
                            return existing;
                        }
                )).values());
    }

    @Override
    public int getLocalPurchase(ReplenishmentResultDTO replenishmentResultDTO, CfgRuleStrategyDTO cfgRuleStrategyDTO) {
        int qty = 0;
        List<ReplenishmentResultDTO.ReplenishmentInventoryDetailDTO> localPurchaseDetail = new ArrayList<>();
        CfgRuleWarehouseDTO.StrategyResultDTO warehouseResult = cfgRuleStrategyDTO.getWarehouseResult();
        List<LocalInventoryDTO> invetoryList = getEstimatedPurchaseInventory(replenishmentResultDTO, cfgRuleStrategyDTO);
        for (LocalInventoryDTO dto : invetoryList) {
            for (CfgRuleWarehouseDTO.StrategyDetailResultDTO result : warehouseResult.getLocalWarehouseList()) {
                if (!result.getWarehouseId().equals(dto.getWarehouseId()) || (VitualWarehouseChannelTypeEnum.SHOP.getCode().equals(result.getChannelType())
                        && !result.getChannelIdJson().contains(replenishmentResultDTO.getReplenishment().getShopId()))) {
                    continue;
                }
                //根据库存分配配置
                qty = getInventoryQty(replenishmentResultDTO, qty, localPurchaseDetail, dto, result, ReplenishmentInventoryTypeEnum.LOCAL_ESTIMATED_DELIVERY);
            }
        }
        replenishmentResultDTO.setLocalPurchaseDetail(localPurchaseDetail);
        return qty;
    }

    @Override
    public void saveAllHistoryInventory(LocalDate calculationDate, String calcDate) {
        //清洗每日库存到历史表
        List<FbaInventoryEntity> inventoryEntities = inventoryMapper.getAllFbaHistoryInventory(SnapshotTableEnum.getTableName(SnapshotTableEnum.FBA_INVENTORY, calcDate));
        if (!CollectionUtils.isEmpty(inventoryEntities)) {
            fbaHistoryInventoryService.saveTodayInventory(inventoryEntities, calculationDate);
        }
        List<OverseasInventoryEntity> overseasHistoryInventory = inventoryMapper.getAllOverseasHistoryInventory(SnapshotTableEnum.getTableName(SnapshotTableEnum.OVERSEAS_INVENTORY, calcDate));
        if (!CollectionUtils.isEmpty(inventoryEntities)) {
            overseasHistoryInventoryService.saveTodayInventory(overseasHistoryInventory, calculationDate);
        }
        List<InventoryEntity> localHistoryInventory = inventoryMapper.getAllLocalHistoryInventory(SnapshotTableEnum.getTableName(SnapshotTableEnum.INVENTORY, calcDate));
        if (!CollectionUtils.isEmpty(inventoryEntities)) {
            localHistoryInventoryService.saveTodayInventory(localHistoryInventory, calculationDate);
        }
        List<VirtualInventoryEntity> virtualInventory = inventoryMapper.getAllVirtualHistoryInventory(SnapshotTableEnum.getTableName(SnapshotTableEnum.VIRTUAL_INVENTORY, calcDate));
        if (!CollectionUtils.isEmpty(inventoryEntities)) {
            virtualInventoryHistoryService.saveTodayInventory(virtualInventory, calculationDate);
        }
    }

    @Override
    public void checkAllTableExists(String calcDate) {
        List<String> tableList = new ArrayList<>();
        for (SnapshotTableEnum value : SnapshotTableEnum.values()) {
            boolean exist = inventoryMapper.isTableExist(getTableName(value, calcDate));
            if (Boolean.FALSE.equals(exist)) {
                tableList.add(value.getCode());
            }
        }
        if (!CollectionUtils.isEmpty(tableList)) {
            throw new ServiceException(ApiError.ERROR_TABLE_NOT_EXIST, String.join(",", tableList));
        }
    }

    /**
     * 获取预计采购库存
     * @param replenishmentResultDTO 补货建议
     */
    private List<LocalInventoryDTO> getEstimatedPurchaseInventory(ReplenishmentResultDTO replenishmentResultDTO, CfgRuleStrategyDTO cfgRuleStrategyDTO) {
        List<ReplenishmentResultDTO.EstimatedPurchaseDetailDTO> detailList = new ArrayList<>();
        String calcDate = replenishmentResultDTO.getReplenishmentDetail().getCalcDate();
        List<CfgRuleCommonDTO.StrategyResultDTO> inventoryResult = cfgRuleStrategyDTO.getInventoryResult();
        String baseKey = "MRP:" + CfgRulePlatformTypeEnum.AMAZON.getCode() + ":" + CfgRuleCommonTypeEnum.INVENTORY.getCode();
        CfgRuleStockUpDTO.StrategyResultDTO stockUpResult = cfgRuleStrategyDTO.getStockUpResult();
        List<String> localWarehouseIds = replenishmentResultDTO.getLocalWarehouseId();
        if (CollectionUtils.isEmpty(localWarehouseIds)) {
            return Collections.emptyList();
        }
        Set<String> localReplenishmentPlan = cfgRuleCommonService.findByKey(baseKey, inventoryResult, baseKey + ":" + CfgRuleInventoryNodeEnum.getLocalReplenishmentPlan());
        if (!CollectionUtils.isEmpty(localReplenishmentPlan)) {
            // todo 本地补货计划
        }

        Set<String> localPurchasePlan = cfgRuleCommonService.findByKey(baseKey, inventoryResult, baseKey + ":" + CfgRuleInventoryNodeEnum.getLocalPurchasePlan());
        if (!CollectionUtils.isEmpty(localPurchasePlan)) {
            List<ReplenishmentResultDTO.EstimatedPurchaseDetailDTO> applications = inventoryMapper.listPurchasePlan(localPurchasePlan, replenishmentResultDTO.getReplenishment().getSkuId(), localWarehouseIds, getTableName(PURCHASE_APPLICATION, calcDate), getTableName(PURCHASE_APPLICATION_DETAIL, calcDate));
            //查询关联采购
            List<String> detailIds = applications.stream().map(ReplenishmentResultDTO.EstimatedPurchaseDetailDTO::getDetailId).collect(Collectors.toList());
            List<PurchaseApplicationRefPoDTO.ListDTO> refList = null;
            List<SubcontractOrderDetailEntity> subcontractOrderDetailList = null;
            if (!CollectionUtils.isEmpty(detailIds)) {
                refList = inventoryMapper.listPurchaseApplicationRefPo(detailIds, getTableName(PURCHASE_ORDER, calcDate), getTableName(PURCHASE_ORDER_DETAIL, calcDate), getTableName(PURCHASE_APPLICATION_REF_PO, calcDate));
                subcontractOrderDetailList = inventoryMapper.listSubcontractOrderDetail(detailIds, getTableName(SUBCONTRACT_ORDER, calcDate), getTableName(SUBCONTRACT_ORDER_DETAIL, calcDate), getTableName(PURCHASE_ORDER_DETAIL, calcDate));
            }
            //已下推委外订单的数量
            //处理已审核 & 部分生成 数据
            for (ReplenishmentResultDTO.EstimatedPurchaseDetailDTO application : applications) {
                if (ApproveStatusEnum.APPROVE.getCode().equals(application.getStatus()) && CreatePoTypeEnum.PARTIAL_GENERATED.getStatus().equals(application.getCreatePoType())) {
                    //委外数量
                    Integer subcontractQty = MathUtil.ZERO;
                    if (!CollectionUtils.isEmpty(subcontractOrderDetailList)) {
                        subcontractQty = subcontractOrderDetailList.stream().filter(v -> v.getSourceDetailId().equals(application.getDetailId()) && StringUtils.isBlank(v.getParentId()))
                                .map(SubcontractOrderDetailEntity::getQty).reduce(MathUtil.ZERO, Integer::sum);
                    }
                    //采购数量
                    Integer purchaseQty = MathUtil.ZERO;
                    if (!CollectionUtils.isEmpty(refList)) {
                        purchaseQty = refList.stream().filter(e -> e.getPurchaseApplicationDetailId().equals(application.getDetailId()))
                                .map(PurchaseApplicationRefPoDTO.ListDTO::getPurchaseQty).reduce(MathUtil.ZERO, Integer::sum);
                    }
                    application.setQty(application.getQty() - subcontractQty - purchaseQty);
                }
            }
            if (!CollectionUtils.isEmpty(applications)) {
                detailList.addAll(applications);
            }
        }
        Set<String> localPurchaseOrder = cfgRuleCommonService.findByKey(baseKey, inventoryResult, baseKey + ":" + CfgRuleInventoryNodeEnum.getLocalPurchaseOrder());
        if (!CollectionUtils.isEmpty(localPurchaseOrder)) {
            List<ReplenishmentResultDTO.EstimatedPurchaseDetailDTO> purchaseDetails = inventoryMapper.listPurchase(localPurchaseOrder, replenishmentResultDTO.getReplenishment().getSkuId(), localWarehouseIds,getTableName(PURCHASE_ORDER, calcDate), getTableName(PURCHASE_ORDER_DETAIL, calcDate));
            if (!CollectionUtils.isEmpty(purchaseDetails)) {
                detailList.addAll(purchaseDetails);
            }
        }
        for (ReplenishmentResultDTO.EstimatedPurchaseDetailDTO detail : detailList) {
            detail.setType(ReplenishmentInventoryTypeEnum.LOCAL_ESTIMATED_DELIVERY.getCode());
            detail.setEstimatedPutAwayDate(detail.getEstimatedPutAwayDate().plusDays(stockUpResult.getPurchaseApproveDays())
                    .plusDays(stockUpResult.getProductionDays()).plusDays(stockUpResult.getSupplierDeliveryDays()).plusDays(stockUpResult.getQcDays())
                    .plusDays(stockUpResult.getPurchaseCycleDays()));
            if (CfgRulePlatformTypeEnum.AMAZON.getCode().equals(replenishmentResultDTO.getReplenishment().getPlatformType()) || CfgRulePlatformTypeEnum.OVERSEAS.getCode().equals(replenishmentResultDTO.getReplenishment().getPlatformType())) {
                detail.setEstimateSalesDate(detail.getEstimatedPutAwayDate().plusDays(stockUpResult.getLogisticsResult().getLogisticsDays()).plusDays(stockUpResult.getInstockDays()));
            } else {
                detail.setEstimateSalesDate(detail.getEstimatedPutAwayDate());
            }
        }
        replenishmentResultDTO.setLocalPurchaseDetails(detailList);
        return new ArrayList<>(detailList.parallelStream()
                .map(v -> new LocalInventoryDTO(v.getWarehouseId(), v.getQty()))
                .collect(Collectors.toMap(
                        LocalInventoryDTO::getWarehouseId,
                        v -> new LocalInventoryDTO(v.getWarehouseId(), v.getQty()),
                        (existing, replacement) -> {
                            // 合并 qty
                            existing.setQty(existing.getQty() + replacement.getQty());
                            return existing;
                        }
                )).values());
    }

    private int getInventoryQty(ReplenishmentResultDTO replenishmentResultDTO, int qty, List<ReplenishmentResultDTO.ReplenishmentInventoryDetailDTO> localUsableDetail, LocalInventoryDTO dto, CfgRuleWarehouseDTO.StrategyDetailResultDTO result, ReplenishmentInventoryTypeEnum inventoryType) {
        if (CfgRuleInventoryAllocateTypeEnum.SHARE.getCode().equals(result.getInventoryAllocateType())) {
            qty += dto.getQty();
            localUsableDetail.add(ReplenishmentResultDTO.ReplenishmentInventoryDetailDTO
                    .buildReplenishmentInventoryDetailDTO(inventoryType.getCode(), result, dto.getQty(), Collections.emptyList()));
        } else {
            List<String> shopIds;
            if (VitualWarehouseChannelTypeEnum.PLATFORM.getCode().equals(result.getChannelType())) {
                //查询平台对应店铺
                shopIds = shopInfoService.getShopInfoByPlatform(String.valueOf(result.getChannelIdJson().get(0)));
            } else {
                shopIds = result.getChannelIdJson().stream().map(Object::toString).collect(Collectors.toList());
            }
            List<LocalInventoryDTO.ShopSalesDTO> shopSales = replenishmentSuggestionService.getSalesByShopIds(shopIds, replenishmentResultDTO.getReplenishment().getSkuId());
            int total = shopSales.stream()
                    .map(LocalInventoryDTO.ShopSalesDTO::getQty)
                    .reduce(0, Math::addExact);
            // 计算每个店铺的占比
            List<ReplenishmentResultDTO.ShopInventoryDetailDTO> detailDTOS = new ArrayList<>();
            for (LocalInventoryDTO.ShopSalesDTO shopSale : shopSales) {
                BigDecimal shopQty = new BigDecimal(dto.getQty()).multiply(new BigDecimal(shopSale.getQty())).divide(new BigDecimal(0 == total ? 1 : total), 2, RoundingMode.HALF_UP).setScale(0, RoundingMode.FLOOR);
                detailDTOS.add(new ReplenishmentResultDTO.ShopInventoryDetailDTO(shopSale.getShopId(), shopQty));
                if (shopSale.getShopId().equals(replenishmentResultDTO.getReplenishment().getShopId())) {
                    qty += shopQty.intValue();
                }
            }
            localUsableDetail.add(ReplenishmentResultDTO.ReplenishmentInventoryDetailDTO
                    .buildReplenishmentInventoryDetailDTO(inventoryType.getCode(), result, dto.getQty(), detailDTOS));
        }
        return qty;
    }
}
