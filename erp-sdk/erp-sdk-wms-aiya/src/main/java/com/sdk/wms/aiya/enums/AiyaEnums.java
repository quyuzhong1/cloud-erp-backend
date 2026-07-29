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
     * AIYA 2C 出库单「阶段」枚举（响应字段 {@code stage}，2026-07-29 联调真实报文确认存在）。
     * <p>
     * {@code status=VALID} 只代表订单本身有效/未被拦截，<b>不代表已实际出库</b>——
     * 联调实测同一单 {@code status=VALID} 时 {@code stage} 会经历 {@code PICKING}/{@code PACKING}
     * 等中间阶段，只有 {@code stage=SHIPPED} 才是已发货实锤（该值来自实测样本核对，其余阶段码
     * 由爱亚提供但未逐一验证语义，仅供参考，禁止假设其代表已发货）。
     * <p>
     * 因此 {@code VALID} 判定是否等价 ERP 已发货，须与 {@link #isShipped(String)} 联合判断，
     * 具体见 {@code AiyaOutBoundDmpHandler}。
     */
    public enum StageEnum {
        /** 已发货（2026-07-29 实测确认，唯一已验证代表"已发货"的阶段码） */
        SHIPPED("SHIPPED"),
        /** 以下均为爱亚提供的阶段码原文，语义未逐一核实，仅供排查参考 */
        DUE_OUT("DUE_OUT"),
        ALLOCATED("ALLOCATED"),
        PICKING("PICKING"),
        PICKED("PICKED"),
        PACKING("PACKING"),
        PACKED("PACKED"),
        SHIPPING("SHIPPING"),
        CLOSED("CLOSED"),
        PICK("PICK"),
        PACK("PACK"),
        PARTIALLY_ALLOCATED("PARTIALLY_ALLOCATED"),
        OPEN("OPEN"),
        CREATED("CREATED"),
        ROUTING("ROUTING"),
        ;

        @Getter
        private final String code;

        StageEnum(String code) {
            this.code = code;
        }

        /**
         * 判断 {@code stage} 是否为已发货唯一确认值 {@code SHIPPED}。
         *
         * @param stage AIYA 响应 {@code stage} 原文
         * @return 是否已发货
         */
        public static boolean isShipped(String stage) {
            return SHIPPED.getCode().equalsIgnoreCase(stage);
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
