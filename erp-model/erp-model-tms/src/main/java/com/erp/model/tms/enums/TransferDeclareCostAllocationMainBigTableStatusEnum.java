package com.erp.model.tms.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 中转费用分摊主表 大表状态 枚举
 * </p>
 *
 * @author shukai
 * @since 2024-12-06 11:37:40
 */
public enum TransferDeclareCostAllocationMainBigTableStatusEnum implements EnumMessage {
	TODO("toDo", "待生成"),
	DONE("done", "已生成"),
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

    TransferDeclareCostAllocationMainBigTableStatusEnum(String code, String name) {
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
        for (TransferDeclareCostAllocationMainBigTableStatusEnum statusEnum : TransferDeclareCostAllocationMainBigTableStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
