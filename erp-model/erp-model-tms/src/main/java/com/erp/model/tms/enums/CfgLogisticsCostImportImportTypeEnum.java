package com.erp.model.tms.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 费用项配置 导入处理 枚举
 * </p>
 *
 * @author jack
 * @since 2026-01-20 17:53:25
 */
public enum CfgLogisticsCostImportImportTypeEnum implements EnumMessage {
	IMPORT_UPDATE("import_update", "导入更新"),
	IMPORT_ADD_OLD("import_add_old", "导入新增(按原单)"),
    // 审查说明：按新单新增会创建新物流单/费用单，当前费用配置入口暂不对用户开放。
	IMPORT_ADD_NEW("import_add_new", "导入新增(按新单)"),
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

    CfgLogisticsCostImportImportTypeEnum(String code, String name) {
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

    public static String getCode(String name) {
        if (StringUtils.isBlank(name)) {
            return "";
        }
        for (CfgLogisticsCostImportImportTypeEnum statusEnum : CfgLogisticsCostImportImportTypeEnum.values()) {
            if (name.equals(statusEnum.getName())) {
                return statusEnum.getCode();
            }
        }
        return "";
    }
    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (CfgLogisticsCostImportImportTypeEnum statusEnum : CfgLogisticsCostImportImportTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
