package com.erp.server.wms.service.impl;

import com.erp.server.wms.service.FbtInboundService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class FbtInboundAsyncService {

    @Resource
    private FbtInboundService fbtInboundService;

    @Async("wmsErpExecutor")
    public void asyncSyncInboundOrder(String inboundOrderId) {
        fbtInboundService.syncInboundOrder(inboundOrderId, null);
    }

    @Async("wmsErpExecutor")
    public void asyncSyncInboundOrder(String inboundOrderId, String sellerOpenId) {
        fbtInboundService.syncInboundOrder(inboundOrderId, sellerOpenId);
    }
}
