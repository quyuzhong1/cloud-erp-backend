package com.erp.server.mrp.calculation.handler;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class FbaInTransitHandler<T> implements SkuCalculationHandler<T> {
    @Resource
    private FbaPlanDeliveryHandler<T> fbaPlanDeliveryHandler;

    @Override
    public SkuCalculationHandler<T> getNextHandler(T t) {
        return fbaPlanDeliveryHandler;
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
