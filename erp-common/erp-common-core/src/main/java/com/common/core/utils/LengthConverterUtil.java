package com.common.core.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class LengthConverterUtil {
    private LengthConverterUtil() {
    }

    // 将厘米转换为毫米
    public static BigDecimal cmToMm(BigDecimal centimeters) {
        return centimeters.multiply(new BigDecimal(10));
    }

    // 将毫米转换为厘米
    public static BigDecimal mmToCm(BigDecimal millimeters) {
        return millimeters.divide(new BigDecimal(10), 2, RoundingMode.HALF_UP);
    }
}
