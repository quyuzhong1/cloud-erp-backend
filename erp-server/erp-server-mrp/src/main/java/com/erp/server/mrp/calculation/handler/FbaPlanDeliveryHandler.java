package com.erp.server.mrp.calculation.handler;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class FbaPlanDeliveryHandler<T> implements SkuCalculationHandler<T> {
    @Resource
    private OverseasUsableHandler<T> overseasUsableHandler;
    @Override
    public SkuCalculationHandler<T> getNextHandler(T t) {
        return overseasUsableHandler;
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
