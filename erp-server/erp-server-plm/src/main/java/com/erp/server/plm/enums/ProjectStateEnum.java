package com.erp.server.plm.enums;

/**
 * @Classname 项目状态
 * @Description TODO
 * @Date 2022-09-21 11:12
 * @Created by yl
 */
public enum ProjectStateEnum {

    NOT_START(0, "未启动"),
    YES_START(1, "已启动"),
    ING(2, "进行中"),
    FINISH(3, "已完成"),
    STOP(4, "已终止");

    private Integer state;
    private String name;


    ProjectStateEnum(Integer state, String name) {
        this.state = state;
        this.name = name;
    }

    public Integer getState() {
        return state;
    }
    public String getName() {
        return name;
    }

    public static String getName(Integer code) {
        for (ProjectStateEnum state : ProjectStateEnum.values()) {
            if (code.equals(state.getState())) {
                return state.getName();
            }
        }
        return "";
    }
}
