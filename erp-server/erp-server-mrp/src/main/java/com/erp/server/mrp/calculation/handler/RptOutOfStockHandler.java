package com.erp.server.mrp.calculation.handler;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class RptOutOfStockHandler<T> implements SkuCalculationHandler<T> {
    @Resource
    private DeliverySuggestHandler<T> deliverySuggestHandler;
    @Override
    public SkuCalculationHandler<T> getNextHandler(T t) {
        return deliverySuggestHandler;
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
