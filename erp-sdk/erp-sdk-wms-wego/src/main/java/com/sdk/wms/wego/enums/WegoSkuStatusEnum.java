package com.sdk.wms.wego.enums;

import lombok.Getter;

/**
 * WEGO product.search 接口返回的 SKU 状态枚举。
 * <p>
 * 对应 WEGO 接口文档 {@code status} 字段：
 * <ul>
 *     <li>0 - 草稿：未提交，ERP 不同步</li>
 *     <li>1 - 已提交：正常上架，需同步</li>
 *     <li>4 - 废弃：已下架，需同步（用于核销已有对照关系）</li>
 * </ul>
 */
@Getter
public enum WegoSkuStatusEnum {

    DRAFT(0, "草稿"),
    SUBMITTED(1, "已提交"),
    ABANDONED(4, "废弃");

    private final int code;
    private final String desc;

    WegoSkuStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据 code 判断是否需要同步，code 为 null 或不匹配任何枚举值时返回 false。
     */
    public static boolean needSync(Integer code) {
        if (code == null) {
            return false;
        }
        for (WegoSkuStatusEnum e : values()) {
            if (e.code == code) {
                return e.needSync();
            }
        }
        return false;
    }

    /**
     * 判断该状态是否需要同步到 ERP（已提交或废弃）。
     */
    public boolean needSync() {
        return this == SUBMITTED || this == ABANDONED;
    }
}
