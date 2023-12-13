package com.cloud.erp.gateway.option;

/**
 * The LogType Of Plugin Filter
 * @Author Luo_WG
 * @Date 2023/12/6 18:28
 **/
public enum GatewayLogTypeEnum {

    /**
     * Gateway LogType all
     */
    ALL("all"),
    /**
     * Gateway LogType configure
     */
    CONFIGURE("configure"),
    /**
     * Gateway LogType service
     */
    SERVICE("service"),
    /**
     * Gateway LogType path
     */
    PATH("path"),
    ;

    private String type;

    GatewayLogTypeEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
