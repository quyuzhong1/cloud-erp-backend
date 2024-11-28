package com.erp.server.mrp.calculation.handler;

import com.erp.model.mrp.dto.CfgRuleStrategyDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractSkuCalculationHandler implements SkuCalculationHandler {

    @Override
    public void handle(CfgRuleStrategyDTO t, ReplenishmentResultDTO r) {
        if (shouldHandle(t, r)) {
            log.error("执行器{}开始执行,时间{}", this.getClass().getSimpleName(), System.currentTimeMillis());
            // 处理当前逻辑
            doHandle(t, r);
            log.error("执行器{}完成执行,时间{}", this.getClass().getSimpleName(), System.currentTimeMillis());

        }
        // 动态获取下一个处理器
        SkuCalculationHandler nextHandler = getNextHandler(t, r);
        if (nextHandler != null) {
            nextHandler.handle(t, r);
        }
    }
}
