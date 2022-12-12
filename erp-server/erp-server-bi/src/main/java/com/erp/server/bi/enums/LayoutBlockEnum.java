package com.erp.server.bi.enums;

/**
 * @Classname 布局块枚举
 * @Description TODO
 * @Date 2022-09-29 14:13
 * @Created by yl
 */
public enum LayoutBlockEnum {

    QUARTER("quarter", "1/4块", 4),
    HALF("half", "1/2块",2),
    SIXTH("sixth", "1/6块",6),
    SINGLE("single", "1块",1);

    private String blockNo;

    private String name;

    private Integer count;


    LayoutBlockEnum(String blockNo, String name, Integer count) {
        this.blockNo = blockNo;
        this.name = name;
        this.count = count;
    }

    public Integer getCount(){ return count; }
    public String getBlockNo() {
        return blockNo;
    }

    public String getName() {
        return name;
    }




}
