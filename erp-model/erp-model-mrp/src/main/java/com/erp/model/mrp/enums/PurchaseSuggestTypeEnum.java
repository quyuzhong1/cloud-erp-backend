package com.erp.model.mrp.enums;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
/**
 * 采购建议类型
 * @author will
 * @date 2025/1/6 12:14
 */
@Getter
@AllArgsConstructor
public enum PurchaseSuggestTypeEnum implements EnumMessage {
    SPILT_BEFORE("spiltBefore", "拆分前"),
    SPLIT_AFTER("splitAfter", "拆分后"),
    ;
    /**
     * 编码
     */
    private final String code;
    /**
     * 名称
     */
    private final String name;

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
        for (PurchaseSuggestTypeEnum statusEnum : PurchaseSuggestTypeEnum.values()) {
            if (CharSequenceUtil.equals(code,statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
