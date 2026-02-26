package com.erp.model.tms.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 异步任务记录 状态 枚举
 * </p>
 *
 * @author jack
 * @since 2026-01-28 12:16:20
 */
public enum AsyncTaskRecordStatusEnum implements EnumMessage {
    FINISH("finish", "已完成"),
	SUCCESS("success", "成功"),
	PART_SUCCESS("part_success", "部分成功"),
    ING("ing", "进行中"),
    PENDING("pending", "待执行"),
	FAILED("failed", "失败"),
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

    AsyncTaskRecordStatusEnum(String code, String name) {
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
        for (AsyncTaskRecordStatusEnum statusEnum : AsyncTaskRecordStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
