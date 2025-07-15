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
 * @since 2025-05-12 18:31:25
 */
public enum DepartmentIdTypeEnum implements EnumMessage {
    DEPARTMENTID("department_id", "department_id"),
    OPENDEPARTMENTID("open_department_id", "open_department_id"),
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

    DepartmentIdTypeEnum(String code, String name) {
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
        for (DepartmentIdTypeEnum statusEnum : DepartmentIdTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
