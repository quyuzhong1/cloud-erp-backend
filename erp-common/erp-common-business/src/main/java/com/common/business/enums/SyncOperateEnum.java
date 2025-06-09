package com.common.business.enums;

/**
 * @author Will
 * @version 1.0

 * @date 2023/4/11 17:13
 */
public enum SyncOperateEnum {

    OPERATE_ADD("operateAdd", "","新增"),
    OPERATE_UPDATE("operateUpdate", "","修改"),
    OPERATE_APPROVE("operateApprove", "","审核"),
    OPERATE_DISAPPROVE("operateDisApprove", "","反审核"),
    OPERATE_ENABLE("operateEnable", "Enable","启用"),
    OPERATE_DISABLE("operateDisable", "Forbid","禁用"),
    OPERATE_INVALID("operateInvalid", "Cancel","作废"),
    OPERATE_UN_INVALID("operateUnInvalid", "Uncancel","反作废"),
    OPERATE_DELETE("operateDelete", "","删除"),
    OPERATE_SUB_EFFECTIVE("operateSubEffective", "SubEffective","取消明细禁用"),
    OPERATE_SUB_UN_EFFECTIVE("operateSubUnEffective", "SubUnEffective","明细禁用"),
    OPERATE_UNDO_TO_PLAN_CONFIRM("operateUndoToPlanConfirm", "UndoToPlanConfirm","反执行至计划确认"),
    OPERATE_SYNC_ERROR("operateSyncError", "","同步错误任务"),

    ;
    private String code;

    private String kingdeeParam;

    private String desc;

    SyncOperateEnum(String code, String kingdeeParam, String desc) {
        this.code = code;
        this.kingdeeParam = kingdeeParam;
        this.desc = desc;
    }

    public static SyncOperateEnum getByCode(String code) {
        for (SyncOperateEnum operateEnum : values()) {
            if (operateEnum.getCode().equals(code)) {
                return operateEnum;
            }
        }
        return null;
    }

    public String getCode() {
        return code;
    }
    public String getKingdeeParam() {
        return kingdeeParam;
    }
    public String getDesc() {
        return desc;
    }

    public static String getNameByCode(String code) {
        SyncOperateEnum[] enums = values();
        for (SyncOperateEnum operateEnum : enums) {
            if (operateEnum.getCode().equals(code)) {
                return operateEnum.getKingdeeParam();
            }
        }
        return null;
    }

    public static String getDescByCode(String code) {
        SyncOperateEnum[] enums = values();
        for (SyncOperateEnum operateEnum : enums) {
            if (operateEnum.getCode().equals(code)) {
                return operateEnum.getDesc();
            }
        }
        return null;
    }
}
