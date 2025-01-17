package com.erp.server.mrp.calculation.handler;

import com.erp.model.mrp.dto.ReplenishmentResultDTO;

import java.util.List;

public interface SkuCalculationHandler {

    void handle(List<ReplenishmentResultDTO> r);

    /**
     * 获取下个责任链
     */
    SkuCalculationHandler getNextHandler(List<ReplenishmentResultDTO> r);

    // 判断是否需要处理当前请求
    boolean shouldHandle(ReplenishmentResultDTO dto);

    /**
     * 执行具体的处理逻辑
     */
    void doHandle(ReplenishmentResultDTO dto, List<ReplenishmentResultDTO> r);
}
