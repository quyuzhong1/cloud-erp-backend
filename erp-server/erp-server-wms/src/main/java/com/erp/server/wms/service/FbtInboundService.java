package com.erp.server.wms.service;

import com.erp.model.wms.dto.TiktokFbtDTO;

public interface FbtInboundService {

    void syncInboundOrder(String inboundOrderId);

    void syncInboundOrder(String inboundOrderId, String sellerOpenId);

    void syncInboundOrderFromDmp(TiktokFbtDTO.InboundOrderDTO inboundOrder);

    void syncRecentInboundOrders();

    void syncRecentInboundOrders(Long startTime, Long endTime);

    void syncRecentInventorySnapshots();

    boolean handleInventoryRecordFromDmp(TiktokFbtDTO.InventoryRecordDTO record);

    boolean upsertInventorySnapshotFromDmp(TiktokFbtDTO.InventorySnapshotDTO snapshot, String authId);
}
