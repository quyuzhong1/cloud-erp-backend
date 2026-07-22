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
     * 按《爱亚海外仓对接方案文档》6.3.3「4、爱亚出库单查询」的字母状态码 A/B/C/D 落地：
     * <ul>
     *     <li>{@code A}（已出库）→ {@link SoB2cBillStatusEnum#ENUM_SHIPPED}；</li>
     *     <li>{@code B}（已取消）→ {@link SoB2cBillStatusEnum#ENUM_DISUSE}；</li>
     *     <li>{@code C}（库存不足）→ {@link SoB2cBillStatusEnum#ENUM_EXCEPTION}；</li>
     *     <li>{@code D}（锁住）→ {@link SoB2cBillStatusEnum#ENUM_EXCEPTION}（"锁住"完整语义未展开，暂按异常态处理）。</li>
     * </ul>
     * TODO：四个状态码的完整语义（尤其 D-锁住是被谁锁住、是否会自动解锁）与是否还有其它未列出的状态码，
     * 均未见真实响应样例验证，详见 docs/integrations/aiya-overseas-warehouse/README.md「待产品确认」。
     */
    @Getter
    public enum OrderStatusEnum {
        SHIPPED("A", "已出库", SoB2cBillStatusEnum.ENUM_SHIPPED),
        CANCELLED("B", "已取消", SoB2cBillStatusEnum.ENUM_DISUSE),
        STOCK_INSUFFICIENT("C", "库存不足", SoB2cBillStatusEnum.ENUM_EXCEPTION),
        LOCKED("D", "锁住", SoB2cBillStatusEnum.ENUM_EXCEPTION),
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
         * 根据 AIYA 状态码获取对应 ERP 订单状态码。
         *
         * @param code AIYA orderStatus 字母码，如 "A"
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
         * 根据 AIYA 状态码获取中文名称。
         *
         * @param code AIYA orderStatus 字母码
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
         * 判断状态码是否属于需要人工介入排查的异常态（库存不足/锁住）。
         *
         * @param code AIYA orderStatus 字母码
         * @return 是否为异常态
         */
        public static boolean isException(String code) {
            return STOCK_INSUFFICIENT.getCode().equalsIgnoreCase(code) || LOCKED.getCode().equalsIgnoreCase(code);
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
