package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * SKU 对照表 - 映射关系状态。
 * <p>
 * 用于标识一条 {@code sku_mapping} 记录当前是否可被业务流程使用：
 * {@link #ENABLE} 启用，正常参与库存SKU解析；{@link #DISABLE} 禁用，业务流程不得使用该映射
 * （例如源端SKU已停用或已从三方仓消失时，由回收逻辑自动置为禁用，需人工确认后才能重新启用）。
 *
 * @author yl
 */
public enum SkuMappingStatusEnum implements EnumMessage {
    ENABLE("enable", "启用"),
    DISABLE("disable", "禁用"),
    ;

    /**
     * 编码
     */
    @EnumValue
    @JsonValue
    private String code;
    /**
     * 名称
     */
    private String name;

    SkuMappingStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (SkuMappingStatusEnum statusEnum : SkuMappingStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }

    public static SkuMappingStatusEnum getByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (SkuMappingStatusEnum statusEnum : SkuMappingStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum;
            }
        }
        return null;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }
}
