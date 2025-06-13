package com.erp.model.workflow.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 查询option配置表(数大臣单据字段) 拓展字典 枚举
 * </p>
 *
 * @author jack
 * @since 2025-06-12
 */
public enum CfgQueryOptionExtendTypeEnum implements EnumMessage {
	NOTICENODE("noticeNode", "通知节点"),
    FIELDMAP("fieldMap", "字段映射"),
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

    CfgQueryOptionExtendTypeEnum(String code, String name) {
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
        for (CfgQueryOptionExtendTypeEnum statusEnum : CfgQueryOptionExtendTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
