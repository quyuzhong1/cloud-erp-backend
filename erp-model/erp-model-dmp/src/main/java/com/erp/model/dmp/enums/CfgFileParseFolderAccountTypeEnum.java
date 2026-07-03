package com.erp.model.dmp.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 月结文件解析配置文件夹账号类型枚举。
 *
 * @author jack
 * @since 2026-06-29
 */
public enum CfgFileParseFolderAccountTypeEnum implements EnumMessage {
    PLATFORM_THIRD_WAREHOUSE("platformThirdWarehouse", "三方仓账号"),
    PLATFORM_SHOP("platformShop", "店铺");

    /**
     * 编码。
     */
    @EnumValue
    @JsonValue
    private final String code;
    /**
     * 名称。
     */
    private final String name;

    CfgFileParseFolderAccountTypeEnum(String code, String name) {
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

    /**
     * 根据编码获取名称。
     *
     * @param code 编码
     * @return 名称
     */
    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (CfgFileParseFolderAccountTypeEnum item : CfgFileParseFolderAccountTypeEnum.values()) {
            if (code.equals(item.getCode())) {
                return item.getName();
            }
        }
        return "";
    }
}
