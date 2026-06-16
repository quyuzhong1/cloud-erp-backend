package com.erp.server.dmp.inout.utils;

import com.common.business.dto.PlatformOrderDTO;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.entity.DmpSoInfoEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * DMP 平台订单 MQ 出向：区分折前订单总额（PlatformOrderDTO.amount）与平台实付（payAmount）。
 */
public final class PlatformOrderAmountUtils {

    private PlatformOrderAmountUtils() {
    }

    /**
     * 订单总额（折前）：优先 allAmount；否则 payAmount + totalDiscount；再否则 payAmount。
     */
    public static BigDecimal resolveOrderAmount(BigDecimal allAmount, BigDecimal payAmount, BigDecimal totalDiscount) {
        BigDecimal all = MathUtil.getValue(allAmount);
        if (all.compareTo(BigDecimal.ZERO) > 0) {
            return all;
        }
        BigDecimal pay = MathUtil.getValue(payAmount);
        BigDecimal discount = MathUtil.getValue(totalDiscount);
        if (pay.compareTo(BigDecimal.ZERO) > 0 || discount.compareTo(BigDecimal.ZERO) > 0) {
            return pay.add(discount);
        }
        return pay;
    }

    public static void applyMainAmounts(PlatformOrderDTO orderDTO, BigDecimal allAmount, BigDecimal payAmount, BigDecimal totalDiscount) {
        if (orderDTO == null) {
            return;
        }
        orderDTO.setAmount(resolveOrderAmount(allAmount, payAmount, totalDiscount));
        orderDTO.setPayAmount(payAmount);
    }

    public static void applyMainAmounts(PlatformOrderDTO orderDTO, DmpSoInfoEntity entity) {
        if (orderDTO == null || entity == null) {
            return;
        }
        applyMainAmounts(orderDTO, entity.getAllAmount(), entity.getPayAmount(), entity.getTotalDiscount());
    }

    public static void applyMainAmounts(PlatformOrderDTO orderDTO, List<DmpSoInfoEntity> entities) {
        if (orderDTO == null || entities == null || entities.isEmpty()) {
            return;
        }
        BigDecimal allAmount = sum(entities, DmpSoInfoEntity::getAllAmount);
        BigDecimal payAmount = sum(entities, DmpSoInfoEntity::getPayAmount);
        BigDecimal totalDiscount = sum(entities, DmpSoInfoEntity::getTotalDiscount);
        applyMainAmounts(orderDTO, allAmount, payAmount, totalDiscount);
    }

    private static BigDecimal sum(List<DmpSoInfoEntity> entities, Function<DmpSoInfoEntity, BigDecimal> getter) {
        return entities.stream()
                .map(getter)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
