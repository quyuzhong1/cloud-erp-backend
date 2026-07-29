package com.erp.server.tms.util;

import cn.hutool.core.collection.CollUtil;
import com.common.core.enums.CurrencyEnum;
import com.erp.model.tms.dto.TmsCostDetailDTO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 尾程费用明细本位币汇总工具：列表展示与分摊均按本位币费用值合计。
 */
public final class CostDetailLocalCurrencyHelper {

    private CostDetailLocalCurrencyHelper() {
    }

    /**
     * 列表/分摊金额：始终按本位币费用值合计。
     */
    public static BigDecimal resolveListAmount(List<TmsCostDetailDTO.CostViewDTO> costList) {
        if (CollUtil.isEmpty(costList)) {
            return BigDecimal.ZERO;
        }
        return costList.stream()
                .map(TmsCostDetailDTO.CostViewDTO::resolveCostValueLocalCurrency)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 列表/分摊币种：始终取本位币。
     */
    public static String resolveListCurrency(List<TmsCostDetailDTO.CostViewDTO> costList) {
        if (CollUtil.isEmpty(costList)) {
            return CurrencyEnum.CNY.getCurrencyCode();
        }
        return costList.get(0).resolveLocalCurrency();
    }
}
