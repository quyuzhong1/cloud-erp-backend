package com.erp.server.mrp.calculation.handler;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class LocalPlanPurchaseHandler<T> implements SkuCalculationHandler<T> {
    @Resource
    private TotalInventoryHandler<T> totalInventoryHandler;
    @Override
    public SkuCalculationHandler<T> getNextHandler(T t) {
        return totalInventoryHandler;
    }

    @Override
    public boolean shouldHandle(T t) {
        return false;
    }

    @Override
    public boolean doHandle(T t) {
        return false;
    }
}
