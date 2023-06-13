package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;


/**
 * <p>
 * 仓库仓位分区类型
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-05-22
 */
public enum WarehouseLocationAreaTypeEnum implements EnumMessage {

    PICK ("pick", "拣货区"),
    DEFECTIVE("defective", "次品区"),
    PACKAGE("package", "包装区"),
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

    WarehouseLocationAreaTypeEnum(String code, String name) {
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
            for (WarehouseLocationAreaTypeEnum item : WarehouseLocationAreaTypeEnum.values()) {
                if (code.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }
}
