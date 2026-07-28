package com.sdk.wms.aiya.enums;

import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import lombok.Getter;

import java.util.Arrays;

/**
 * AIYA（爱亚）海外仓枚举集合。
 * <p>
 * 参照 {@code WegoEnums} 搭建。
 * TODO：{@link InventoryTypeEnum} 取值仍为占位，需按 AIYA 官方文档核对。
 */
@Getter
public enum AiyaEnums {
    ;

    /**
     * AIYA 2C 出库单状态枚举与 ERP {@link SoB2cBillStatusEnum} 的映射。
     * <p>
     * 以爱亚开放平台查询响应字段 {@code status} 为准（2026-07-28 对照官方文档）：
     * <ul>
     *     <li>{@code VALID}（有效）→ {@link SoB2cBillStatusEnum#ENUM_SHIPPED}（有效出库单，含已发运同步）；</li>
     *     <li>{@code HELD}（锁住/暂挂）→ {@link SoB2cBillStatusEnum#ENUM_EXCEPTION}；</li>
     *     <li>{@code CANCELLED}（已取消）→ {@link SoB2cBillStatusEnum#ENUM_DISUSE}（拦截成功终态）。</li>
     * </ul>
     * 方案文档字母码 A/B/C/D 是数大臣业务处理说明，不是网关真实枚举，已废弃。
     */
    @Getter
    public enum OrderStatusEnum {
        VALID("VALID", "有效", SoB2cBillStatusEnum.ENUM_SHIPPED),
        HELD("HELD", "锁住", SoB2cBillStatusEnum.ENUM_EXCEPTION),
        CANCELLED("CANCELLED", "已取消", SoB2cBillStatusEnum.ENUM_DISUSE),
        ;

        private final String code;
        private final String name;
        private final SoB2cBillStatusEnum erpSoStatus;

        OrderStatusEnum(String code, String name, SoB2cBillStatusEnum erpSoStatus) {
            this.code = code;
            this.name = name;
            this.erpSoStatus = erpSoStatus;
        }

        /**
         * 根据 AIYA {@code status} 获取对应 ERP 订单状态码。
         *
         * @param code AIYA status，如 {@code CANCELLED}
         * @return ERP 状态码；未匹配时返回空串（调用方应跳过推送）
         */
        public static String getErpOrderStatus(String code) {
            return Arrays.stream(OrderStatusEnum.values())
                    .filter(item -> item.getCode().equalsIgnoreCase(code))
                    .findFirst()
                    .map(OrderStatusEnum::getErpSoStatus)
                    .map(SoB2cBillStatusEnum::getCode)
                    .orElse("");
        }

        /**
         * 根据 AIYA {@code status} 获取中文名称。
         *
         * @param code AIYA status
         * @return 中文名称；未匹配时返回 {@code null}
         */
        public static String getName(String code) {
            return Arrays.stream(OrderStatusEnum.values())
                    .filter(item -> item.getCode().equalsIgnoreCase(code))
                    .findFirst()
                    .map(OrderStatusEnum::getName)
                    .orElse(null);
        }

        /**
         * 是否为拦截/取消终态（{@code CANCELLED}）。
         */
        public static boolean isCancelled(String code) {
            return CANCELLED.getCode().equalsIgnoreCase(code);
        }

        /**
         * 判断状态是否属于需要人工介入排查的异常态（锁住）。
         *
         * @param code AIYA status
         * @return 是否为异常态
         */
        public static boolean isException(String code) {
            return HELD.getCode().equalsIgnoreCase(code);
        }
    }

    /**
     * AIYA 库存类型枚举（占位）。
     * <p>TODO：取值需按 AIYA 官方文档核对。
     */
    @Getter
    public enum InventoryTypeEnum {
        C2("0", "2C库存"),
        B2("1", "2B库存"),
        DEFECTIVE("3", "不良品"),
        ;

        private final String code;
        private final String name;

        InventoryTypeEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }

        /**
         * 判断给定的 AIYA {@code inventoryType} 值是否为不良品。
         *
         * @param code AIYA inventoryType 转字符串后的值
         * @return 是否为不良品库存类型
         */
        public static boolean isDefective(String code) {
            return DEFECTIVE.getCode().equals(code);
        }
    }
}
