package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 收发差异
 * @Author Luo_WG
 * @Date 2023/10/31 16:01
 **/
public enum WmsFileTypeEnum implements EnumMessage {
    REVIEW_REPORT("reviewReport", "评审报告"),
    MANUFACTURING_REPORT("manufacturingReport", "量产报告"),
    CATEGORY_GENERAL_STANDARD("categoryGeneralStandard", "品类通用标准"),
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

    WmsFileTypeEnum(String code, String name) {
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
        if (StringUtils.isNotBlank(code)) {
            for (WmsFileTypeEnum item : WmsFileTypeEnum.values()) {
                if (code.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

}
