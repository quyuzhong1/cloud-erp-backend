package com.erp.server.bi.enums;

/**
 * @author Will
 * @version 1.0
 * @description: 订单审核状态
 * @date 2022/12/15 17:24
 */
public enum OrderStateEnum {

        DISTRIBUTION(2, "配货中"),
        SHIPPED(3,"已发货"),
        COMPLETED(4, "已完成"),
        VOIDED(5, "已作废"),
        RETURNGOODS(6, "退货"),
        REFUND(7, "退款");

        private Integer code;
        private String name;

        OrderStateEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        public Integer getCode() {
            return code;
        }
        public String getName() {
            return name;
        }

        public static String getName(Integer code) {
            for (OrderStateEnum state : OrderStateEnum.values()) {
                if (state.getCode().equals(code)) {
                    return state.getName();
                }
            }
            return "";
        }
    public static Integer getCodeByName(String name) {
        OrderStateEnum[] orderStateEnums = values();
        for (OrderStateEnum orderStateEnum : orderStateEnums) {
            if (orderStateEnum.getName().equals(name)) {
                return orderStateEnum.getCode();
            }
        }
        return null;
    }
}
