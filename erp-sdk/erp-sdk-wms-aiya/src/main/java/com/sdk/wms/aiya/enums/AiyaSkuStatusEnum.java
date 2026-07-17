package com.sdk.wms.aiya.enums;

import lombok.Getter;

/**
 * AIYA {@code GLINK_QUERY_ITEM_NOTIFY}（商品注册/查询）接口返回的 SKU 状态枚举。
 * <p>
 * 对应《爱亚海外仓对接方案文档》商品注册/查询接口 {@code status} 字段：
 * <ul>
 *     <li>{@code Active} - 商品启用状态</li>
 *     <li>{@code Inactive} - 商品停用状态</li>
 * </ul>
 * 文档处理策略：两种状态都拉取，本枚举负责标识两种状态均需同步到 OMS 未匹配表；
 * 爱亚已停用（{@code Inactive}）时，若该 SKU 已建立映射关系，由
 * {@code ListingInfoService#reconcileWarehouseSkuSnapshot} 全量快照回收逻辑将对应
 * {@code sku_mapping.status} 置为禁用（不会自动重新启用，需人工确认后手动恢复）。
 * <p>
 * 2026-07-16 联调实测确认：真实响应 {@code status} 字段值为 {@code "Active"}（首字母大写，与文档一致）。
 * TODO：目前仅实测验证过 {@code Active}，{@code Inactive} 的真实大小写仍未见过真实响应样例，
 * 暂按文档原样保留；{@link #needSync} 已用 {@code equalsIgnoreCase} 做大小写兼容，即使实际大小写
 * 有出入也不影响同步判断。
 */
@Getter
public enum AiyaSkuStatusEnum {

    ACTIVE("Active", "启用"),
    INACTIVE("Inactive", "停用");

    private final String code;
    private final String desc;

    AiyaSkuStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据 code 判断是否需要同步。
     * <p>
     * 按文档"两种状态都拉取"的策略，只要能匹配到已知状态（Active/Inactive）即需同步；
     * code 为空或不匹配任何已知枚举值时返回 false（视为异常/未知状态，不同步）。
     */
    public static boolean needSync(String code) {
        if (code == null || code.isEmpty()) {
            return false;
        }
        for (AiyaSkuStatusEnum e : values()) {
            if (e.code.equalsIgnoreCase(code)) {
                return true;
            }
        }
        return false;
    }
}
