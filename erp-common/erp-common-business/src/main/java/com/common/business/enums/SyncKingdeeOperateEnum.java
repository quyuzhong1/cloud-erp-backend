package com.common.business.enums;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/4/11 17:13
 */
public enum SyncKingdeeOperateEnum {

    OPERATE_ADD("operateAdd", "","新增"),
    OPERATE_UPDATE("operateUpdate", "","修改"),
    OPERATE_APPROVE("operateApprove", "","审核"),
    OPERATE_DISAPPROVE("operateDisApprove", "","反审核"),
    OPERATE_ENABLE("operateEnable", "Enable","启用"),
    OPERATE_DISABLE("operateDisable", "Forbid","禁用"),
    OPERATE_INVALID("operateInvalid", "Cancel","作废"),
    OPERATE_UN_INVALID("operateUnInvalid", "Uncancel","反作废"),
    OPERATE_DELETE("operateDelete", "","删除"),
    ;
    private String code;

    private String name;

    private String desc;

    SyncKingdeeOperateEnum(String code, String name,String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }
    public String getName() {
        return name;
    }
    public String getDesc() {
        return desc;
    }

    public static String getNameByCode(String code) {
        SyncKingdeeOperateEnum[] enums = values();
        for (SyncKingdeeOperateEnum operateEnum : enums) {
            if (operateEnum.getCode().equals(code)) {
                return operateEnum.getName();
            }
        }
        return null;
    }

    public static String getDescByCode(String code) {
        SyncKingdeeOperateEnum[] enums = values();
        for (SyncKingdeeOperateEnum operateEnum : enums) {
            if (operateEnum.getCode().equals(code)) {
                return operateEnum.getDesc();
            }
        }
        return null;
    }
}
