package com.common.business.utils;

import cn.hutool.core.text.CharSequenceUtil;

/**
 * IML 仓库商品条码规则。
 */
public final class ImlBarcodeUtil {

    private ImlBarcodeUtil() {
    }

    public static String buildBarcode(String sku, String ownerCode) {
        sku = CharSequenceUtil.trim(sku);
        ownerCode = CharSequenceUtil.trim(ownerCode);
        if (CharSequenceUtil.isBlank(sku)) {
            return "";
        }
        if (CharSequenceUtil.isBlank(ownerCode)) {
            return sku;
        }
        String ownerPrefix = ownerCode + "-";
        return sku.startsWith(ownerPrefix) ? sku : ownerPrefix + sku;
    }
}
