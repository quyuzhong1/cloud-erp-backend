package com.erp.model.tms.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 偏远邮编明细表 匹配类型dict_basic表matchType 枚举
 * </p>
 *
 * @author jack
 * @since 2024-11-29 15:36:59
 */
public enum RemotePostcodeDetailMatchTypeEnum implements EnumMessage {
	PRECISEMATCH("preciseMatch", "精准匹配"),
	PREFIXMATCH("prefixMatch", "匹配前缀"),
	SUFFIXMATCH("suffixMatch", "匹配后缀"),
	FUZZYMATCH("fuzzyMatch", "模糊匹配"),
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

    RemotePostcodeDetailMatchTypeEnum(String code, String name) {
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
        for (RemotePostcodeDetailMatchTypeEnum statusEnum : RemotePostcodeDetailMatchTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
