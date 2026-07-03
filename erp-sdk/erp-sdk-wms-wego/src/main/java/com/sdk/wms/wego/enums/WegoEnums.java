package com.sdk.wms.wego.enums;

import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import lombok.Getter;

import java.util.Arrays;

/**
 * WEGO 海外仓枚举集合。
 */
@Getter
public enum WegoEnums {
    ;

    /**
     * WEGO 2C 出库单状态枚举（来自 WEGO 官方文档 orderStatus 字段）。
     * <p>
     * 状态码与 ERP {@link SoB2cBillStatusEnum} 的映射关系：
     * <ul>
     *   <li>0  草稿：不纳入映射（跳过不推送）</li>
     *   <li>1  提交失败 → exception</li>
     *   <li>2  已提交   → waitShipped</li>
     *   <li>3  拣货中   → waitShipped</li>
     *   <li>4  已拣货   → waitShipped</li>
     *   <li>10 已出库   → shipped</li>
     *   <li>11 已签收   → shipped</li>
     *   <li>13 出库异常 → exception</li>
     *   <li>15 已取消   → disuse</li>
     * </ul>
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
         * 根据 WEGO 状态码（Integer 转 String）获取对应 ERP 订单状态码。
         *
         * @param code WEGO orderStatus 转字符串后的值，如 "10"
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
         * 根据 WEGO 状态码获取中文名称。
         *
         * @param code WEGO orderStatus 转字符串后的值
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
}
