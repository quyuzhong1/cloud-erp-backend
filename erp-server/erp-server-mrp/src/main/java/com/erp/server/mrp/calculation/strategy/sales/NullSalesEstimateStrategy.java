package com.erp.server.mrp.calculation.strategy.sales;

import com.erp.model.mrp.enums.SalesEstimateTypeEnum;

public class NullSalesEstimateStrategy<T, R> implements SalesEstimateStrategy<T, R> {
    @Override
    public SalesEstimateTypeEnum getType() {
        return null;
    }

    @Override
    public R process(T t) {
        return null;
    }
}
