package com.erp.server.bi.enums;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/15 17:48
 */
public enum ReturnOrderEnum {

    PENDING(2, "待处理"),
    REFUNDED(3,"已退款"),
    RESEND(4, "已重发"),
    VOIDOMPLETEDED(5, "已完成"),
    VOIDED(6, "已作废");

        private Integer code;
        private String name;

        ReturnOrderEnum(Integer code, String name) {
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
            for (ReturnOrderEnum state : ReturnOrderEnum.values()) {
                if (code.equals(state.getCode())) {
                    return state.getName();
                }
            }
            return "";
        }
}
