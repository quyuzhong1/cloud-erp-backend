package com.erp.model.wms.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 数据对比任务 任务状态 枚举
 * </p>
 *
 * @author shukai
 * @since 2024-03-20 17:21:53
 */
public enum WmsDataCompareTaskStatusEnum implements EnumMessage {
	INIT("init", "初始"),
	DOING("doing", "进行中"),
	FINISH("finish", "已完成"),
	ERROR("error", "异常"),
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

    WmsDataCompareTaskStatusEnum(String code, String name) {
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
        for (WmsDataCompareTaskStatusEnum statusEnum : WmsDataCompareTaskStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
