package com.erp.server.mrp.calculation.handler;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class PurchaseSuggestHandler<T> implements SkuCalculationHandler<T> {
    @Resource
    private OverseasInTransitHandler<T> overseasInTransitHandler;
    @Override
    public SkuCalculationHandler<T> getNextHandler(T t) {
        return overseasInTransitHandler;
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
