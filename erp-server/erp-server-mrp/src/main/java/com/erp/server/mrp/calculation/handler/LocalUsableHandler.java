package com.erp.server.mrp.calculation.handler;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class LocalUsableHandler<T> implements SkuCalculationHandler<T> {
    @Resource
    private LocalInTransitHandler<T> localInTransitHandler;
    @Override
    public SkuCalculationHandler<T> getNextHandler(T t) {
        return localInTransitHandler;
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
