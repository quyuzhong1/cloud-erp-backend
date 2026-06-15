package com.erp.server.oms.utils;

import cn.hutool.core.collection.CollUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoInfoEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * B2B 销售订单主表实付总额计算。
 * 新增/编辑时不接受入参 paidAmount，统一由明细实付金额汇总得出。
 */
public final class SoInfoAmountUtil {

    private static final int SCALE = 4;

    private SoInfoAmountUtil() {
    }

    /**
     * 忽略入参中的实付总额，避免 BeanMapper 拷贝后直接落库。
     */
    public static void ignoreRequestPaidAmount(SoInfoEntity main) {
        if (main != null) {
            main.setPaidAmount(null);
        }
    }

    /**
     * 主表实付总额 = sum(明细.实付金额)；无明细时回退为 orderAmount - discountAmount。
     */
    public static void applyMainPaidAmount(SoInfoEntity main, List<SoDetailEntity> details) {
        if (main == null) {
            return;
        }
        BigDecimal paidTotal;
        if (CollUtil.isNotEmpty(details)) {
            paidTotal = details.stream()
                    .map(SoDetailEntity::getAmount)
                    .map(MathUtil::getValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } else {
            paidTotal = MathUtil.getValue(main.getOrderAmount())
                    .subtract(MathUtil.getValue(main.getDiscountAmount()));
        }
        main.setPaidAmount(paidTotal.setScale(SCALE, RoundingMode.HALF_UP));
    }
}
