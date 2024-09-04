package com.erp.server.mrp.calculation.handler;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class OverseasPlanDeliveryHandler<T> implements SkuCalculationHandler<T> {
    @Resource
    private LocalUsableHandler<T> localUsableHandler;
    @Override
    public SkuCalculationHandler<T> getNextHandler(T t) {
        return localUsableHandler;
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
