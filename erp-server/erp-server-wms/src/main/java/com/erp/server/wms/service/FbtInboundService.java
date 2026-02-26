package com.erp.server.wms.service;

public interface FbtInboundService {

    void syncInboundOrder(String inboundOrderId);

    void syncInboundOrder(String inboundOrderId, String sellerOpenId);

    void syncRecentInboundOrders();

    void syncRecentInboundOrders(Long startTime, Long endTime);

    void syncRecentInventorySnapshots();
}
