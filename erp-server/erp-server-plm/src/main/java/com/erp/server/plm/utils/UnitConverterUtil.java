package com.erp.server.plm.utils;

public final class UnitConverterUtil {

    private UnitConverterUtil() {
    }

    // 将毫米转换为点 (points)
    public static float mmToPoints(float mm) {
        return mm * 72 / 25.4f;
    }

    // 将点 (points) 转换为毫米
    public static float pointsToMm(float points) {
        return points * 25.4f / 72;
    }

    // 将毫米转换为像素，DPI 设为 300
    public static int mmToPixels(float mm, int dpi) {
        return Math.round(mm * dpi / 25.4f);
    }

    // 将毫米转换为像素，DPI 设为 300
    public static int mmToPixelsRate(float mm, int dpi, float rate) {
        return Math.round(mm * dpi / 25.4f * rate);
    }
}
