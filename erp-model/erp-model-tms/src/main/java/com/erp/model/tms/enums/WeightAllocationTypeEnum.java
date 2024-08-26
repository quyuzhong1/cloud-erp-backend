package com.erp.model.tms.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 重量分摊方式
 * @date 2024-08-25
 * @author tanmujin
 */
@Getter
@AllArgsConstructor
public enum WeightAllocationTypeEnum implements EnumMessage {
    CHARGED("charged", "按{出库计费重}分摊"),
    BOX_ACTUAL("boxActual", "按{出库实重}分摊"),
    VOLUME("volume", "按{出库体积重}分摊"),
    PRODUCT("product", "按单产品重量分摊");

    private String code;
    private String name;

    public static String getName(String code){
        for (WeightAllocationTypeEnum typeEnum : values()) {
            if(typeEnum.getCode().equals(code)){
                return typeEnum.getName();
            }
        }
        return "";
    }
}
