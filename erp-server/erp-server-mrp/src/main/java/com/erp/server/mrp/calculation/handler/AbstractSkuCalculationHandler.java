package com.erp.server.mrp.calculation.handler;

import com.erp.model.mrp.dto.ReplenishmentResultDTO;

import java.util.List;

public abstract class AbstractSkuCalculationHandler implements SkuCalculationHandler {

    @Override
    public void handle(List<ReplenishmentResultDTO> r) {
        for (ReplenishmentResultDTO dto : r) {
            if (shouldHandle(dto)) {
                // 处理当前逻辑
                doHandle(dto, r);
            }
        }
        // 动态获取下一个处理器
        SkuCalculationHandler nextHandler = getNextHandler(r);
        if (nextHandler != null) {
            nextHandler.handle(r);
        }
    }
}
