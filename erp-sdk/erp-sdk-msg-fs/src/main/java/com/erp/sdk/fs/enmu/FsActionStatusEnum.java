package com.erp.sdk.fs.enmu;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 *
 * </p>
 *
 * @author jack
 * @since 2025-05-19
 */
public enum FsActionStatusEnum implements EnumMessage {
    APPROVED("APPROVED", "已同意"),
    REJECTED("REJECTED", "已拒绝"),
    CANCELLED("CANCELLED", "已撤回"),
    FORWARDED("FORWARDED", "已转交"),
    ROLLBACK("ROLLBACK", "已回退"),
    ADD("ADD", "已加签"),
    DELETED("DELETED", "已删除"),
    PROCESSED("PROCESSED", "已处理"),

//    飞书没有恢复和暂停，因此要采用自定义按钮状态
    CUSTOM("CUSTOM", "自定义按钮状态"),
    SUSPEND("SUSPEND", "暂停操作"),
    RESTORE("RESTORE", "恢复操作"),

    ;
    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private String code;
    /**
     * 名称
     */
    private String name;

    FsActionStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (FsActionStatusEnum statusEnum : FsActionStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
