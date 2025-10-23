package com.erp.model.plm.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 模具监控 返还状态 枚举
 * </p>
 *
 * @author jack
 * @since 2025-10-22 16:35:38
 */
public enum MoldMonitorReturnStatusEnum implements EnumMessage {
	UNDERACHIEVED("underachieved", "未达量"),
	NOTRETURNED("notReturned", "未返"),
	RETURNED("returned", "已返"),
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

    MoldMonitorReturnStatusEnum(String code, String name) {
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
        for (MoldMonitorReturnStatusEnum statusEnum : MoldMonitorReturnStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }

    public static List<String> getStatusList() {
        return Arrays.stream(MoldMonitorReturnStatusEnum.values()).map(MoldMonitorReturnStatusEnum::getCode).collect(Collectors.toList());
    }
}
