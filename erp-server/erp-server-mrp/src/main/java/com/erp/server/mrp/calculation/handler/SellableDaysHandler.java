package com.erp.server.mrp.calculation.handler;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class SellableDaysHandler<T> implements SkuCalculationHandler<T> {
    @Resource
    private RptOutOfStockHandler<T> rptOutOfStockHandler;
    @Override
    public SkuCalculationHandler<T> getNextHandler(T t) {
        return rptOutOfStockHandler;
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
