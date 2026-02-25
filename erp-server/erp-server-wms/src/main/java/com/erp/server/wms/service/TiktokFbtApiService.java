package com.erp.server.wms.service;

import com.erp.model.wms.dto.TiktokFbtDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface TiktokFbtApiService {

    List<TiktokFbtDTO.InboundOrderDTO> queryInboundOrders(String shopId, List<String> inboundOrderIds, LocalDateTime updatedAfter);

    List<TiktokFbtDTO.InventoryRecordDTO> queryInventoryRecords(String shopId,
                                                                List<String> goodsIds,
                                                                List<String> warehouseIds,
                                                                Long startTime,
                                                                Long endTime);
}
