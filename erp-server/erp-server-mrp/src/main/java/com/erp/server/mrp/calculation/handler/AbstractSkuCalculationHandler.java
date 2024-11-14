package com.erp.server.mrp.calculation.handler;

import com.erp.model.mrp.dto.CfgRuleStrategyDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;

public abstract class AbstractSkuCalculationHandler implements SkuCalculationHandler {

    @Override
    public void handle(CfgRuleStrategyDTO t, ReplenishmentResultDTO r) {
        if (shouldHandle(t, r)) {
            // 处理当前逻辑
            doHandle(t, r);
        }
        // 动态获取下一个处理器
        SkuCalculationHandler nextHandler = getNextHandler(t, r);
        if (nextHandler != null) {
            nextHandler.handle(t, r);
        }
    }
}
