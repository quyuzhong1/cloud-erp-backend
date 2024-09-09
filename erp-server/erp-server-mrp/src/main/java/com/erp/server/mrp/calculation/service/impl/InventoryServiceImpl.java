package com.erp.server.mrp.calculation.service.impl;

import com.erp.model.mrp.dto.CfgRuleStockUpDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.enums.CfgRuleInventoryNodeEnum;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import com.erp.model.mrp.enums.SnapshotTableEnum;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.server.mrp.calculation.service.InventoryService;
import com.erp.server.mrp.mapper.InventoryMapper;
import com.google.common.collect.Lists;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.erp.model.mrp.enums.SnapshotTableEnum.*;

@Service
public class InventoryServiceImpl implements InventoryService {
    @Resource
    private InventoryMapper inventoryMapper;
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
