package com.sdk.wms.aiya.enums;

import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import lombok.Getter;

import java.util.Arrays;

/**
 * AIYA（爱亚）海外仓枚举集合（骨架）。
 * <p>
 * 参照 {@code WegoEnums} 搭建。
 * TODO：以下状态码与含义为占位映射，需按 AIYA 官方文档核对 orderStatus / inventoryType 的取值后调整。
 */
@Getter
public enum AiyaEnums {
    ;

    /**
     * AIYA 2C 出库单状态枚举与 ERP {@link SoB2cBillStatusEnum} 的映射（占位）。
     * <p>TODO：状态码需按 AIYA 官方文档核对。
     */
    @Getter
    public enum OrderStatusEnum {
        SUBMIT_FAIL("1", "提交失败", SoB2cBillStatusEnum.ENUM_EXCEPTION),
        SUBMITTED("2", "已提交", SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED),
        PICKING("3", "拣货中", SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED),
        PICKED("4", "已拣货", SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED),
        SHIPPED("10", "已出库", SoB2cBillStatusEnum.ENUM_SHIPPED),
        SIGNED("11", "已签收", SoB2cBillStatusEnum.ENUM_SHIPPED),
        OUTBOUND_EXCEPTION("13", "出库异常", SoB2cBillStatusEnum.ENUM_EXCEPTION),
        CANCELLED("15", "已取消", SoB2cBillStatusEnum.ENUM_DISUSE),
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
         * 根据 AIYA 状态码（Integer 转 String）获取对应 ERP 订单状态码。
         *
         * @param code AIYA orderStatus 转字符串后的值，如 "10"
         * @return ERP 状态码；未匹配时返回空串（调用方应跳过推送）
         */
        public static String getErpOrderStatus(String code) {
            return Arrays.stream(OrderStatusEnum.values())
                    .filter(item -> item.getCode().equals(code))
                    .findFirst()
                    .map(OrderStatusEnum::getErpSoStatus)
                    .map(SoB2cBillStatusEnum::getCode)
                    .orElse("");
        }

        /**
         * 根据 AIYA 状态码获取中文名称。
         *
         * @param code AIYA orderStatus 转字符串后的值
         * @return 中文名称；未匹配时返回 {@code null}
         */
        public static String getName(String code) {
            return Arrays.stream(OrderStatusEnum.values())
                    .filter(item -> item.getCode().equals(code))
                    .findFirst()
                    .map(OrderStatusEnum::getName)
                    .orElse(null);
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
