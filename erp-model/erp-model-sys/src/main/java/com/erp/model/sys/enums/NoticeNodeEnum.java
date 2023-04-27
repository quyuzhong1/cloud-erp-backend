package com.erp.model.sys.enums;

/**
 * @author Administrator
 * @Classname NoticeEnum
 * @Description TODO
 * @Date 2022-11-11 10:57
 * @Created by yl
 */
public enum NoticeNodeEnum {
    QC_NEW_PRODUCT("qcResultNewProduct", "质检通知-新品"),
    QC_OLD_PRODUCT("qcResultOldProduct", "质检通知-老品");






    private String code;
    private String name;

    NoticeNodeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }


    public static String getName(String code) {
        for (NoticeNodeEnum item : NoticeNodeEnum.values()) {
            if (code.equals(item.getCode())) {
                return item.getName();
            }
        }
        return "";
    }
}
