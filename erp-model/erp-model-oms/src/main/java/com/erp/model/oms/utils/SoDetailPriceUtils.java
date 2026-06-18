package com.erp.model.oms.utils;

import com.erp.model.oms.entity.SoDetailEntity;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 销售订单明细单价取值工具。
 */
public final class SoDetailPriceUtils {

    private SoDetailPriceUtils() {
    }

    /**
     * 解析报关用销售单价：优先含税单价，缺失时回退不含税单价。
     */
    public static BigDecimal resolveTaxUnitPrice(SoDetailEntity soDetailEntity) {
        if (Objects.isNull(soDetailEntity)) {
            return null;
        }
        if (Objects.nonNull(soDetailEntity.getTaxPrice())) {
            return soDetailEntity.getTaxPrice();
        }
        return soDetailEntity.getPrice();
    }
}
