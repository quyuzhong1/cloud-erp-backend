package com.erp.server.mrp.calculation.handler;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class LocalInTransitHandler<T> implements SkuCalculationHandler<T> {
    @Resource
    private LocalPlanPurchaseHandler<T> localPlanPurchaseHandler;
    @Override
    public SkuCalculationHandler<T> getNextHandler(T t) {
        return localPlanPurchaseHandler;
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
