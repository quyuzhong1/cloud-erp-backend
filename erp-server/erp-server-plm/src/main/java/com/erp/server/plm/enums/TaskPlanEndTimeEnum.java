package com.erp.server.plm.enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Classname TaskPlanEndTimeEnum
 * @Description TODO
 * @Date 2022-11-08 15:32
 * @Created by yl
 */
public enum TaskPlanEndTimeEnum {
    TODAY("今天", "today"),
    TOMORROW("明天", "tomorrow"),
    LAST_THREE_DAYS("最近三天", "lastThreeDays"),
    LAST_SEVEN_DAYS("最近七天", "lastSevenDays"),
    LAST_FIFTEEN_DAYS("最近十五天", "lastFifteenDays"),
    LAST_THIRTY_DAYS("最近三十天", "lastThirtyDays"),
    AFTER_THIRTY_DAYS("30天以后", "afterThirtyDays");

    private String name;
    private String flag;

    TaskPlanEndTimeEnum(String name, String flag) {
        this.name = name;
        this.flag = flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFlag() {
        return flag;
    }

    public String getName() {
        return name;
    }


    public static List<Map<String,String>> getAll() {
        List<Map<String,String>> resultList = new ArrayList<>();
        for (TaskPlanEndTimeEnum item : TaskPlanEndTimeEnum.values()) {
            Map<String, String> map = new HashMap<>();
            map.put("flag", item.flag);
            map.put("name", item.name);
            resultList.add(map);
        }
        return resultList;
    }
}
