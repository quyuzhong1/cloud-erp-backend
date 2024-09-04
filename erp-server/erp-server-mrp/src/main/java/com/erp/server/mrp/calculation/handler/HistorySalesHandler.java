package com.erp.server.mrp.calculation.handler;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class HistorySalesHandler<T> implements SkuCalculationHandler<T> {
    @Resource
    private SalesEstimateHandler<T> salesEstimateHandler;
    @Override
    public SkuCalculationHandler<T> getNextHandler(T t) {
        return salesEstimateHandler;
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
