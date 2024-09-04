package com.erp.server.mrp.calculation.handler;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class TotalInventoryHandler<T> implements SkuCalculationHandler<T> {
    @Resource
    private HistorySalesHandler<T> historySalesHandler;
    @Override
    public SkuCalculationHandler<T> getNextHandler(T t) {
        return historySalesHandler;
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
