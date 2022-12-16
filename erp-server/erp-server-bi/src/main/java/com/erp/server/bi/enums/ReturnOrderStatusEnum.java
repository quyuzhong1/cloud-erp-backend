package com.erp.server.bi.enums;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/15 17:48
 */
public enum ReturnOrderStatusEnum {

    PENDING(2, "待处理"),
    REFUNDED(3,"已退款"),
    RESEND(4, "已重发"),
    VOIDOMPLETEDED(5, "已完成"),
    VOIDED(6, "已作废");

        private Integer code;
        private String name;

        ReturnOrderStatusEnum(Integer code, String name) {
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
            for (ReturnOrderStatusEnum state : ReturnOrderStatusEnum.values()) {
                if (code.equals(state.getCode())) {
                    return state.getName();
                }
            }
            return "";
        }

    public static Integer getCodeByName(String name) {
        ReturnOrderStatusEnum[] returnOrderStatusEnums = values();
        for (ReturnOrderStatusEnum returnOrderStatusEnum : returnOrderStatusEnums) {
            if (returnOrderStatusEnum.getName().equals(name)) {
                return returnOrderStatusEnum.getCode();
            }
        }
        return null;
    }
}
