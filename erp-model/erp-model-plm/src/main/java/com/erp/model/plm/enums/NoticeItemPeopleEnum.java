package com.erp.model.plm.enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知的项目人员 枚举
 *
 * @Classname NoticeItemPeopleEnum

 * @Date 2022-11-07 12:04
 * @Created by yl
 */
public enum NoticeItemPeopleEnum {

    ITEM_MANAGER("projectCharge", "项目经理"),
    PRODUCT_MANAGER("productCharge", "产品经理"),
    AUDITOR("auditor", "审核人"),
    TASK_CHARGE("taskCharge", "任务负责人"),
    FOLLOWER("concern", "关注人");

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
        StringBuilder sb = new StringBuilder();
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

    public static List<Map<String,String>> getAll() {
        List<Map<String,String>> resultList = new ArrayList<>();
        for (NoticeItemPeopleEnum item : NoticeItemPeopleEnum.values()) {
            Map<String, String> map = new HashMap<>();
            map.put("flag", item.flag);
            map.put("name", item.name);
            resultList.add(map);
        }
        return resultList;
    }


}
