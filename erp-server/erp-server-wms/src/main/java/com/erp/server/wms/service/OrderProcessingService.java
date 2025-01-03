package com.erp.server.wms.service;

import java.time.LocalDate;

public interface OrderProcessingService {
    /**
     * 自动执行订单跟踪
     * @author will
     * @date 2024/12/18 18:23
     * @param startDate
     */
    void autoOrderProcessing(LocalDate startDate);
}
