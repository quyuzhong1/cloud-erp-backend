package com.sdk.wms.aiya.enums;

import lombok.Getter;

/**
 * AIYA product.search 接口返回的 SKU 状态枚举（骨架）。
 * <p>
 * 参照 {@code WegoSkuStatusEnum} 搭建。
 * TODO：取值（0 草稿 / 1 已提交 / 4 废弃）为占位值，需按 AIYA 官方文档核对。
 */
@Getter
public enum AiyaSkuStatusEnum {

    DRAFT(0, "草稿"),
    SUBMITTED(1, "已提交"),
    ABANDONED(4, "废弃");

    private final int code;
    private final String desc;

    AiyaSkuStatusEnum(int code, String desc) {
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
        for (AiyaSkuStatusEnum e : values()) {
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
