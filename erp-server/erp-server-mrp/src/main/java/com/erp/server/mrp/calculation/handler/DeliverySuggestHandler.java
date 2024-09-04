package com.erp.server.mrp.calculation.handler;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class DeliverySuggestHandler<T> implements SkuCalculationHandler<T> {
    @Resource
    private PurchaseSuggestHandler<T> purchaseSuggestHandler;
    @Override
    public SkuCalculationHandler<T> getNextHandler(T t) {
        return purchaseSuggestHandler;
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
