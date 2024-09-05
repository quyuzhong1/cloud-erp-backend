package com.erp.server.mrp.calculation.service.impl;

import com.erp.model.mrp.dto.CfgRuleStockUpDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.enums.CfgRuleInventoryNodeEnum;
import com.erp.model.mrp.enums.SnapshotTableEnum;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.server.mrp.calculation.service.InventoryService;
import com.erp.server.mrp.mapper.InventoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryServiceImpl implements InventoryService {
    @Resource
    private InventoryMapper inventoryMapper;

    @Override
    public int getFbaUsable(ReplenishmentResultDTO replenishmentResultDTO, List<String> codes) {
        String code = String.join("+", codes);
        return inventoryMapper.getFbaUsable(replenishmentResultDTO, code, SnapshotTableEnum.FBA_INVENTORY.getCode() + "_" + replenishmentResultDTO.getReplenishmentDetail().getCalcDate());
    }

    @Override
    public int getFbaInTransit(ReplenishmentResultDTO replenishmentResultDTO, String code, CfgRuleStockUpDTO.StrategyResultDTO stockUpResult) {
        List<ReplenishmentResultDTO.FbaInTransitDetailDTO> inTransitDetails = new ArrayList<>();
        //1、若已生成头程物流单，货件--发货单--头程物流单：
        //已生成头程物流单，且已下单，则预计到货日期 = 头程物流单的下单时间 + 头程物流单上的预计时效
        //已生成头程物流单，但未下单，则预计到货时间 = 发货单的发货时间 + 本地发FBA时效 + FBA入库时间
        //2、若未生成头程物流单，已生成发货单，则预计到货时间 = 发货单的发货时间 + 本地发FBA时效 + FBA入库时间
        if (CfgRuleInventoryNodeEnum.FBA_DELIVERY.getCode().equals(code)) {
            //发FBA，签收数量取对应货件的签收数量 在途数量 = 发货单上的实发数量 - 签收数量；
            inTransitDetails = inventoryMapper.getFbaDelivery(replenishmentResultDTO,
                    SnapshotTableEnum.FBA_SHIPMENT.getCode() + "_" + replenishmentResultDTO.getReplenishmentDetail().getCalcDate(),
                    SnapshotTableEnum.FBA_SHIPMENT_DETAIL.getCode() + "_" + replenishmentResultDTO.getReplenishmentDetail().getCalcDate(),
                    SnapshotTableEnum.FIRST_MILE_DELIVERY.getCode() + "_" + replenishmentResultDTO.getReplenishmentDetail().getCalcDate(),
                    SnapshotTableEnum.FIRST_MILE_DELIVERY_DETAIL.getCode() + "_" + replenishmentResultDTO.getReplenishmentDetail().getCalcDate()
            );
            List<String> firstMileDeliveryIds = inTransitDetails.stream().map(ReplenishmentResultDTO.FbaInTransitDetailDTO::getSourceId).collect(Collectors.toList());
            //查询头程物流单
            List<LogisticsBillEntity> logisticsBills = inventoryMapper.getLogisticsBillBySourceIds(firstMileDeliveryIds, SnapshotTableEnum.LOGISTICS_BILL.getCode() + "_" + replenishmentResultDTO.getReplenishmentDetail().getCalcDate());
            for (ReplenishmentResultDTO.FbaInTransitDetailDTO detail : inTransitDetails) {
                LogisticsBillEntity logisticsBill = logisticsBills.stream().filter(v -> v.getSourceId().equals(detail.getSourceId()))
                        .filter(v -> !ObjectUtils.isEmpty(v.getOrderTime()))
                        .findFirst()
                        .orElse(null);
                if (ObjectUtils.isEmpty(logisticsBill)) {
                    detail.setPlanArrivalDate(detail.getDeliveryDate().plusDays(stockUpResult.getLogisticsResult().getLogisticsDays()).plusDays(stockUpResult.getInstockDays()));
                }else {
                    detail.setPlanArrivalDate(logisticsBill.getOrderTime().toLocalDate().plusDays(stockUpResult.getLogisticsResult().getLogisticsDays()).plusDays(stockUpResult.getInstockDays()));
                }
            }
        } else if (CfgRuleInventoryNodeEnum.FBA_SHIPMENT.getCode().equals(code)) {
            //FBA在途 =发货数量 - 签收数量
            inTransitDetails = inventoryMapper.getFbaShipment(replenishmentResultDTO,
                    SnapshotTableEnum.FBA_SHIPMENT.getCode() + "_" + replenishmentResultDTO.getReplenishmentDetail().getCalcDate(),
                    SnapshotTableEnum.FBA_SHIPMENT_DETAIL.getCode() + "_" + replenishmentResultDTO.getReplenishmentDetail().getCalcDate()
            );
            if (CollectionUtils.isEmpty(inTransitDetails)) {
                return 0;
            }
            List<String> codes = inTransitDetails.stream().map(ReplenishmentResultDTO.FbaInTransitDetailDTO::getSourceCode).collect(Collectors.toList());
            List<FirstMileDeliveryDTO.FbaShipmentDTO> firstMileDeliveryList =  inventoryMapper.listFirstMileDelivery(codes, SnapshotTableEnum.FIRST_MILE_DELIVERY.getCode() + "_" + replenishmentResultDTO.getReplenishmentDetail().getCalcDate(),
                    SnapshotTableEnum.FIRST_MILE_DELIVERY_DETAIL.getCode() + "_" + replenishmentResultDTO.getReplenishmentDetail().getCalcDate());
            List<String> firstMileDeliveryIds = firstMileDeliveryList.stream().map(FirstMileDeliveryDTO.FbaShipmentDTO::getId).collect(Collectors.toList());
            List<LogisticsBillEntity> logisticsBills = inventoryMapper.getLogisticsBillBySourceIds(firstMileDeliveryIds, SnapshotTableEnum.LOGISTICS_BILL.getCode() + "_" + replenishmentResultDTO.getReplenishmentDetail().getCalcDate());
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
                    detail.setPlanArrivalDate(fbaShipmentDTO.getApproveTime().toLocalDate().plusDays(stockUpResult.getLogisticsResult().getLogisticsDays()).plusDays(stockUpResult.getInstockDays()));
                }else {
                    detail.setPlanArrivalDate(logisticsBill.getOrderTime().toLocalDate().plusDays(stockUpResult.getLogisticsResult().getLogisticsDays()).plusDays(stockUpResult.getInstockDays()));
                }
            }

        }
        replenishmentResultDTO.setFbaInTransitDetails(inTransitDetails);
        return inTransitDetails.stream().map(ReplenishmentResultDTO.FbaInTransitDetailDTO::getInTransitQty)
                .reduce(0, Math::addExact);
    }
}
