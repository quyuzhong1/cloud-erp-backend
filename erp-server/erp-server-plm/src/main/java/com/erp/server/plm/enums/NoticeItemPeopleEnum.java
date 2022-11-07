package com.erp.server.plm.enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知的项目人员 枚举
 *
 * @Classname NoticeItemPeopleEnum
 * @Description TODO
 * @Date 2022-11-07 12:04
 * @Created by yl
 */
public enum NoticeItemPeopleEnum {

    ITEM_MANAGER("projectCharge", "项目经理"),
    PRODUCT_MANAGER("productCharge", "产品经理"),
    TASK_CHARGE("taskCharge", "任务负责人");

    private String flag;
    private String name;

    NoticeItemPeopleEnum(String flag, String name) {
        this.flag = flag;
        this.name = name;
    }

    public String getFlag() {
        return flag;
    }

    public String getName() {
        return name;
    }

    public static String getName(String flag) {
        for (NoticeItemPeopleEnum item : NoticeItemPeopleEnum.values()) {
            if (flag.equals(item.getFlag())) {
                return item.getName();
            }
        }
        return "";
    }

    public static String getNameByFlags(String flag, String splitFlag) {
        String flags[] = flag.split(splitFlag);
        StringBuffer sb = new StringBuffer();
        boolean mark = false;
        for (String flagStr : flags) {
            if (mark) {
                sb.append(",");
            }
            sb.append(NoticeItemPeopleEnum.getName(flagStr));
            mark = true;
        }
        return sb.toString();
    }

    public static List<Map> getAll() {
        List<Map> resultList = new ArrayList<>();
        for (NoticeItemPeopleEnum item : NoticeItemPeopleEnum.values()) {
            Map<String, String> map = new HashMap<>();
            map.put("flag", item.flag);
            map.put("name", item.name);
            resultList.add(map);
        }
        return resultList;
    }

    public static void main(String[] args) {
        System.out.println(NoticeItemPeopleEnum.getAll());
    }

}
