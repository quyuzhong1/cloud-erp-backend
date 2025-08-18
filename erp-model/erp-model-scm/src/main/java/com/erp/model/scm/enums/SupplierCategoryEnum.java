package com.erp.model.scm.enums;

import com.common.core.constant.EnumMessage;
import org.apache.commons.lang3.StringUtils;

/**
 * 供应商分类枚举
 * @author will
 * @date 2025/8/18 10:53
 */
public enum SupplierCategoryEnum implements EnumMessage {

    LOGISTICS("logistics", "物流供应商"),
    OTHER("other", "其它供应商"),
    LOAN("loan", "货款供应商"),
    SELF_LOGISTICS("selfLogistics", "我司合作物流商"),
    PLATFORM_LOGISTICS("platformLogistics", "平台合作物流商"),
    CUSTOMER_LOGISTICS("customerLogistics", "客户指定物流商"),
    WAREHOUSE_LOGISTICS("warehouseLogistics", "仓储服务商"),
    OTHER_LOGISTICS("otherLogistics", "其他物流商"),
    ;

    private String code;
    private String name;

    SupplierCategoryEnum(String code, String name) {
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
        for (SupplierCategoryEnum statusEnum : SupplierCategoryEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }

}
