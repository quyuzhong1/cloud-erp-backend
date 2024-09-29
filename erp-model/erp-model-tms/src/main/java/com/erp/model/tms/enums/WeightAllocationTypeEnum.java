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
    BILLING_WEIGHT("billingWeight", "按{出库计费重}分摊"),
    NET_WEIGHT("netWeight", "按{出库实重}分摊"),
    VOLUME_WEIGHT("volumeWeight", "按{出库体积重}分摊"),
    PRODUCT_WEIGHT("productWeight", "按单产品重量分摊");

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
