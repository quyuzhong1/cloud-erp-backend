package com.erp.server.mrp.calculation.handler;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class OverseasInTransitHandler<T> implements SkuCalculationHandler<T> {
    @Resource
    private OverseasPlanDeliveryHandler<T> overseasPlanDeliveryHandler;
    @Override
    public SkuCalculationHandler<T> getNextHandler(T t) {
        return overseasPlanDeliveryHandler;
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
