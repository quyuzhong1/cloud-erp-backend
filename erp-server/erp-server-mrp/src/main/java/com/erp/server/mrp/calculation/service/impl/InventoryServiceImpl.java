package com.erp.server.mrp.calculation.service.impl;

import com.common.business.enums.SourceTypeEnum;
import com.erp.model.mrp.dto.*;
import com.erp.model.mrp.enums.*;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.enums.DeliveryPlanTypeEnum;
import com.erp.model.wms.enums.VitualWarehouseChannelTypeEnum;
import com.erp.server.mrp.calculation.service.InventoryService;
import com.erp.server.mrp.calculation.service.ShopInfoService;
import com.erp.server.mrp.mapper.InventoryMapper;
import com.erp.server.mrp.service.ReplenishmentSuggestionService;
import com.google.common.collect.Lists;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
    @Resource(name = "mrpExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Override
    public int getFbaUsable(ReplenishmentResultDTO replenishmentResultDTO, List<String> codes) {
        String code = String.join("+", codes);
        String calcDate = replenishmentResultDTO.getReplenishmentDetail().getCalcDate();
        return inventoryMapper.getFbaUsable(replenishmentResultDTO, code, SnapshotTableEnum.getTableName(FBA_INVENTORY, calcDate));
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
                    SnapshotTableEnum.getTableName(FBA_SHIPMENT, calcDate),
                    SnapshotTableEnum.getTableName(FBA_SHIPMENT_DETAIL, calcDate),
                    SnapshotTableEnum.getTableName(FIRST_MILE_DELIVERY, calcDate),
                    SnapshotTableEnum.getTableName(FIRST_MILE_DELIVERY_DETAIL, calcDate)
            );
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
    public void getHistoryInventory(ReplenishmentResultDTO replenishmentResult) {
        //拆分时间为表名
        String calcDate = replenishmentResult.getReplenishmentDetail().getCalcDate();
        LocalDate endDate = LocalDate.parse(calcDate, DateTimeFormatter.BASIC_ISO_DATE);
        LocalDate startDate = LocalDate.parse(calcDate, DateTimeFormatter.BASIC_ISO_DATE).minusDays(360);
        List<LocalDate> dateList = new ArrayList<>();
        // 遍历每一天
        while (!startDate.isAfter(endDate)) {
            dateList.add(startDate);
            startDate = startDate.plusDays(1);
        }
        //拆分为时间list
        List<List<LocalDate>> partition = Lists.partition(dateList, 60);
        for (List<LocalDate> list : partition) {
            CompletableFuture.runAsync(() -> {
                for (LocalDate localDate : list) {
                    getHistoryInventoryByPlatformType(localDate, replenishmentResult);
                }
            }, threadPoolTaskExecutor);
        }
    }

    @Override
    public void getFbaPlanDelivery(ReplenishmentResultDTO replenishmentResultDTO, List<String> strategyCodes, CfgRuleStockUpDTO.StrategyResultDTO stockUpResult) {
        String calcDate = replenishmentResultDTO.getReplenishmentDetail().getCalcDate();
        List<ReplenishmentResultDTO.EstimatedDeliveryDetailDTO> estimatedDeliveryDetails = inventoryMapper.getPlanDelivery(DeliveryPlanTypeEnum.FBA.getCode(), strategyCodes, replenishmentResultDTO, SnapshotTableEnum.getTableName(WMS_DELIVERY_PLAN, calcDate), SnapshotTableEnum.getTableName(WMS_DELIVERY_PLAN_DETAIL, calcDate));
        for (ReplenishmentResultDTO.EstimatedDeliveryDetailDTO detail : estimatedDeliveryDetails) {
            detail.setType(CfgRulePlatformTypeEnum.AMAZON.getCode());
            detail.setEstimateSalesDate(detail.getEstimateSalesDate().plusDays(stockUpResult.getInstockDays()).plusDays(stockUpResult.getLogisticsResult().getLogisticsDays()).plusDays(stockUpResult.getLogisticsResult().getLogisticsCycleDays()));
            detail.setSourceType(SourceTypeEnum.DELIVERY_PLAN.getCode());
        }
        if (CollectionUtils.isEmpty(replenishmentResultDTO.getFbaDeliveryDetails())) {
            replenishmentResultDTO.setFbaDeliveryDetails(estimatedDeliveryDetails);
        } else {
            replenishmentResultDTO.getFbaDeliveryDetails().addAll(estimatedDeliveryDetails);
        }
    }

    @Override
    public int getOverseasUsable(ReplenishmentResultDTO replenishmentResultDTO, List<String> codes, CfgRuleStrategyDTO cfgRuleStrategyDTO) {
        String code = String.join("+", codes);
        String calcDate = replenishmentResultDTO.getReplenishmentDetail().getCalcDate();
        int overseasUsable = inventoryMapper.getOverseasUsable(replenishmentResultDTO, code, getTableName(OVERSEAS_INVENTORY, calcDate));
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
    public int getLocalUsable(ReplenishmentResultDTO replenishmentResultDTO, List<String> codes, CfgRuleStrategyDTO cfgRuleStrategyDTO) {
        int qty = 0;
        CfgRuleWarehouseDTO.StrategyResultDTO warehouseResult = cfgRuleStrategyDTO.getWarehouseResult();
        String calcDate = replenishmentResultDTO.getReplenishmentDetail().getCalcDate();
        List<ReplenishmentResultDTO.ReplenishmentInventoryDetailDTO> localUsableDetail = new ArrayList<>();
        if (warehouseResult.getIsEnableVirtual()) {
            List<LocalInventoryDTO> invetoryList = inventoryMapper.getVirtualUsable(replenishmentResultDTO.getReplenishment().getSkuId(), codes, getTableName(VIRTUAL_INVENTORY, calcDate));
            for (LocalInventoryDTO dto : invetoryList) {
                for (CfgRuleWarehouseDTO.StrategyDetailResultDTO result : warehouseResult.getLocalWarehouseList()) {
                    if (!result.getVirtualWarehouseId().equals(dto.getWarehouseId()) || (VitualWarehouseChannelTypeEnum.SHOP.getCode().equals(result.getChannelType())
                            && !result.getChannelIdJson().contains(replenishmentResultDTO.getReplenishment().getShopId()))) {
                        continue;
                    }
                    //根据库存分配配置
                    qty = getInventoryQty(replenishmentResultDTO, qty, localUsableDetail, dto, result);
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
                    qty = getInventoryQty(replenishmentResultDTO, qty, localUsableDetail, dto, result);
                }
            }
        }
        replenishmentResultDTO.setLocalUsableDetail(localUsableDetail);
        return qty;
    }

    private int getInventoryQty(ReplenishmentResultDTO replenishmentResultDTO, int qty, List<ReplenishmentResultDTO.ReplenishmentInventoryDetailDTO> localUsableDetail, LocalInventoryDTO dto, CfgRuleWarehouseDTO.StrategyDetailResultDTO result) {
        if (CfgRuleInventoryAllocateTypeEnum.SHARE.getCode().equals(result.getInventoryAllocateType())) {
            qty += dto.getQty();
            localUsableDetail.add(ReplenishmentResultDTO.ReplenishmentInventoryDetailDTO
                    .buildReplenishmentInventoryDetailDTO(ReplenishmentInventoryTypeEnum.LOCAL_USABLE.getCode(), result, dto.getQty(), Collections.emptyList()));
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
                detailDTOS.add(new ReplenishmentResultDTO.ShopInventoryDetailDTO(shopSale.getShopId(), new BigDecimal(dto.getQty()).multiply(new BigDecimal(shopSale.getQty()).divide(new BigDecimal(total), 2 , RoundingMode.HALF_UP))));
            }
            localUsableDetail.add(ReplenishmentResultDTO.ReplenishmentInventoryDetailDTO
                    .buildReplenishmentInventoryDetailDTO(ReplenishmentInventoryTypeEnum.LOCAL_USABLE.getCode(), result, dto.getQty(), detailDTOS));
        }
        return qty;
    }

    /**
     * 根据平台获取历史库存
     *
     * @param localDate           日期
     * @param replenishmentResult 补货结果
     */
    private void getHistoryInventoryByPlatformType(LocalDate localDate, ReplenishmentResultDTO replenishmentResult) {
        if (CfgRulePlatformTypeEnum.AMAZON.getCode().equals(replenishmentResult.getReplenishment().getPlatformType())) {
            getHistoryInventoryByFba(localDate, replenishmentResult);
        } else if (CfgRulePlatformTypeEnum.OVERSEAS.getCode().equals(replenishmentResult.getReplenishment().getPlatformType())) {
            //todo 后期做
        } else if (CfgRulePlatformTypeEnum.B2B.getCode().equals(replenishmentResult.getReplenishment().getPlatformType()) ||
                CfgRulePlatformTypeEnum.INTERNAL.getCode().equals(replenishmentResult.getReplenishment().getPlatformType())) {
            //todo 后期做
        }
    }


    /**
     * 获取Fba历史库存
     *
     * @param localDate           日期
     * @param replenishmentResult 补货结果
     */
    private void getHistoryInventoryByFba(LocalDate localDate, ReplenishmentResultDTO replenishmentResult) {
        int qty = 0;
        // 判断表是否存在
        String tableName = getTableName(FBA_INVENTORY, localDate.format(DateTimeFormatter.BASIC_ISO_DATE));
        boolean exist = inventoryMapper.isTableExist(tableName);
        //如果表不存在，库存默认为0
        if (exist) {
            qty = inventoryMapper.getFbaOldUsable(replenishmentResult, tableName);
        }
        ReplenishmentResultDTO.SalesInfoDTO infoDTO = replenishmentResult.getSalesInfos()
                .stream()
                .filter(v -> v.getDate().equals(localDate))
                .findFirst()
                .orElseGet(() -> {
                    ReplenishmentResultDTO.SalesInfoDTO salesInfoDTO = new ReplenishmentResultDTO.SalesInfoDTO();
                    salesInfoDTO.setOriginalSalesQty(0);
                    return salesInfoDTO;
                });
        infoDTO.setOriginalInventoryQty(qty);
    }
}
