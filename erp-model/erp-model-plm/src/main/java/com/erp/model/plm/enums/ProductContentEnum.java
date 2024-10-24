package com.erp.model.plm.enums;

import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ProductContentEnum {
    /**
     * SKU
     */
    SKU("SKU", 1),
    /**
     * EAN
     */
    EAN("EAN", 2),
    /**
     * 品名
     */
    PRODUCT_NAME("PRODUCT_NAME", 3),
    /**
     * 自定义
     */
    CUSTOM("CUSTOM", 4),
    /**
     * MADE_IN_CHINA
     */
    MADE_IN_CHINA("MADE_IN_CHINA", 5),
    /**
     * 日期
     */
    DATE("DATE", 6);

    private final String code;

    private final Integer order;

    public static ProductContentEnum ofCode(String code) {
        return Arrays.stream(ProductContentEnum.values())
                .filter(v -> v.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new ServiceException(ApiError.ERROR_9028));
    }
}
