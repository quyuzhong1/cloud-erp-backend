package com.erp.server.mrp.calculation.handler;

import com.erp.model.mrp.dto.CfgRuleStrategyDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;

public interface SkuCalculationHandler {

    void handle(CfgRuleStrategyDTO t, ReplenishmentResultDTO r);

    /**
     * 获取下个责任链
     */
    SkuCalculationHandler getNextHandler(CfgRuleStrategyDTO t, ReplenishmentResultDTO r);

    // 判断是否需要处理当前请求
    boolean shouldHandle(CfgRuleStrategyDTO t, ReplenishmentResultDTO r);

    /**
     * 执行具体的处理逻辑
     */
    void doHandle(CfgRuleStrategyDTO t, ReplenishmentResultDTO r);
}
