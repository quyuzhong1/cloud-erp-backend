package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/5/15 16:43
 */
public enum TransferDirectionEnum implements EnumMessage {


    ORDINARY ("ordinary", "普通"),
    RETURN_GOODS("returnGoods", "退货");

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

    TransferDirectionEnum(String code, String name) {
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
            for (TransferDirectionEnum item : TransferDirectionEnum.values()) {
                if (code.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    /**
     * 根据代码获取
     * @param code
     * @return
     */
    public static TransferDirectionEnum of(String code) {
        return Arrays.stream(TransferDirectionEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }
}
