package com.erp.model.dmp.enums;

/**
 * webhook服务列表
 * @author zdy
 * @version 1.0

 * @date 2024/12/11 18:41
 */
public enum WebhookServiceEnum {

    TRACK123("track123", "track123", "物流轨迹查询"),
    ;

    private String code;

    private String name;

    private String desc;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    WebhookServiceEnum(String code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public static WebhookServiceEnum getByCode(String code) {
        WebhookServiceEnum[] values = values();
        for (WebhookServiceEnum value : values) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    public static WebhookServiceEnum getByName(String name) {
        WebhookServiceEnum[] values = values();
        for (WebhookServiceEnum value : values) {
            if (value.name.equals(name)) {
                return value;
            }
        }
        return null;
    }

}
