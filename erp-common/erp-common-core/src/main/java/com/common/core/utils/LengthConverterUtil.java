package com.common.core.utils;

import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class LengthConverterUtil {
    private LengthConverterUtil() {
    }

    // 将厘米转换为毫米
    public static BigDecimal cmToMm(BigDecimal centimeters) {
        if (ObjectUtils.isEmpty(centimeters)){
            return BigDecimal.ZERO;
        }
        return centimeters.multiply(new BigDecimal(10));
    }

    // 将毫米转换为厘米
    public static BigDecimal mmToCm(BigDecimal millimeters) {
        if (ObjectUtils.isEmpty(millimeters)){
            return BigDecimal.ZERO;
        }
        return millimeters.divide(new BigDecimal(10), 2, RoundingMode.HALF_UP);
    }
}
