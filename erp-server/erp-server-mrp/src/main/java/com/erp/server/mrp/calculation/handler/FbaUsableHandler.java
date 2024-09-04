package com.erp.server.mrp.calculation.handler;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class FbaUsableHandler<T> implements SkuCalculationHandler<T> {
    @Resource
    private FbaInTransitHandler<T> fbaInTransitHandler;
    @Override
    public SkuCalculationHandler<T> getNextHandler(T t) {
        return fbaInTransitHandler;
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
