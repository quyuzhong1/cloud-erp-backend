package com.erp.server.plm.enums;

/**
 * @Classname ProductInfoStateEnum
 * @Description TODO
 * @Date 2022-09-19 18:17
 * @Created by yl
 */
public enum ProductInfoStateEnum {

    TO_BE_RELEASED(0,"待发布"),

    NOT_START(0,"未开始"),

    WAIT_APPROVAL(0,"待审核"),

    ING(1,"进行中"),

    APPROVAL_ING(1,"审核中"),

    APPROVAL_NO_PASS(2,"审核不通过"),

    FINISH(3,"已完成"),

    WAIT_CONFIRM(3,"完成待审核"),

    APPROVAL_PASS(3,"审核通过");


    private Integer colourState;

    private String name;

    ProductInfoStateEnum(Integer colourState, String name) {
        this.colourState = colourState;
        this.name = name;
    }

    public Integer getColourState() {
        return colourState;
    }

    public String getName() {
        return name;
    }


}
