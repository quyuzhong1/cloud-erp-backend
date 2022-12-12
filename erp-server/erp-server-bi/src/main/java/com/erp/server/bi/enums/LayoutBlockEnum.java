package com.erp.server.bi.enums;

/**
 * @Classname  布局块枚举
 * @Description TODO
 * @Date 2022-09-29 14:13
 * @Created by yl
 */
public enum LayoutBlockEnum {

    QUARTER("quarter","1/4块"),
    HALF("half","1/2块"),
    SIXTH("sixth","1/6块"),
    SINGLE("single","1块");

    private String blockNo;

    private String name;


    LayoutBlockEnum(String blockNo, String name) {
        this.blockNo = blockNo;
        this.name = name;
    }

    public String getBlockNo() {
        return blockNo;
    }

    public String getName() {
        return name;
    }


}
