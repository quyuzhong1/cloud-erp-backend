package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author Cloud
 * @version 1.0

 * @date 2023/8/11 11:05
 */
public enum OperationTypeEnum {

    ADD("add", "添加"),
    SUBMIT("submit", "提交操作"),
    UPDATE("update", "更新操作"),
    APPROVE_PASS("approve_pass", "单据审核通过操作"),
    APPROVE_REJECT("approve_reject", "单据审核不通过操作"),
    DISAPPROVE("disapprove", "反审核"),
    CANCEL_PROCESS("cancel_process", "撤回流程操作"),
    DISABLED("disabled", "启用停用操作"),

    INVALID("invalid", "作废操作"),
    UN_INVALID("unInvalid", "反作废操作"),


    DELETE("delete", "删除操作"),
    PERMISSION("permission", "设置权限操作"),
    UPDATE_STATUS("update_status", "状态变更操作"),
    GENERATE("generate", "下推操作"),
    REGENERATE("regenerate", "重推操作"),

    CONFIRM("confirm", "确认操作"),
    CANCEL_CONFIRM("cancelConfirm", "取消确认操作"),
    RECEIVE("receive", "单据签收"),
    DECLARE_RULE("declareRule", "申报规则匹配"),
    MANUAL_FINISH("manualFinish", "手动完结"),

    EXECUTE("execute", "执行"),

    LOCKING("locking", "锁定"),
    ;
    @JsonValue
    @EnumValue
    private String status;
    private String name;

    OperationTypeEnum(String status, String name) {
        this.status = status;
        this.name = name;
    }

    public static String approveStatus(ApproveStatusEnum approveStatus) {
        if (Objects.nonNull(approveStatus)) {
            switch (approveStatus) {
                case APPROVE:
                    return OperationTypeEnum.APPROVE_PASS.getName();
                case REJECT:
                    return OperationTypeEnum.APPROVE_REJECT.getName();
                default:
                    return null;
            }
        }
        return null;
    }

    public String getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public static String getName(String state) {
        if (StringUtils.isNotBlank(state)) {
            for (OperationTypeEnum item : OperationTypeEnum.values()) {
                if (state.equals(item.getStatus())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static OperationTypeEnum getByCode(String code) {
        return Arrays.stream(OperationTypeEnum.values())
                .filter(e -> e.getStatus().equals(code))
                .findFirst()
                .orElse(null);
    }
}
