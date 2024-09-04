package com.erp.server.mrp.calculation.handler;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class SalesEstimateHandler<T> implements SkuCalculationHandler<T> {
    @Resource
    private StockingTimeHandler<T> stockingTimeHandler;
    @Override
    public SkuCalculationHandler<T> getNextHandler(T t) {
        return stockingTimeHandler;
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
