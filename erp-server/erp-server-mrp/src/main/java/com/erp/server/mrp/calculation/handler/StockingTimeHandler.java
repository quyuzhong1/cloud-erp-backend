package com.erp.server.mrp.calculation.handler;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class StockingTimeHandler<T> implements SkuCalculationHandler<T> {
    @Resource
    private SellableDaysHandler<T> sellableDaysHandler;
    @Override
    public SkuCalculationHandler<T> getNextHandler(T t) {
        return sellableDaysHandler;
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
