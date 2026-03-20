package com.erp.server.wms.rocketmq.consumer.handler;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformFbtShipmentDTO;
import com.erp.model.wms.dto.TiktokFbtDTO;
import com.erp.server.wms.service.FbtInboundService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * FBT货件消息处理器
 */
@Component
public class FbtShipmentMessageHandler {

    @Resource
    private FbtInboundService fbtInboundService;

    public void handle(String data) {
        PlatformFbtShipmentDTO dto = JSON.parseObject(data, PlatformFbtShipmentDTO.class);
        if (dto == null || dto.getInboundOrderId() == null) {
            return;
        }
        TiktokFbtDTO.InboundOrderDTO inboundOrderDTO = new TiktokFbtDTO.InboundOrderDTO();
        inboundOrderDTO.setShopId(dto.getShopId());
        inboundOrderDTO.setInboundOrderId(dto.getInboundOrderId());
        inboundOrderDTO.setShipmentName(dto.getShipmentName());
        inboundOrderDTO.setWarehouseCode(dto.getWarehouseCode());
        inboundOrderDTO.setStatus(dto.getPlatformShipmentStatus());
        inboundOrderDTO.setUpdatedTime(dto.getUpdatedTime());
        inboundOrderDTO.setFbtWarehouseIds(dto.getWarehouseCode() == null
                ? Collections.emptyList()
                : Collections.singletonList(dto.getWarehouseCode()));
        inboundOrderDTO.setCarriers(toCarrierList(dto.getCarrierList()));
        inboundOrderDTO.setPlannedGoods(toPlannedGoods(dto.getPlannedGoods()));
        inboundOrderDTO.setReceivedBatches(toReceivedBatches(dto.getReceivedBatches()));
        inboundOrderDTO.setGoodsIds(inboundOrderDTO.getPlannedGoods().stream()
                .map(TiktokFbtDTO.PlannedGoodDTO::getGoodsId)
                .filter(goodsId -> goodsId != null && !goodsId.isEmpty())
                .distinct()
                .collect(Collectors.toList()));
        fbtInboundService.syncInboundOrderFromDmp(inboundOrderDTO);
    }

    private List<TiktokFbtDTO.CarrierDTO> toCarrierList(List<PlatformFbtShipmentDTO.CarrierDTO> carrierList) {
        if (CollUtil.isEmpty(carrierList)) {
            return Collections.emptyList();
        }
        return carrierList.stream().map(item -> {
            TiktokFbtDTO.CarrierDTO dto = new TiktokFbtDTO.CarrierDTO();
            dto.setCarrierName(item.getCarrierName());
            dto.setTrackingNumber(item.getTrackingNumber());
            return dto;
        }).collect(Collectors.toList());
    }

    private List<TiktokFbtDTO.PlannedGoodDTO> toPlannedGoods(List<PlatformFbtShipmentDTO.PlannedGoodDTO> plannedGoods) {
        if (CollUtil.isEmpty(plannedGoods)) {
            return Collections.emptyList();
        }
        return plannedGoods.stream().map(item -> {
            TiktokFbtDTO.PlannedGoodDTO dto = new TiktokFbtDTO.PlannedGoodDTO();
            dto.setGoodsId(item.getGoodsId());
            dto.setReferenceCode(item.getReferenceCode());
            dto.setName(item.getName());
            dto.setQuantity(item.getQuantity());
            dto.setSkuIds(item.getSkuIds() == null ? Collections.emptyList() : item.getSkuIds());
            return dto;
        }).collect(Collectors.toList());
    }

    private List<TiktokFbtDTO.ReceivedBatchDTO> toReceivedBatches(List<PlatformFbtShipmentDTO.ReceivedBatchDTO> receivedBatches) {
        if (CollUtil.isEmpty(receivedBatches)) {
            return Collections.emptyList();
        }
        return receivedBatches.stream().map(item -> {
            TiktokFbtDTO.ReceivedBatchDTO dto = new TiktokFbtDTO.ReceivedBatchDTO();
            dto.setBatchId(item.getBatchId());
            dto.setGoodsId(item.getGoodsId());
            dto.setNormalQuantity(item.getNormalQuantity());
            dto.setDefectiveQuantity(item.getDefectiveQuantity());
            dto.setTotalQuantity(item.getTotalQuantity());
            dto.setProductIds(item.getProductIds() == null ? Collections.emptyList() : item.getProductIds());
            dto.setSkuIds(item.getSkuIds() == null ? Collections.emptyList() : item.getSkuIds());
            return dto;
        }).collect(Collectors.toList());
    }
}
