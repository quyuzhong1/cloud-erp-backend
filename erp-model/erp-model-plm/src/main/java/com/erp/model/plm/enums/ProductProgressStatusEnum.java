package com.erp.model.plm.enums;

/**
 * 产品进展枚举
 *
 * @Classname
 * @Description TODO
 * @Date 2023-02-23 16:39
 * @Created by yl
 */
public enum ProductProgressStatusEnum {

    NO("no", "暂无"),
    NORMAL("normal", "正常"),
    POSTPONE("postpone", "延期"),
    RISK("risk", "风险");


    private String status;
    private String name;

    ProductProgressStatusEnum(String status, String name) {
        this.status = status;
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public static String getName(String status) {
        for (ProductProgressStatusEnum state : ProductProgressStatusEnum.values()) {
            if (status.equals(state.getStatus())) {
                return state.getName();
            }
        }
        return "";
    }
}
