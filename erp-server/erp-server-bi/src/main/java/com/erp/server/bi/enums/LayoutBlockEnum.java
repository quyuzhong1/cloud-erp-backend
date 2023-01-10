package com.erp.server.bi.enums;

/**
 * @Classname 布局块枚举
 * @Description TODO
 * @Date 2022-09-29 14:13
 * @Created by yl
 */
public enum LayoutBlockEnum {

    QUARTER("L04", "1/4块", 4),
    HALF("L02", "1/2块",2),
    THIRD("L03", "1/3块",3),
    SIXTH("L06", "1/6块",6),
    SINGLE("L01", "1块",1);

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


    public static Integer getCount(String blockNo) {
        for (LayoutBlockEnum item : LayoutBlockEnum.values()) {
            if (blockNo.equals(item.getBlockNo())) {
                return item.getCount();
            }
        }
        return 0;
    }

}
