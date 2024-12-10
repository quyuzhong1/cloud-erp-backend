package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
@AllArgsConstructor
public enum RequisitionChangeTypeEnum implements EnumMessage {
    ADD("add", "新增" ),
    UPDATE("update", "修改"),
    DELETE("delete", "删除" ),
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

    public static String getName(String code) {
        if (StringUtils.isNotBlank(code)) {
            for (RequisitionChangeTypeEnum item : RequisitionChangeTypeEnum.values()) {
                if (code.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static RequisitionChangeTypeEnum getByCode(String code) {
        RequisitionChangeTypeEnum[] eumnList = RequisitionChangeTypeEnum.values();
        for (RequisitionChangeTypeEnum item : eumnList) {
            if (code.equals(item.getCode())) {
                return item;
            }
        }
        return null;
    }
}
